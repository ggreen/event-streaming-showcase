#!/usr/bin/env bash

# See https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-rabbitmq-oci/4-3/tanzu-rabbitmq-oci-image/site-standby-replication-without-k8s.html#-configure-warm-standby-replication-by-using-the-http-api
#

# RabbitMQ configuration
URL="http://localhost:15672/api/vhosts"
#promote_url="http://localhost:15673/api/tanzu/osr/standby/promote"
promote_url="http://localhost:15673/api/tanzu/osr/downstream/promote"

USER="vmware"
PASS="tanzu"
CHECK_INTERVAL=10  # Seconds to wait between health checks

failure_count=0
promoted=false

# Function to handle downstream cluster promotion
promote_downstream_cluster() {
  echo "ACTION: Initiating failover — Promoting downstream cluster..."



  # Temporary file to capture stderr (curl connection errors)
  local curl_err_file
  curl_err_file=$(mktemp)

  # Perform POST request and extract HTTP status code + response body
  local response
  response=$(curl -s -w "\n%{http_code}" -X POST -u "${USER}:${PASS}" "${promote_url}" 2>"${curl_err_file}")
  local curl_status=$?

  # 1. Check curl network/connection failure
  if [ ${curl_status} -ne 0 ]; then
    local curl_err
    curl_err=$(cat "${curl_err_file}")
    rm -f "${curl_err_file}"

    echo "ERROR: Failed to reach promotion endpoint ${promote_url} (curl exit code: ${curl_status})." >&2
    [ -n "${curl_err}" ] && echo "Details: ${curl_err}" >&2
    return 1
  fi

  rm -f "${curl_err_file}"

  # Separate body and HTTP status code
  local http_code
  local body
  http_code=$(echo "${response}" | tail -n 1)
  body=$(echo "${response}" | sed '$d')

  # 2. Check HTTP status code (Accept 200-299 success range)
  if [[ "${http_code}" -ge 200 && "${http_code}" -lt 300 ]]; then
    echo "ACTION: Downstream cluster promotion succeeded (HTTP ${http_code}). url: ${promote_url}"
    [ -n "${body}" ] && echo "Response: ${body}"
    return 0
  else
    echo "ERROR: Downstream cluster promotion failed with HTTP status ${http_code}, promote_url: ${promote_url}." >&2
    if [ -n "${body}" ]; then
      echo "Server Error Response: ${body}" >&2
    else
      echo "Server Error Response: [Empty Body]" >&2
    fi
    return 1
  fi
}

while true; do
  # Fetch HTTP status code and response body in a single curl call
  response=$(curl -s -w "\n%{http_code}" -u "${USER}:${PASS}" "${URL}")
  curl_status=$?

  issue_found=0
  issue_message=""

  # 1. Check curl connection error
  if [ ${curl_status} -ne 0 ]; then
    issue_found=1
    issue_message="Failed to connect to URL: ${URL} (curl error code: ${curl_status})"
  else
    # Separate body and HTTP status code
    http_code=$(echo "${response}" | tail -n 1)
    body=$(echo "${response}" | sed '$d')

    # 2. Check HTTP status code
    if [ "${http_code}" -ne 200 ]; then
      issue_found=1
      issue_message="RabbitMQ API returned HTTP status code ${http_code}"
    else
      # 3. Parse JSON to check if any cluster_state node is not 'running'
      not_running=$(echo "${body}" | jq -r '.[].cluster_state | to_entries[] | select(.value != "running") | "\(.key) is \(.value)"' 2>/dev/null)

      if [ -n "${not_running}" ]; then
        issue_found=1
        issue_message="Found node(s) not running:\n${not_running}"
      fi
    fi
  fi

  # Handle check outcome and track failures
  if [ ${issue_found} -eq 1 ]; then
    failure_count=$((failure_count + 1))

    if [ ${failure_count} -ge 3 ]; then
      echo -e "CRITICAL (Failure ${failure_count}/3): ${issue_message}"

      # Execute promotion on the threshold trigger (and only once until recovery)
      if [ "${promoted}" = false ]; then
        promote_downstream_cluster
        promoted=true
      fi
    else
      echo -e "WARNING (Failure ${failure_count}/3): ${issue_message}"
    fi
  else
    # Reset failure counter and promotion status upon a successful check
    if [ ${failure_count} -gt 0 ]; then
      echo "OK: Issue resolved. All cluster states are back to running."
    else
      echo "OK: All cluster states are running."
    fi
    failure_count=0
    promoted=false
  fi

  sleep "${CHECK_INTERVAL}"
done