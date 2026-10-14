
```shell
PLUGINS="rabbitmq_schema_definition_sync,rabbitmq_standby_replication_delayed_queue,rabbitmq_standby_replication,rabbitmq_jms,rabbitmq_jms_management,rabbitmq_amqp1_0,rabbitmq_shovel,rabbitmq_shovel_management,rabbitmq_stream,rabbitmq_stream_browser,rabbitmq_stream_management,rabbitmq_delayed_queue, rabbitmq_standby_replication_delayed_queue,rabbitmq_warm_standby,rabbitmq_schema_definition_sync_prometheus,rabbitmq_standby_replication_prometheus,rabbitmq_standby_replication_delayed_queue_prometheus,rabbitmq_web_amqp,rabbitmq_lint_management"

echo "Starting Tanzu RabbitMQ with initial plugins: $PLUGINS..."

podman network create rabbitmq 
podman run -it --rm \
--name tanzu-rabbitmq-upstream --network rabbitmq \
--hostname tanzu-rabbitmq-upstream \
-p 5672:5672 \
-p 5552:5552 \
-p 15672:15672 \
-p 1883:1883 \
-v $(pwd)/docs/DEMO/Local/HA/up/rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf:Z \
  -v $(pwd)/docs/DEMO/Local/HA/up/definitions.json:/etc/rabbitmq/definitions.json:Z \
-e RABBITMQ_ENABLED_PLUGINS="$PLUGINS" \
-e RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS="-rabbitmq_stream advertised_host localhost -rabbitmq_stream advertised_port 5552" \
rabbitmq.packages.broadcom.com/vmware-tanzu-rabbitmq:4.3.0
```


```shell
echo "1. Create dedicated schema vhost"
podman exec -it tanzu-rabbitmq-upstream rabbitmqctl add_vhost  "vhost1" --tags "standby_replication"

podman exec -it tanzu-rabbitmq-upstream rabbitmqctl add_user offsite-replicator "ReplPassword123"

podman exec -it tanzu-rabbitmq-upstream rabbitmqctl set_permissions -p vhost1 offsite-replicator ". *" ". *" ". *"

podman exec -it tanzu-rabbitmq-upstream rabbitmqctl set_permissions -p vhost1 schema-replicator ". *" ". *" ". *"

podman exec -it tanzu-rabbitmq-upstream rabbitmqctl set_permissions -p vhost1 vmware ". *" ". *" ". *"

podman exec -it tanzu-rabbitmq-upstream rabbitmqctl set_permissions -p vhost1 offsite-replicator ". *" ". *" ". *"

echo "Check the virtual host tags by running"

podman exec -it tanzu-rabbitmq-upstream  rabbitmqctl list_vhosts name tags

podman exec -it tanzu-rabbitmq-upstream  rabbitmqctl set_policy --vhost "vhost1" osr "^.*" '{"remote-dc-replicate": true}' --apply-to "queues"


curl -i -u vmware:tanzu -H "Content-Type: application/json" \
  -X PUT http://localhost:15672/api/queues/vhost1/q1.quorum \
  -d '{"durable": true, "arguments": {"x-queue-type": "quorum"}}'
  
  
curl -i -u "vmware:tanzu" \
  -H "Content-Type: application/json" \
  -X POST \
  -d '{
    "routing_key": "#",
    "arguments": {}
  }' \
  "http://localhost:15672/api/bindings/vhost1/e/amq.topic/q/q1.quorum"

podman exec -it tanzu-rabbitmq-upstream  rabbitmqctl enable_warm_standby
podman exec -it tanzu-rabbitmq-upstream  rabbitmqctl standby_replication_status


```


Start Downstream

```shell
PLUGINS="rabbitmq_schema_definition_sync,rabbitmq_standby_replication_delayed_queue,rabbitmq_standby_replication,rabbitmq_jms,rabbitmq_jms_management,rabbitmq_amqp1_0,rabbitmq_shovel,rabbitmq_shovel_management,rabbitmq_stream,rabbitmq_stream_browser,rabbitmq_stream_management,rabbitmq_delayed_queue, rabbitmq_standby_replication_delayed_queue,rabbitmq_warm_standby,rabbitmq_standby_replication_prometheus,rabbitmq_schema_definition_sync_prometheus,rabbitmq_standby_replication_delayed_queue_prometheus,rabbitmq_lint_management"

echo "Starting Tanzu RabbitMQ Downstream with plugins..."

podman run -it --rm \
  --name tanzu-rabbitmq-downstream \
  --network rabbitmq \
  --hostname tanzu-rabbitmq-downstream \
  -p 5673:5672 \
  -p 5553:5552 \
  -p 15673:15672 \
  -p 1884:1883 \
  -v $(pwd)/docs/DEMO/Local/HA/down/rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf:Z \
  -e RABBITMQ_ENABLED_PLUGINS="$PLUGINS" \
  -e RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS="-rabbitmq_stream advertised_host localhost -rabbitmq_stream advertised_port 5553" \
  rabbitmq.packages.broadcom.com/vmware-tanzu-rabbitmq:4.3.0
```


```shell
curl -i -u vmware:tanzu \
  -H "Content-Type: application/json" \
  -X POST \
  -d '{
    "properties": {
      "content_type": "application/json",
      "delivery_mode": 2
    },
    "routing_key": "account",
    "payload": "Hello, RabbitMQ!",
    "payload_encoding": "string"
  }' \
  "http://localhost:15672/api/exchanges/vhost1/amq.topic/publish"
```


