#!/usr/bin/env bash
# ==============================================================================
# RabbitMQ Standby Replication - Health Check & DR Automation Script
# ==============================================================================

set -euo pipefail

# --- Configuration Section ---
UPSTREAM_USER="${RABBITMQ_UPSTREAM_USER:-vmware}"
UPSTREAM_PASS="${RABBITMQ_UPSTREAM_PASS:-tanzu}"
DOWNSTREAM_USER="${RABBITMQ_DOWNSTREAM_USER:-${UPSTREAM_USER}}"
DOWNSTREAM_PASS="${RABBITMQ_DOWNSTREAM_PASS:-${UPSTREAM_PASS}}"

# Health check endpoints on all upstream nodes
UPSTREAM_NODES=(
  "${UPSTREAM_NODE1:-http://localhost:15672}"
  "${UPSTREAM_NODE2:-http://localhost:15672}"
  "${UPSTREAM_NODE3:-http://localhost:15672}"
)

# Downstream nodes to call for cluster promotion
DOWNSTREAM_NODES=(
  "${DOWNSTREAM_NODE1:-http://localhost:15673}"
  "${DOWNSTREAM_NODE2:-http://localhost:15673}"
  "${DOWNSTREAM_NODE3:-http://localhost:15673}"
)

# Relative promotion API endpoint path
PROMOTE_ENDPOINT_PATH="/api/tanzu/osr/downstream/promote"

# Timing & Retry Thresholds
CHECK_INTERVAL=10          # Seconds between iterations
MAX_CONSECUTIVE_FAILURES=6  # 6 * 10s = 60s failure window before triggering
CURL_CONNECT_TIMEOUT=3     # Seconds allowed for TCP connection
CURL_MAX_TIME=5            # Total seconds allowed per curl execution

# State File (Persists across script restarts to avoid multi-promotions on flap)
STATE_FILE="/var/run/rabbitmq_dr_promoted.state"

# --- Helper Functions ---

log() {
  echo "[$(date -u +'%Y-%m-%dT%H:%M:%SZ')] $*"
}

log_err() {
  echo "[$(date -u +'%Y-%m-%dT%H:%M:%SZ')] ERROR: $*" >&2
}

# Check if the cluster was previously promoted (across script runs)
is_already_promoted() {
  [[ -f "${STATE_FILE}" ]]
}

mark_as_promoted() {
  touch "${STATE_FILE}"
}

# Verify cluster health by inspecting /api/vhosts cluster_state values.
# The endpoint/node is considered healthy if at least one cluster_state value is "running".
check_cluster_health() {
  local base_url="$1"
  local vhost_endpoint="${base_url}/api/vhosts"

  local response_file
  response_file=$(mktemp)

  local http_code
  http_code=$(curl -s -o "${response_file}" -w "%{http_code}" \
    --connect-timeout "${CURL_CONNECT_TIMEOUT}" \
    --max-time "${CURL_MAX_TIME}" \
    -u "${UPSTREAM_USER}:${UPSTREAM_PASS}" \
    "${vhost_endpoint}" 2>/dev/null || echo "000")

  if [[ "${http_code}" -ne 200 ]]; then
    rm -f "${response_file}"
    return 1
  fi

  # Extract all cluster_state node values across all vhosts.
  # If at least one node state equals "running", the cluster is actively serving clients.
  local running_count
  running_count=$(jq -r '.[].cluster_state | values[]' "${response_file}" 2>/dev/null | grep -c "^running$" || true)

  rm -f "${response_file}"

  if [[ "${running_count}" -gt 0 ]]; then
    return 0 # Healthy: cluster has active/running nodes across vhosts
  else
    return 1 # Unhealthy: no nodes report "running" state in any vhost
  fi
}

# Action: Trigger downstream promotion across all downstream nodes
promote_downstream_cluster() {
  log "ACTION: Initiating Disaster Recovery Failover — Promoting downstream cluster..."

  if is_already_promoted; then
    log_err "ABORT: Cluster marked as ALREADY PROMOTED in ${STATE_FILE}."
    log_err "Manual operator intervention required. Refusing to re-promote."
    return 1
  fi

  local success_count=0
  local total_nodes=${#DOWNSTREAM_NODES[@]}

  # Iterate over all downstream promotion nodes
  for node_base_url in "${DOWNSTREAM_NODES[@]}"; do
    local promote_url="${node_base_url}${PROMOTE_ENDPOINT_PATH}"
    local response_file
    response_file=$(mktemp)

    log "Calling promotion endpoint: ${promote_url}"

    local http_code
    http_code=$(curl -s -o "${response_file}" -w "%{http_code}" \
      --connect-timeout "${CURL_CONNECT_TIMEOUT}" \
      --max-time 30 \
      -X POST \
      -u "${DOWNSTREAM_USER}:${DOWNSTREAM_PASS}" \
      "${promote_url}" 2>/dev/null || echo "000")

    local body
    body=$(cat "${response_file}" 2>/dev/null || true)
    rm -f "${response_file}"

    if [[ "${http_code}" -ge 200 && "${http_code}" -lt 300 ]]; then
      log "SUCCESS: Node ${promote_url} accepted promotion request (HTTP ${http_code})."
      [ -n "${body}" ] && log "Response payload: ${body}"
      ((success_count++))
    else
      log_err "Promotion FAILED on node ${promote_url} with HTTP status ${http_code}."
      [ -n "${body}" ] && log_err "Server Response: ${body}"
    fi
  done

  # Require at least one node to succeed before marking the cluster as promoted
  if [[ ${success_count} -gt 0 ]]; then
    log "SUCCESS: Promotion dispatched to downstream cluster (${success_count}/${total_nodes} nodes succeeded)."
    mark_as_promoted
    return 0
  else
    log_err "CRITICAL: Promotion failed across ALL downstream nodes."
    return 1
  fi
}

# --- Main Monitoring Loop ---

main() {
  log "Starting RabbitMQ DR Observer..."
  log "Monitoring upstream endpoints: ${UPSTREAM_NODES[*]}"
  log "Target downstream promotion nodes: ${DOWNSTREAM_NODES[*]}"

  local failure_count=0

  while true; do
    local active_endpoints_count=0
    local total_endpoints=${#UPSTREAM_NODES[@]}

    # Query /api/vhosts across all configured node URLs
    for node_url in "${UPSTREAM_NODES[@]}"; do
      if check_cluster_health "${node_url}"; then
        ((active_endpoints_count++))
      fi
    done

    # DR Logic: Require ZERO endpoints reporting active/running cluster states
    if [[ ${active_endpoints_count} -eq 0 ]]; then
      ((failure_count++))
      log_err "WARNING: ALL ${total_endpoints} upstream endpoints failed health checks (Failure ${failure_count}/${MAX_CONSECUTIVE_FAILURES})."

      if [[ ${failure_count} -ge ${MAX_CONSECUTIVE_FAILURES} ]]; then
        log_err "CRITICAL: Outage threshold reached. Initiating failover!"
        if promote_downstream_cluster; then
          log "Failover sequence triggered. Exiting monitoring loop to prevent re-promotion."
          exit 0
        else
          log_err "Failover attempt failed. Retrying on next iteration..."
        fi
      fi
    else
      # Reset failure threshold if AT LEAST ONE endpoint confirms running vhosts
      if [[ ${failure_count} -gt 0 ]]; then
        log "RECOVERY: At least 1 endpoint confirmed running cluster state (${active_endpoints_count}/${total_endpoints} responding). Resetting failure counter."
      fi
      failure_count=0
    fi

    sleep "${CHECK_INTERVAL}"
  done
}

main "$@"