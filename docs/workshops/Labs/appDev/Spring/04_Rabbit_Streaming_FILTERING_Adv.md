# LAB 04 - Rabbit Advance Filtering


**Prerequisite**

Example with git
```shell
git clone https://github.com/ggreen/event-streaming-showcase.git
cd event-streaming-showcase
```


- Run RabbitMQ 

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



Create Consumer for NY 

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

Using Curl
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

# SQL Filtering


Http SQL Filtering Source

```shell
java -jar applications/sources/event-account-http-source/target/event-account-http-source-1.0.0.jar --spring.rabbitmq.host=localhost --spring.rabbitmq.stream.host=localhost --server.port="8087" --spring_rabbitmq_username=guest --spring.rabbitmq.password=guest --spring.cloud.stream.bindings.output.destination="accounts.account.sql" --spring.profiles.active="stream" --rabbitmq.streaming.use.filter="true"
```

Start SQL Processor

```shell
java -jar applications/processors/stream-sql-filter-processor/target/stream-sql-filter-processor-1.0.0.jar --spring.application.name=stream-sql-filter-processo --stream.filter.sql="properties.message_id LIKE 'W%' AND stateProvince IN('CA','NY','NJ')" --spring.profiles.active=outputStream --spring.cloud.stream.bindings.input.destination="accounts.account.sql" --spring.cloud.stream.bindings.output.destination="accounts.account.state" --server.port=0
```



Not Filtered

```shell
curl -X 'POST' \
  'http://localhost:8087/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "W001",
  "name": "Event Demo from SQL Filter",
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


Will be Filtered

```shell
curl -X 'POST' \
  'http://localhost:8087/accounts' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "id": "F001",
  "name": "Will not be delivered",
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
# Review Code


## Bloom Value/Filters

Bloom Filtering Processor in [ConsumerConfig.java](../../../../../applications/processors/stream-bloom-filter-processor/src/main/java/showcase/event/stream/bloom/ConsumerConfig.java)

Filtering Snipplet 
```java
builder.filter().values(filterValues)
                            .postFilter(msg ->
                                    {
                                        if(msg.getApplicationProperties() == null)
                                            return false;

                                        var mustFilter = Arrays.asList(filterValues)
                                                .contains(valueOf(msg.getApplicationProperties().get(FILTER_PROP_NM)));
                                        log.info("Must filter: {} for values: {}",mustFilter,Debugger.toString(filterValues));
                                        return mustFilter;
                                    }
                            );
```


## SQL Filtering

SQL Filtering Processor in [ConsumerConfig.java](../../../../../applications/processors/stream-sql-filter-processor/src/main/java/showcase/event/stream/sql/ConsumerConfig.java)

Example

```java
Consumer consumer(@Qualifier("inputConnection") Connection connection, Publisher publisher,
                      @Qualifier("input") Management.QueueInfo input){

        log.info("input consumed with SQL '{}' from input stream {}",sqlFilter,input.name());

        return connection.consumerBuilder()
                .queue(input.name())
                .stream()
                .offset(ConsumerBuilder.StreamOffsetSpecification.valueOf(offsetName))
                .filter()
                .sql(sqlFilter)
                .stream()
                .builder().messageHandler((ctx,inputMessage) -> {
                    //Processing input message
                    log.info("Processing input: {}, msg id: {}", inputQueue, inputMessage.messageId());

                    var outputMap = new HashMap<String,String>();
                    inputMessage.forEachProperty((name,value)-> {
                        var valueText = Text.toString(value);
                        log.info("Setting property: {}, value: {}",name,valueText);
                        outputMap.put(name, valueText);
                    });

                    if(outputMap.isEmpty())
                    {
                        log.warn("No properties provided to filter Nothing to filter");
                        return; //exit
                    }

                    Map.Entry<String,String> entry =  outputMap.entrySet().iterator().next();
                    log.info("entry: {}",entry);

                    var outputMsg = publisher.message(inputMessage.body())
                            .annotation("x-stream-filter-value", entry.getValue())
                            .subject(entry.getValue())
                                    .property(entry.getKey(),entry.getValue());

                    //Publish output
                    publisher.publish(outputMsg, outCtx ->{

                        if(outCtx.status() == Publisher.Status.ACCEPTED)
                        {
                            //confirm message
                            ctx.accept();
                            log.warn("Message Sent");
                        }
                        else
                            log.warn("Status {} != ACCEPTED",outCtx.status()); // do not accept message
                    });


                })
                .build();
    }
```

See project [stream-sql-filter-processor](../../../../../applications/processors/stream-sql-filter-processor)

---------------------------
#  Cleanup

Stop Apps and RabbitMQ