Posting upstream messages


```shell
podman exec -it tanzu-rabbitmq-upstream rabbitmq-plugins list
```

*****************************************************


*Configure Downstream*


Verify Replication via CLI

```shell
echo "Check schema sync status"
podman exec -it tanzu-rabbitmq-downstream rabbitmqctl schema_replication_status

echo "Check standby message replication status"
podman exec -it tanzu-rabbitmq-downstream rabbitmqctl standby_replication_status
```





```shell
echo "Promoting downstream"
podman exec -it tanzu-rabbitmq-downstream  rabbitmqctl promote_standby_replication_downstream_cluster
```



```shell
podman exec -it tanzu-rabbitmq-downstream  rabbitmqctl restart_warm_standby
```


```shell
curl -X POST -u "schema-replicator:ReplPassword123" "http://localhost:15673/api/tanzu/osr/downstream/promote"
```
See documentation

https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-rabbitmq-oci/4-3/tanzu-rabbitmq-oci-image/site-standby-replication-without-k8s.html#-configure-warm-standby-replication-by-using-the-http-api

```shell

curl -X GET -u vmware:tanzu "http://localhost:15672/api/tanzu/osr/standby/status"     
```

```shell
curl  -X GET -u -i -u vmware:tanzu http://localhost:15672/api/tanzu/osr/standby/promotion/rabbi
```

```shell

curl -X GET -u vmware:tanzu "http://localhost:15672/api/tanzu/osr/standby/status"     
```


```shell
curl  -X GET -u -i -u vmware:tanzu http://localhost:15672/api/vhosts
```


```shell
curl  -X GET -u -i -u vmware:tanzu http://localhost:15672/api/tanzu/osr/schema/status

curl  -X GET -u -i -u vmware:tanzu http://localhost:15672/api/tanzu/osr/schema/status/rabbitmq%40tanzu-rabbitmq-upstream

curl  -X GET -u -i -u vmware:tanzu http://localhost:15672/api/tanzu/osr/schema/status/rabbit%40tanzu-rabbitmq-upstream


curl -i -u vmware:tanzu http://localhost:15672/api/vhosts
```



```text
HTTP/1.1 200 OK
allow: HEAD, GET, OPTIONS
cache-control: no-cache
content-length: 1423
content-security-policy: script-src 'self' 'unsafe-eval' 'unsafe-inline'; object-src 'self'
content-type: application/json
date: Wed, 07 Oct 2026 19:21:06 GMT
server: Cowboy
vary: accept, accept-encoding, origin

[{"name":"/","description":"Default virtual host","metadata":{"description":"Default virtual host","tags":[],"default_queue_type":"classic"},"tags":[],"default_queue_type":"classic","protected_from_deletion":false,"tracing":false,"cluster_state":{"rabbit@tanzu-rabbitmq-upstream":"running"}},{"messages":0,"name":"rabbitmq_schema_definition_sync","recv_oct":86396,"send_oct":91101,"description":"","metadata":{"description":"","tags":[],"default_queue_type":"classic"},"tags":[],"default_queue_type":"classic","messages_ready":0,"messages_unacknowledged":0,"protected_from_deletion":false,"tracing":false,"cluster_state":{"rabbit@tanzu-rabbitmq-upstream":"running"},"recv_oct_details":{"rate":432.8},"send_oct_details":{"rate":313.6},"messages_details":{"rate":0.0},"messages_unacknowledged_details":{"rate":0.0},"messages_ready_details":{"rate":0.0}},{"messages":7,"name":"vhost1","recv_oct":467,"send_oct":3131,"description":"","metadata":{"description":"","tags":["standby_replication"],"default_queue_type":"classic"},"tags":["standby_replication"],"default_queue_type":"classic","messages_ready":7,"messages_unacknowledged":0,"protected_from_deletion":false,"tracing":false,"cluster_state":{"rabbit@tanzu-rabbitmq-upstream":"running"},"recv_oct_details":{"rate":0.0},"send_oct_details":{"rate":79.0},"messages_details":{"rate":0.0},"messages_unacknowledged_details":{"rate":0.0},"messages_ready_details":{"rate":0.0}}]%       
```

```shell
#!/usr/bin/env bash

# RabbitMQ configuration
URL="http://localhost:15672/api/vhosts"
USER="vmware"
PASS="tanzu"

# Fetch HTTP status code and response body in a single curl call
response=$(curl -s -w "\n%{http_code}" -u "${USER}:${PASS}" "${URL}")

# Check if curl failed to connect
if [ $? -ne 0 ]; # curl error code
then
  echo "WARNING: Failed to connect to URL: ${URL}"
  exit 1
fi

# Separate body and HTTP status code
http_code=$(echo "${response}" | tail -n 1)
body=$(echo "${response}" | sed '$d')

# Verify HTTP success (200 OK)
if [ "${http_code}" -ne 200 ]; then
  echo "WARNING: RabbitMQ API returned HTTP status code ${http_code}"
  exit 1
fi

# Parse JSON to check if any cluster_state node is not 'running'
not_running=$(echo "${body}" | jq -r '.[].cluster_state | to_entries[] | select(.value != "running") | "\(.key) is \(.value)"')

if [ -n "${not_running}" ]; then
  echo "WARNING: Found node(s) not running:"
  echo "${not_running}"
  exit 1
else
  echo "OK: All cluster states are running."
fi
```