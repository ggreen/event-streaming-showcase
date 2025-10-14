
Start RabbitMq


```shell
./deployment/local/containers/rabbit.sh
```


Start a start cluster instance

```shell
deployment/local/containers/bunny.sh
```

Open Bunny

```shell
open http://localhost:25672
```


Create Exchange

```shell
podman exec -it  rabbitmq rabbitmqadmin declare exchange name=routing-cluster  type=topic durable=true
```



```shell
podman exec -it  rabbitmq rabbitmqadmin declare queue name=bunny-queue  queue_type=quorum 
```


Bind

```shell
podman exec -it  rabbitmq rabbitmqadmin declare binding source=routing-cluster destination=bunny-queue  routing_key="#"
```


Adds a federation upstream named "origin" to downstream
```shell
podman exec -it  rabbitmq  rabbitmqctl set_parameter shovel bunny-shovel \
  '{"src-protocol": "amqp091", "src-uri": "amqp://", "src-queue": "bunny-queue", "dest-protocol": "amqp091", "dest-uri": "amqp://bunny", "dest-exchange": "amq.topic"}'
```




Source - cluster 1 - AMQP

```shell
java -jar applications/sources/http-amqp-source/target/http-amqp-source-0.0.8-SNAPSHOT.jar --spring.rabbitmq.host=localhost --server.port=8551 --spring.rabbitmq.port=5672 --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --spring.application.name=http-amqp-source --spring.cloud.stream.bindings.output.destination=routing-cluster
```

Consumer - Cluster 1 - AMQP 

```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-0.0.4-SNAPSHOT.jar  --spring.profiles.active=amq --spring.cloud.stream.bindings.input.group=event-sink-log --spring.cloud.stream.bindings.input.destination=amq.topic --spring.cloud.stream.rabbit.bindings.input.consumer.queueNameGroupOnly=true --spring.cloud.stream.rabbit.bindings.input.consumer.bindingRoutingKey="robot.*"
```



Start Cluster 1 MQTT Source

```shell
java -jar applications/sources/http-mqtt-source/target/http-mqtt-source-0.0.2-SNAPSHOT.jar \
  --mqtt.connectionUrl=tcp://localhost:1883 \
  --spring.application.name=mqtt-consumer-1 \
  --mqtt.userName=guest \
  --mqtt.userPassword=guest --server.port=8383
``` 

```shell
open http://localhost:8383
```


MQTT consumer cluster 2


```shell
java -jar applications/sinks/mqtt-log-sink/target/mqtt-log-sink-0.0.2-SNAPSHOT.jar --mqtt.connectionUrl="tcp://localhost:21883" --mqtt.userName=guest --mqtt.userPassword="guest" --mqtt.topic.filter="robot/+" --spring.application.name=mqtt-log-sink
```


MQTT Source in Cluster 1 to AMQP Consumer Cluster 1

```shell
curl -X 'POST' \
  'http://localhost:8383/mqtt?topic=robot%2Ftest' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '"Hello World"'
```


AMQP Source in Cluster 1 to MQTT Consumer Cluster 2

```shell
open http://localhost:8551
```

```shell
curl -X 'POST' \
  'http://localhost:8551/amqp/?exchange=routing-cluster&routingKey=robot.test' \
  -H 'accept: application/hal+json' \
  -H 'rabbitContentType: application/json' \
  -H 'Content-Type: application/json' \
  -d '"Hi Terminal 1"'
```