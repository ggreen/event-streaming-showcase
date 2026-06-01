PLUGINS="rabbitmq_jms,rabbitmq_jms_management,rabbitmq_amqp1_0,rabbitmq_shovel,rabbitmq_shovel_management,rabbitmq_stream,rabbitmq_stream_browser,rabbitmq_stream_management,rabbitmq_delayed_queue"

echo "Starting Tanzu RabbitMQ with initial plugins: $PLUGINS..."

podman run -it --rm \
  --name tanzu-rabbitmq \
  -p 5672:5672 \
  -p 5552:5552 \
  -p 15672:15672 \
  -p 1883:1883 \
  -e RABBITMQ_ENABLED_PLUGINS="$PLUGINS" \
  -e RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS="-rabbitmq_stream advertised_host localhost -rabbitmq_stream advertised_port 5552" \
  rabbitmq.packages.broadcom.com/vmware-tanzu-rabbitmq:4.3.0