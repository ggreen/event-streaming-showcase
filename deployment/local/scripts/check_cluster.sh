#!/usr/bin/env bash

# RabbitMQ configuration
URL="http://localhost:15672/api/vhosts"
USER="vmware"
PASS="tanzu"
CHECK_INTERVAL=10  # Seconds to wait between health checks

failure_count=0

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
    else
      echo -e "WARNING (Failure ${failure_count}/3): ${issue_message}"
    fi
  else
    # Reset failure counter upon a successful check
    if [ ${failure_count} -gt 0 ]; then
      echo "OK: Issue resolved. All cluster states are back to running."
    else
      echo "OK: All cluster states are running."
    fi
    failure_count=0
  fi

  sleep "${CHECK_INTERVAL}"
done