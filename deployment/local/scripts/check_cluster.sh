#!/usr/bin/env bash

# RabbitMQ configuration (add list of nodes to check in a single cluster)
# Note 3 identical URLs are added for demo purposes only to represent a typical 3 broker cluster
URLS=(
  "http://localhost:15672/api/vhosts"
  "http://localhost:15672/api/vhosts"
  "http://localhost:15672/api/vhosts"
)

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

# Function to check a single URL's status
check_url_status() {
  local target_url="$1"

  # Fetch HTTP status code and response body
  local response
  response=$(curl -s -w "\n%{http_code}" -u "${USER}:${PASS}" "${target_url}")
  local curl_status=$?

  # 1. Check curl connection error
  if [ ${curl_status} -ne 0 ]; then
    echo "Failed to connect to ${target_url} (curl error code: ${curl_status})"
    return 1
  fi

  # Separate body and HTTP status code
  local http_code
  local body
  http_code=$(echo "${response}" | tail -n 1)
  body=$(echo "${response}" | sed '$d')

  # 2. Check HTTP status code
  if [ "${http_code}" -ne 200 ]; then
    echo "${target_url} returned HTTP status ${http_code}"
    return 1
  fi

  # 3. Parse JSON to check if any cluster_state node is not 'running'
  local not_running
  not_running=$(echo "${body}" | jq -r '.[].cluster_state | to_entries[] | select(.value != "running") | "\(.key) is \(.value)"' 2>/dev/null)

  if [ -n "${not_running}" ]; then
    echo "${target_url} has non-running node(s): ${not_running}"
    return 1
  fi

  # URL is fully healthy
  return 0
}

while true; do
  failed_urls_count=0
  url_issue_messages=""

  # Check every URL in the array
  for current_url in "${URLS[@]}"; do
    err_msg=$(check_url_status "${current_url}")
    if [ $? -ne 0 ]; then
      failed_urls_count=$((failed_urls_count + 1))
      url_issue_messages="${url_issue_messages}\n - ${err_msg}"
    fi
  done

  # Issue is triggered ONLY when ALL URLs fail
  if [ ${failed_urls_count} -eq ${#URLS[@]} ]; then
    failure_count=$((failure_count + 1))

    if [ ${failure_count} -ge 3 ]; then
      echo -e "CRITICAL (Failure ${failure_count}/3): ALL ${#URLS[@]} endpoints failed health check:${url_issue_messages}"

      # Execute promotion on threshold trigger (only once until recovery)
      if [ "${promoted}" = false ]; then
        promote_downstream_cluster
        promoted=true
      fi
    else
      echo -e "WARNING (Failure ${failure_count}/3): ALL ${#URLS[@]} endpoints failed health check:${url_issue_messages}"
    fi
  else
    # If at least one URL passed, reset failure counter and promotion status
    if [ ${failed_urls_count} -gt 0 ]; then
      echo "OK: Partial failure detected (${failed_urls_count}/${#URLS[@]} failed), but at least one endpoint is alive."
    else
      if [ ${failure_count} -gt 0 ]; then
        echo "OK: Issue resolved. All endpoint cluster states are back to running."
      else
        echo "OK: All endpoint cluster states are running."
      fi
    fi

    failure_count=0
    promoted=false
  fi

  sleep "${CHECK_INTERVAL}"
done