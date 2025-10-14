#  event-log-sink

This application is a Spring Cloud Stream RabbitMQ Stream Sink that consumes messages from a RabbitMQ Stream and 
logs the messages to the console.

It can be used to demonstrate consuming messages from a RabbitMQ Stream.

Configuration

| Property                                                              | Description   | 
|-----------------------------------------------------------------------|---------------|
| spring.cloud.stream.bindings.input.destination                        | Exchange name |
| spring.cloud.stream.bindings.input.group                              | Queue name    |
| spring.cloud.stream.rabbit.bindings.input.consumer.queueNameGroupOnly | true or false |
| spring.cloud.stream.rabbit.bindings.input.consumer.bindingRoutingKey  | Default #     |

| spring.profiles.active                                                | amqp or stream |
| spring.rabbitmq.username                                              | user name      |
| spring.rabbitmq.password                                              | password       |
| spring.rabbitmq.host                                                  | host           |
| spring.rabbitmq.port                                                  | port           |
| spring.rabbitmq.routing.key                                           | routing key    |



AMQP Consumer

```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-0.0.4-SNAPSHOT.jar --spring.profiles.active=amqp --spring.rabbitmq.host=localhost --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --spring.rabbitmq.port=5672  --spring.rabbitmq.queue=event-log-sink --spring.rabbitmq.routing.key="robot.*" --spring.rabbitmq.exchange=amq.topic spring.cloud.stream.bindings.input.destination=event-log-sink
```

Example command to run the consumer last

```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-0.0.4-SNAPSHOT.jar --spring.cloud.stream.bindings.input.destination=event.stream --spring.profiles.active=stream --rabbitmq.streaming.offset=last 
```

Replay stream
```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-0.0.4-SNAPSHOT.jar --spring.cloud.stream.bindings.input.destination=event.stream --spring.profiles.active=stream --rabbitmq.streaming.offset=first 
```

```shell
rabbitmqctl -n rabbit enable_feature_flag stream_filtering
```

Filtering

```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-0.0.4-SNAPSHOT.jar --spring.cloud.stream.bindings.input.destination=event.stream --spring.profiles.active=stream --rabbitmq.streaming.offset=last --rabbitmq.streaming.filter.values="NY"
```


Singe Active Consumer

```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-0.0.4-SNAPSHOT.jar --spring.profiles.active=ampq --spring.rabbitmq.queue=event-log-sink --spring.rabbitmq.routing.key="robot.*" --spring.rabbitmq.exchange=amq.topic

```

----------------------------
Consuming MQTT messages

```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-0.0.4-SNAPSHOT.jar --spring.cloud.stream.bindings.input.destination=showcase.event.super.streaming.accounts --spring.profiles.active=superStream --rabbitmq.streaming.offset=last --rabbitmq.streaming.partitions=2  --spring.cloud.stream.rabbit.bindings.input.consumer.singleActiveConsumer=true
```

----------------------------

# RabbitMQ CLI Tip

Review stream offsets
```shell
rabbitmq-streams -n rabbit stream_status showcase.event.streaming.accounts --tracking
```

## Docker building image

```shell
mvn install
cd applications/event-log-sink
mvn package

docker build  --platform linux/amd64,linux/arm64 -t event-log-sink:0.0.2-SNAPSHOT .

#mvn spring-boot:build-image
```

linux/arm/v7

```shell
docker tag event-log-sink:0.0.2-SNAPSHOT cloudnativedata/event-log-sink:0.0.2-SNAPSHOT
docker push cloudnativedata/event-log-sink:0.0.2-SNAPSHOT
```

docker run cloudnativedata/event-log-sink:0.0.2-SNAPSHOT