# LAB 02 - Rabbit setup w Pub/Sub Apps

**Prerequisite**

- Download Source Code

Example with git
```shell
git clone https://github.com/ggreen/event-streaming-showcase.git
cd event-streaming-showcase
```

Run RabbitMQ 

```shell
deployment/local/containers/rabbit.sh
```

- Open Management Console with credentials *guest/guest*

```shell
open http://localhost:15672
```

# 1—Work Queues

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


Stop Publisher and Consumers using CTRL-C

---------------------------
# 2 - Direct Exchange Routing


## Start Consumer
```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-1.0.0.jar  --spring.application.name=event-log-corporate --spring.cloud.stream.rabbit.bindings.input.consumer.bindingRoutingKey=corporate --server.port=0 --spring.cloud.stream.rabbit.bindings.input.consumer.exchangeType=direct --spring.cloud.stream.rabbit.bindings.input.consumer.quorum.enabled=true --spring.cloud.stream.bindings.input.destination=accounts.account.direct
```

Start Another Consumer

```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-1.0.0.jar  --spring.application.name=event-log-residential --spring.cloud.stream.rabbit.bindings.input.consumer.bindingRoutingKey=residential --server.port=0 --spring.cloud.stream.rabbit.bindings.input.consumer.exchangeType=direct --spring.cloud.stream.rabbit.bindings.input.consumer.quorum.enabled=true --spring.cloud.stream.bindings.input.destination=accounts.account.direct
```


## Start Publisher

Publish with direct exchange

```shell
 java -jar applications/sources/event-account-http-source/target/event-account-http-source-1.0.0.jar --amqp.exchange.direct=true  --spring.profiles.active=amqp --server.port=8095 --spring.cloud.stream.bindings.output.destination=accounts.account.direct
``` 

Publisher

```shell
curl -X 'POST' \
  'http://localhost:8095/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "01",
  "name": "corporate account 1",
  "accountType": "corporate",
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


Publish account

```shell
curl -X 'POST' \
  'http://localhost:8095/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "R01",
  "name": "residential account 1",
  "accountType": "residential",
  "status": "OPEN",
  "notes": "Notes for account",
  "location": {
    "id": "residential-02-loc-02",
    "address": "123 Straight Street",
    "cityTown": "Springfield",
    "stateProvince": "IL",
    "zipPostalCode": "62701",
    "countryCode": "US"
  }
}'
```


Stop Publisher and Consumers using CTRL-C

---------------------------
# 2 - Topic Exchange Routing


Spring Cloud Stream Rabbit Consumer Properties start with 

    spring.cloud.stream.rabbit.bindings.input.consumer..

## Start Consumer

```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-1.0.0.jar  --spring.application.name=event-log-premium  --server.port=0 --spring.cloud.stream.rabbit.bindings.input.consumer.exchangeType=topic --spring.cloud.stream.rabbit.bindings.input.consumer.quorum.enabled=true --spring.cloud.stream.bindings.input.destination=accounts.account.categories --spring.cloud.stream.rabbit.bindings.input.consumer.bindingRoutingKey="premium.#"
``` 

Start Another Consumer

```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-1.0.0.jar  --spring.application.name=event-log-standard --server.port=0 --spring.cloud.stream.rabbit.bindings.input.consumer.exchangeType=topic --spring.cloud.stream.rabbit.bindings.input.consumer.quorum.enabled=true --spring.cloud.stream.bindings.input.destination=accounts.account.categories --spring.cloud.stream.rabbit.bindings.input.consumer.bindingRoutingKey="standard.#"
```


## Start Publisher

Start Source

```shell
 java -jar applications/sources/event-account-http-source/target/event-account-http-source-1.0.0.jar  --spring.profiles.active=amqp --server.port=8095 --spring.cloud.stream.bindings.output.destination=accounts.account.categories
