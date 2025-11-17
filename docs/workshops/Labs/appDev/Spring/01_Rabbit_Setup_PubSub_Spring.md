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
deployment/local/containers/rabbit.sh 
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

```shell
open http://localhost:15672
```

User/password: guest/guest

- Click Overview
- Click Connections
- Click Queues and Streams


# 3 - Run Publisher

Open a new terminal

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

Check if the message was received by the consumer.

Review  Management Console 

- Click Overview
- Click Connections
- Click Queues and Streams


# Review Source Code

- Consumer Application

[event-log-sink](../../../../../applications/sinks/event-log-sink)

See Core Consumer [LoggingConsumer.java](../../../../../applications/sinks/event-log-sink/src/main/java/showcase/event/stream/rabbitmq/log/sink/functions/LoggingConsumer.java)

```java
@Component("loggingConsumer")
public class LoggingConsumer implements Consumer<String> {
    public LoggingConsumer()
    {
        log.info("Created LoggingConsumer");
    }

    @Override
    public void accept(String text) {
        log.info("CONSUMED: {} ",text);
    }
}
```

- Publisher Application

[event-account-http-source](../../../../../applications/sources/event-account-http-source)

See consumer [AccountController.java](../../../../../applications/sources/event-account-http-source/src/main/java/showcase/event/stream/rabbitmq/account/http/source/controller/AccountController.java)

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("accounts")
@Slf4j
public class AccountController {
    private final MessageChannel publisher;

    @PostMapping
    public void publish(@RequestBody Account account) {
        log.info("Publishing Account: {}",account);
        publisher.send(MessageBuilder.withPayload(account)
                .setHeader(ROUTING_KEY,account.getAccountType())
                        .setHeader(MessageHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build());
    }
}
```

--------------------------------
# Clean Up 

Stop all applications using CTRL-C

Stop RabbitMQ Broker

```shell
podman rm -f rabbitmq
```