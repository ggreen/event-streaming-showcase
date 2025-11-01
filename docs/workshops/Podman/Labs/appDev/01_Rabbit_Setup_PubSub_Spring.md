# LAB 01 - Rabbit setup w Pub/Sub Apps


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



# 1 - Install RabbitMQ Broker

- Run RabbitMQ
```shell
podman run --name rabbitmq01  --network tanzu --rm -e RABBITMQ_MANAGEMENT_ALLOW_WEB_ACCESS=true -p 5672:5672 -p 5552:5552 -p 15672:15672  -p  1883:1883  rabbitmq:4.2-management 
```


- Open Management Console with credentials *guest/guest*

```shell
open http://localhost:15672
```
# 2 - Run Consumer

```shell
java -jar applications/sinks/event-log-sink/target/event-log-sink-1.0.0.jar   --spring.rabbitmq.host=localhost --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --spring.cloud.stream.bindings.input.group=event-log-sink --spring.profiles.active=ampq --spring.cloud.stream.bindings.input.destination=accounts.account  
```

Review  Management Console

- Click Overview
- Click Connections
- Click Queues and Streams


# 3 - Run Publisher

In new terminal
```shell
 java -jar applications/sources/event-account-http-source/target/event-account-http-source-1.0.0.jar --spring.profiles.active=amqp --spring.rabbitmq.host=localhost --spring.rabbitmq.username=guest --spring.rabbitmq.password=guest --server.port=8095 --spring.cloud.stream.bindings.output.destination=accounts.account 
```

Open Swagger UI

```shell
open http://localhost:8095/swagger-ui/index.html
```


Post JSON using Swagger

```json
{
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
}
```



or Use Curl

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

Check if Receive app consumed message 

Review  Management Console 

- Click Overview
- Click Connections
- Click Queues and Streams


Stop all applications