# LAB 04 - Rabbit Advance Filtering


**Prerequisite**


Create the podman network (if not existing)
```shell
podman network create tanzu
```

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



---------------------------
#  RabbitMQ Stream Filter



Create Consumer NY 

```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-1.0.0.jar --spring.application.name=event-log-sink-ny --spring.rabbitmq.host=localhost --spring.rabbitmq.stream.host=localhost --spring_rabbitmq_username=guest --spring.rabbitmq.password=guest --spring.profiles.active="stream" --spring.cloud.stream.bindings.input.destination="accounts.account.state" --rabbitmq.streaming.offset="last" --rabbitmq.streaming.filter.values="NY"
```


Create Consumer NJ

```shell

java -jar applications/sinks/event-log-sink/target/event-log-sink-1.0.0.jar --spring.application.name=event-log-sink-nj --spring.rabbitmq.host=localhost --spring.rabbitmq.stream.host=localhost --spring_rabbitmq_username=guest --spring.rabbitmq.password=guest --spring.profiles.active="stream" --spring.cloud.stream.bindings.input.destination="accounts.account.state" --rabbitmq.streaming.offset="last" --rabbitmq.streaming.filter.values="NJ"

```



Create Filtering Source

```shell
java -jar applications/sources/event-account-http-source/target/event-account-http-source-1.0.0.jar --spring.rabbitmq.host=localhost --spring.rabbitmq.stream.host=localhost --server.port="8080" --spring_rabbitmq_username=guest --spring.rabbitmq.password=guest --spring.cloud.stream.bindings.output.destination="accounts.account.state" --rabbitmq.streaming.partitions="2" --spring.profiles.active="stream" --rabbitmq.streaming.use.filter="true"
```

Testings




Open Source App

Example
```shell
open http://localhost:8080/
```

Test NY
```json
{
  "id": "NY1",
  "name": "Event NY Filtering",
  "accountType": "test",
  "status": "IN-PROGRESS",
  "notes": "Testing 222",
  "location": {
    "id": "002.002",
    "address": "2 Straight Street",
    "cityTown": "JamesTown",
    "stateProvince": "NY",
    "zipPostalCode": "45555",
    "countryCode": "US"
  }
}
```

```shell
curl -X 'POST' \
  'http://localhost:8080/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "NY1",
  "name": "Event NY Filtering",
  "accountType": "test",
  "status": "IN-PROGRESS",
  "notes": "Testing 222",
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


Test NJ

```json
{
  "id": "001",
  "name": "Event Demo 1",
  "accountType": "test",
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


```shell
curl -X 'POST' \
  'http://localhost:8080/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "001",
  "name": "Event Demo 1",
  "accountType": "test",
  "status": "IN-PROGRESS",
  "notes": "Testing 123",
  "location": {
    "id": "001.001",
    "address": "1 Straight Street",
    "cityTown": "Wayne",
    "stateProvince": "NJ",
    "zipPostalCode": "55555",
    "countryCode": "US"
  }
}'
```

---------------------------
# 5 - Cleanup

Stop Apps and RabbitMQ