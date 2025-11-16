# LAB 02 - Rabbit setup w Pub/Sub Apps

**Prerequisite**

- Download Source Code

Example with git
```shell
git clone https://github.com/ggreen/event-streaming-showcase.git
cd event-streaming-showcase
```


Create the podman network (if not existing)
```shell
podman network create tanzu
```

- Run RabbitMQ (if not running)

```shell
podman run --name rabbitmq01  --network tanzu --rm -it -e RABBITMQ_MANAGEMENT_ALLOW_WEB_ACCESS=true -p 5672:5672 -p 5552:5552 -p 15672:15672  -p  1883:1883  rabbitmq:4.2-management 
```

- View Logs (wait for message: started TCP listener on [::]:5672)



- Open Management Console with credentials *guest/guest*
```shell
open http://localhost:15672
```

# 1 - Work Queues

## Start Consumer
```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-1.0.0.jar --spring.application.name=event-log-sink1   --spring.rabbitmq.host=localhost --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --spring.cloud.stream.bindings.input.group=event-log-sink --spring.profiles.active=ampq --spring.cloud.stream.bindings.input.destination=accounts.account --spring.cloud.stream.rabbit.bindings.input.consumer.quorum.enabled=true
```
Start Another Consumer

```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-1.0.0.jar --spring.application.name=event-log-sink2  --spring.rabbitmq.host=localhost --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --spring.cloud.stream.bindings.input.group=event-log-sink --spring.profiles.active=ampq --spring.cloud.stream.bindings.input.destination=accounts.account --spring.cloud.stream.rabbit.bindings.input.consumer.quorum.enabled=true
```

## Start Publisher

Publish

```shell
java -jar applications/sources/event-account-http-source/target/event-account-http-source-1.0.0.jar --spring.profiles.active=amqp --spring.rabbitmq.host=localhost --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --server.port=8095 --spring.cloud.stream.bindings.output.destination=accounts.account
```

Send messages (round-robin)

```shell
curl -X 'POST' \
  'http://localhost:8095/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "01",
  "name": "Account 1",
  "accountType": "business",
  "status": "OPEN",
  "notes": "Notes for account",
  "location": {
    "id": "acc-01-loc-01",
    "address": "123 ",
    "cityTown": "Springfield",
    "stateProvince": "IL",
    "zipPostalCode": "62701",
    "countryCode": "US"
  }
}'
```


```shell
curl -X 'POST' \
  'http://localhost:8095/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "01",
  "name": "Account 2",
  "accountType": "business",
  "status": "OPEN",
  "notes": "Notes for account",
  "location": {
    "id": "acc-02-loc-02",
    "address": "123 Straight Street",
    "cityTown": "Springfield",
    "stateProvince": "IL",
    "zipPostalCode": "62701",
    "countryCode": "US"
  }
}'
```


## Review  Management Console (guest/guest)

```shell
open http://localhost:15672
```

- Click Overview
- Click Connections
- Click Queues and Streams


Review  Management Console

- Click Overview
- Click Connections
- Click Queues and Streams


Stop Publisher and Consumers

---------------------------
# 2 - Direct Exchange Routing


## Start Consumer
```shell
java -jar applications/sinks/rabbit-consumer-cli/target/rabbit-consumer-cli-1.0.0.jar  --clientName=Receive1 --queue=app.receive.1 --server.port=0
```
Start Another Consumer
```shell
java -jar applications/sinks/rabbit-consumer-cli/target/rabbit-consumer-cli-1.0.0.jar --clientName=Receive2 --queue=app.receive.2  --server.port=0
```


## Start Publisher

Publish

```shell
 java -jar applications/sources/rabbit-publisher-cli/target/rabbit-publisher-cli-1.0.0.jar --routingKey=app.receive.1 --message="Testing app.receive.1"  --server.port=0
``` 
Hit Enter

```shell
 java -jar applications/sources/rabbit-publisher-cli/target/rabbit-publisher-cli-1.0.0.jar --routingKey=app.receive.2 --message="Testing app.receive.2"  --server.port=0
```
Hit Enter


Stop Publisher and Consumers

---------------------------
# 2 - Topic Exchange Routing



## Start Consumer
```shell
java -jar applications/sinks/rabbit-consumer-cli/target/rabbit-consumer-cli-1.0.0.jar   --clientName=rahway --exchange="amq.topic" --routingKey="city.Rahway.*" --queue=app.receive.rahway  --server.port=0
``` 
Start Another Consumer
```shell
java -jar applications/sinks/rabbit-consumer-cli/target/rabbit-consumer-cli-1.0.0.jar  --clientName=ny --exchange="amq.topic" --routingKey="city.NY.#" --queue=app.receive.ny  --server.port=0
```


## Start Publisher

Publish

```shell
 java -jar applications/sources/rabbit-publisher-cli/target/rabbit-publisher-cli-1.0.0.jar   --exchange="amq.topic" --routingKey=city.NY.uptown.store --message="Testing NY City"  --server.port=0
```

Hit Enter


```shell
 java -jar applications/sources/rabbit-publisher-cli/target/rabbit-publisher-cli-1.0.0.jar   --exchange="amq.topic" --routingKey=city.Rahway.office --message="Testing Rahway"  --server.port=0
```

Hit Enter

Stop Publisher and Consumers