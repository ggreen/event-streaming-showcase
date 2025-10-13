
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


Adds a federation upstream named "origin" to downstream
```shell
podman exec -it  rabbitmq  rabbitmqctl set_parameter shovel bunny-shovel \
  '{"src-protocol": "amqp091", "src-uri": "amqp://", "src-queue": "bunny-queue", "dest-protocol": "amqp091", "dest-uri": "amqp://bunny", "dest-exchange": "amq.topic"}'
```    

Start An AMQP Source for cluster 1

```shell
java -jar applications/sources/http-amqp-source/target/http-amqp-source-0.0.8-SNAPSHOT.jar --spring.rabbitmq.host=localhost --server.port=8551 --spring.rabbitmq.port=5672 --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --spring.application.name=amq.topic --spring.cloud.stream.bindings.output.destination=routing-cluster-exchange
```

Start Cluster 1 AMQP Consumer

```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-0.0.4-SNAPSHOT.jar --spring.profiles.active=amqp --spring.rabbitmq.host=localhost --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --spring.rabbitmq.port=5672  --spring.rabbitmq.queue=event-log-sink --spring.rabbitmq.routing.key="robot.*" --spring.rabbitmq.exchange=amq.topic spring.cloud.stream.bindings.input.destination=event-log-sink
```

Start Cluster 1 MQTT Source

```shell
java -jar applications/sources/http-mqtt-source/target/http-mqtt-source-0.0.2-SNAPSHOT.jar \
  --mqtt.connectionUrl=tcp://localhost:1883 \
  --spring.application.name=http-mqtt-source \
  --mqtt.userName=guest \
  --mqtt.userPassword=guest --server.port=8383
``` 

```shell
open http://localhost:8383
```

Start Cluster 1 MQTT Consumer

```shell
java -jar applications/sources/http-mqtt-source/target/http-mqtt-source-0.0.2-SNAPSHOT.jar \
  --mqtt.connectionUrl=tcp://localhost:1883 \
  --spring.application.name=http-mqtt-source \
  --mqtt.userName=guest \
  --mqtt.userPassword=guest
``` 



Start An AMQP Source for cluster 2

```shell
java -jar applications/sources/http-amqp-source/target/http-amqp-source-0.0.8-SNAPSHOT.jar --spring.rabbitmq.host=localhost --server.port=8552 --spring.rabbitmq.port=5222 --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --spring.application.name=amq.topic --spring.cloud.stream.bindings.output.destination=routing-cluster-exchange
```


Post from MQTT Source to AMQP in Cluster 1

```shell

```