```



Testing

```shell
 curl -X 'POST' \
  'http://localhost:8095/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "PR01",
  "name": "premium residential account 1",
  "accountType": "premium.residential",
  "status": "OPEN",
  "notes": "Notes for account",
  "location": {
    "id": "premium-02-loc-02",
    "address": "123 Straight Street",
    "cityTown": "Springfield",
    "stateProvince": "IL",
    "zipPostalCode": "62701",
    "countryCode": "US"
  }
}'
```

Send Standard Account

```shell
 curl -X 'POST' \
  'http://localhost:8095/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "CR01",
  "name": "standard residential account 1",
  "accountType": "standard.residential",
  "status": "OPEN",
  "notes": "Notes for account",
  "location": {
    "id": "premium-02-loc-02",
    "address": "123 Straight Street",
    "cityTown": "Springfield",
    "stateProvince": "IL",
    "zipPostalCode": "62701",
    "countryCode": "US"
  }
}'
```


---------------------------
# 3 - Error Handling - Dead letter - Exchange


## Start Consumer


Start Postgres


```shell
podman run --rm --network=tanzu -it  --name postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_DB=postgres \
  -e POSTGRES_HOST_AUTH_METHOD=trust \
  -p 5432:5432 \
  postgres:15
```


Start Sink

```shell
java -jar applications/sinks/event-account-jdbc-sink/target/event-account-jdbc-sink-1.0.0.jar --db.schema=evt_showcase --spring.application.name=event-log-audit  --server.port=0 --spring.cloud.stream.rabbit.bindings.input.consumer.quorum.enabled=true --spring.cloud.stream.bindings.input.destination=accounts.account.categories  --spring.datasource.username=postgres  --spring.datasource.url=jdbc:postgresql://localhost:5432/postgres --spring.cloud.stream.rabbit.bindings.input.consumer.autoBindDlq=true --spring.cloud.stream.rabbit.bindings.input.consumer.dlqQuorum.enabled=true
``` 


## Start Source Http

```shell
java -jar applications/sources/http-amqp-source/target/http-amqp-source-1.0.0.jar --spring.application.name="http-amqp-source" --spring.cloud.stream.bindings.output.destination=accounts.account.categories --server.port=8098
```

Send Success Message

```shell
curl -X 'POST' \
  'http://localhost:8098/amqp/?exchange=accounts.account.categories&routingKey=01' \
  -H 'accept: application/hal+json' \
  -H 'rabbitContentType: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "G01",
  "name": "account 1",
  "accountType": "good",
  "status": "OPEN",
  "notes": "Notes for account",
  "location": {
    "id": "premium-02-loc-02",
    "address": "123 Straight Street",
    "cityTown": "Springfield",
    "stateProvince": "IL",
    "zipPostalCode": "62701",
    "countryCode": "US"
  }
}'
```


```shell
podman exec -it postgres psql -d postgres -U postgres
```

Select Results

```sql
select *  from evt_showcase.evt_accounts ;
select *  from evt_showcase.evt_locations ;

```

Does not match Account JSON structure

```shell
curl -X 'POST' \
  'http://localhost:8098/amqp/?exchange=accounts.account.categories&routingKey=accounts.error' \
  -H 'accept: application/hal+json' \
  -H 'rabbitContentType: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "idDELETE": "ER01",
  "name": "error account 1",
  "accountType": "error",
  "status": "OPEN",
  "notes": "Notes for account",
  "location": {
    "id": "premium-02-loc-02",
    "address": "123 Straight Street",
    "cityTown": "Springfield",
    "stateProvince": "IL",
    "zipPostalCode": "62701",
    "countryCode": "US"
  }
}'
```


See Message in Queue accounts.account.categories.event-log-audit.dlq


```shell
open http://localhost:15672/#/queues/%2F/accounts.account.categories.event-log-audit.dlq
```

----------------
# Review Source Code

## HTTP AMQP Source

See [HttpPublisherController.kt](../../../../../applications/sources/http-amqp-source/src/main/kotlin/showcase/streaming/event/rabbitmq/streaming/account/controller/HttpPublisherController.kt)

See project [http-amqp-source](../../../../../applications/sources/http-amqp-source)

## JBC Sink DLQ config

See [AccountConsumer.java](../../../../../applications/sinks/event-account-jdbc-sink/src/main/java/showcase/streaming/event/account/jdbc/sink/consumer/AccountConsumer.java)

See Also [RabbitConsumerConfig.java](../../../../../applications/sinks/event-account-jdbc-sink/src/main/java/showcase/event/stream/rabbitmq/jdbc/sink/RabbitConsumerConfig.java)

----------------

# Cleanup

Stop Publisher and Consumers

```shell
podman rm -f rabbitmq postgres
```