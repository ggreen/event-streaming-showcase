# LAB 03 - 


**Prerequisite**

- Dotnet SDK 7.0.401 or higher
- Docker version 4.29 or higher

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

# 1 - RabbitMQ Streaming 

## Start Consumer

```shell
java -jar applications/sinks/rabbit-consumer-cli/target/rabbit-consumer-cli-1.0.0.jar  --clientName=ReceiveStream1 --queue=app.receive.stream --queueType=stream --autoAck=false
```


## Start Publisher

Publish

```shell
java -jar applications/sources/rabbit-publisher-cli/target/rabbit-publisher-cli-1.0.0.jar  --routingKey=app.receive.stream --message="Testing app.receive STREAMING DATA 1"
```

Send another message

```shell
java -jar applications/sources/rabbit-publisher-cli/target/rabbit-publisher-cli-1.0.0.jar  --routingKey=app.receive.stream --message="Testing app.receive STREAMING DATA 2"
```

## Review  Management Console (guest/guest)

```shell
open http://localhost:15672
```

Review  Management Console

- Click Overview
- Click Connections
- Click Queues and Streams


Stop Consumer

---------------------------
# 2 - Stream Offsets


## Start Consumer

Replay all messages
```shell
java -jar applications/sinks/rabbit-consumer-cli/target/rabbit-consumer-cli-1.0.0.jar   --clientName=ReceiveStream1 --queue=app.receive.stream --queueType=stream --streamOffset=first --autoAck=false 
```

Hit Enter/Control C

Reading last chunk
```shell
java -jar applications/sinks/rabbit-consumer-cli/target/rabbit-consumer-cli-1.0.0.jar   --clientName=ReceiveStream1 --queue=app.receive.stream --queueType=stream --streamOffset=last --autoAck=false
```
Hit Enter/Control C

Reading next message

```shell
java -jar applications/sinks/rabbit-consumer-cli/target/rabbit-consumer-cli-1.0.0.jar   --clientName=ReceiveStream1 --queue=app.receive.stream --autoAck=false --queueType=stream --streamOffset=next 
```

Send another message

```shell
java -jar applications/sources/rabbit-publisher-cli/target/rabbit-publisher-cli-1.0.0.jar --routingKey=app.receive.stream --message="NEXT MESSAGE"
```


Stop Customer

# Shutdown/Cleanup

Stop applications


---------------------------
# 3 - Spring Filter Single Active Consumer


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


Example CLI

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
    "address": "1 Straight Stree",
    "cityTown": "Wayne",
    "stateProvince": "NJ",
    "zipPostalCode": "55555",
    "countryCode": "US"
  }
}'
```

Review Logs for each


Note message are routed by account id application get the logs events

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
  "accountType": "test",
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
  "accountType": "test",
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



Example CLI

```shell
curl -X 'POST' \
  'http://localhost:8080/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "003",
  "name": "Event Demo 3",
  "accountType": "test",
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

Stop Applications




---------------------------
# 4 - RabbitMQ Stream Filter

enable filtering

```shell
kubectl exec  rabbitmq-server-0 -- rabbitmqctl enable_feature_flag stream_filtering
```


Create Consumer NY 

```shell
kubectl apply -f https://raw.githubusercontent.com/Tanzu-Solutions-Engineering/event-streaming-showcase/main/deployment/cloud/k8/apps/event-log-sink/event-log-sink-filter-NY.yml
```


Create Consumer NJ

```shell
kubectl apply -f https://raw.githubusercontent.com/Tanzu-Solutions-Engineering/event-streaming-showcase/main/deployment/cloud/k8/apps/event-log-sink/event-log-sink-filter-NJ.yml
```


Create Filtering Source

```shell
kubectl apply -f https://raw.githubusercontent.com/Tanzu-Solutions-Engineering/event-streaming-showcase/main/deployment/cloud/k8/apps/event-account-http-source/event-account-http-source-filter.yml
```

Testings


View NJ filter accounts

```shell
kubectl logs deployment/event-log-sink-nj -f
```

Watch for "Started" message

View NY filter accounts (new terminal)

```shell
kubectl logs deployment/event-log-sink-ny -f
```


Open Source App

Example
```shell
open http://localhost:8090/
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


---------------------------
# 5 - Cleanup

Delete Apps
```shell
kubectl delete -f https://raw.githubusercontent.com/Tanzu-Solutions-Engineering/event-streaming-showcase/main/deployment/cloud/k8/apps/event-log-sink/event-log-sink.yml
kubectl delete -f https://raw.githubusercontent.com/Tanzu-Solutions-Engineering/event-streaming-showcase/main/deployment/cloud/k8/apps/event-account-http-source/event-account-http-source.yml
kubectl delete -f https://raw.githubusercontent.com/Tanzu-Solutions-Engineering/event-streaming-showcase/main/deployment/cloud/k8/apps/event-log-sink/event-log-sink-filter-NY.yml
kubectl delete -f https://raw.githubusercontent.com/Tanzu-Solutions-Engineering/event-streaming-showcase/main/deployment/cloud/k8/apps/event-log-sink/event-log-sink-filter-NJ.yml
kubectl delete -f https://raw.githubusercontent.com/Tanzu-Solutions-Engineering/event-streaming-showcase/main/deployment/cloud/k8/apps/event-account-http-source/event-account-http-source-filter.yml
```

Delete RabbitMQ

```shell
kubectl delete -f https://raw.githubusercontent.com/Tanzu-Solutions-Engineering/event-streaming-showcase/main/deployment/cloud/k8/data-services/rabbitmq/rabbitmq-1-node.yml
```
