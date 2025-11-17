# LAB 03 - Rabbit as a Streaming Broker

**Prerequisite**


- Download Source Code

Example with git
```shell
git clone https://github.com/ggreen/event-streaming-showcase.git
cd event-streaming-showcase
```


- Run RabbitMQ (if not running)

```shell
deployment/local/containers/rabbit.sh
```

- View Logs (wait for message: started TCP listener on [::]:5672)


- Open Management Console with credentials *guest/guest*
```shell
open http://localhost:15672
```

# 1 - RabbitMQ Streaming 

## Start Consumer

```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-1.0.0.jar --spring.application.name=event-log-sink1   --spring.rabbitmq.host=localhost --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --spring.cloud.stream.bindings.input.group=accounts.account --spring.profiles.active=stream --spring.cloud.stream.bindings.input.destination=accounts.account --spring.cloud.stream.rabbit.bindings.input.consumer.containerType=STREAM
```


## Start Publisher

Publish

```shell
java -jar applications/sources/event-account-http-source/target/event-account-http-source-1.0.0.jar --spring.profiles.active=stream --spring.rabbitmq.host=localhost --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --server.port=8095 --spring.cloud.stream.bindings.output.destination=accounts.account
```

Send message

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


Send another message

```shell
curl -X 'POST' \
  'http://localhost:8095/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "02",
  "name": "Account 2",
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

Replay all messages

- Stop Consumer Ctrl-C
- Start with options to replay all messages

```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-1.0.0.jar --spring.application.name=event-log-sink1   --spring.rabbitmq.host=localhost --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --spring.cloud.stream.bindings.input.group=accounts.account --spring.profiles.active=stream --spring.cloud.stream.bindings.input.destination=accounts.account --spring.cloud.stream.rabbit.bindings.input.consumer.containerType=STREAM --rabbitmq.streaming.offset=first
```

Stop Consumer Ctrl-C

Start from next message

```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-1.0.0.jar --spring.application.name=event-log-sink1   --spring.rabbitmq.host=localhost --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --spring.cloud.stream.bindings.input.group=accounts.account --spring.profiles.active=stream --spring.cloud.stream.bindings.input.destination=accounts.account --spring.cloud.stream.rabbit.bindings.input.consumer.containerType=STREAM --rabbitmq.streaming.offset=next
```


Send another message

```shell
curl -X 'POST' \
  'http://localhost:8095/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "03",
  "name": "Account Next",
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


## Review  Management Console (guest/guest)

```shell
open http://localhost:15672
```

Review  Management Console

- Click Overview
- Click Connections
- Click Queues and Streams


# Shutdown/Cleanup

Stop applications


---------------------------
# 3 - Spring Single Active Consumer


Deploy Event Log Application

Consumer 1

```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-1.0.0.jar --spring.application.name=event-log-sink  --spring.rabbitmq.host=localhost --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --spring.profiles.active=superStream --spring.cloud.stream.bindings.input.destination=accounts.account.superstream --rabbitmq.streaming.offset=last --rabbitmq.streaming.partitions=2 --spring.cloud.stream.rabbit.bindings.input.consumer.singleActiveConsumer=true
```


Consumer 2

```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-1.0.0.jar --spring.application.name=event-log-sink  --spring.rabbitmq.host=localhost --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --spring.profiles.active=superStream --spring.cloud.stream.bindings.input.destination=accounts.account.superstream --rabbitmq.streaming.offset=last --rabbitmq.streaming.partitions=2 --spring.cloud.stream.rabbit.bindings.input.consumer.singleActiveConsumer=true
```

Deploy Http Source App

```shell
java -jar applications/sources/event-account-http-source/target/event-account-http-source-1.0.0.jar --spring.rabbitmq.host=localhost --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --server.port=8080 --spring.cloud.stream.bindings.output.destination=accounts.account.superstream --spring.profiles.active=superStream
```


Submit account
```shell
open http://localhost:8080/swagger-ui/index.html
```

```json
{
  "id": "001",
  "name": "Event Demo 1",
  "accountType": "standard",
  "status": "IN-PROGRESS",
  "notes": "Testing 123",
  "location": {
    "id": "001.001",
    "address": "1 Straight Stree",
    "cityTown": "Wayne",
    "stateProvince": "NJ",
    "zipPostalCode": "55555",
    "countryCode": "US"
  }
}
```


Example CLI

```shell
curl -X 'POST' \
  'http://localhost:8080/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "001",
  "name": "Event Demo 1",
  "accountType": "standard",
  "status": "IN-PROGRESS",
  "notes": "Testing 123",
  "location": {
    "id": "001.001",
    "address": "1 Straight Stree",
    "cityTown": "Wayne",
    "stateProvince": "NJ",
    "zipPostalCode": "55555",
    "countryCode": "US"
  }
}'
```

Review Logs for each


Note messages are routed by account type. See the application logs

Change Id to test routing

Example Json

Open Sources  Submit account
```shell
open http://localhost:8080/swagger-ui/index.html
```

```json
{
  "id": "002",
  "name": "Event Demo 2",
  "accountType": "residential",
  "status": "IN-PROGRESS",
  "notes": "Testing 222",
  "location": {
    "id": "002.002",
    "address": "2 Straight Stree",
    "cityTown": "JamesTown",
    "stateProvince": "NY",
    "zipPostalCode": "45555",
    "countryCode": "US"
  }
}
```
Example CLI

```shell
curl -X 'POST' \
  'http://localhost:8080/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "002",
  "name": "Event Demo 2",
  "accountType": "residential",
  "status": "IN-PROGRESS",
  "notes": "Testing 222",
  "location": {
    "id": "002.002",
    "address": "2 Straight Stree",
    "cityTown": "JamesTown",
    "stateProvince": "NY",
    "zipPostalCode": "45555",
    "countryCode": "US"
  }
}'
```



Send Message

```shell
curl -X 'POST' \
  'http://localhost:8080/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "003",
  "name": "Event Demo 3",
  "accountType": "premium",
  "status": "IN-PROGRESS",
  "notes": "Testing 3",
  "location": {
    "id": "002.002",
    "address": "2 Straight Street",
    "cityTown": "JamesTown",
    "stateProvince": "NY",
    "zipPostalCode": "45555",
    "countryCode": "US"
  }
}'
```


Loop

```shell
for i in {1..100}; do
    curl -X 'POST' \
  'http://localhost:8080/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d "{
  \"id\": \"00$i\",
  \"name\": \"Acct Demo i\",
  \"accountType\": \"premium$i\",
  \"status\": \"IN-PROGRESS\",
  \"notes\": \"Testing 3\",
  \"location\": {
    \"id\": \"002.002\",
    \"address\": \"2 Straight Street\",
    \"cityTown\": \"JamesTown\",
    \"stateProvince\": \"NY\",
    \"zipPostalCode\": \"45555\",
    \"countryCode\": \"US\"
  }
}";
done;
```

---------------------------
# Review Source Code

## Replay all messages


See [RabbitConsumerConfig.java](../../../../../applications/sinks/event-log-sink/src/main/java/showcase/event/stream/rabbitmq/log/sink/RabbitConsumerConfig.java)

Code Snippet

```java
    builder.subscriptionListener(
                            subscriptionContext -> subscriptionContext
                                    .offsetSpecification(OffsetSpecification.first()));
```
----------------------------
# Clean Up

Stop Applications/RabbitMQ


```shell
podman rm -f rabbitmq
```