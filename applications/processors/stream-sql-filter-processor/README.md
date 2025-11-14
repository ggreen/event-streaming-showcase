

```text
--stream.filter.sql="stateProvince IN('CA','GA') OR accountType IN('wealth')"
--spring.cloud.stream.bindings.input.destination=sqlInput
--spring.cloud.stream.bindings.output.destination=sqlOutput
```


```shell
java -jar applications/sources/event-account-http-source/target/event-account-http-source-1.0.0.jar --spring.rabbitmq.host=localhost --spring.rabbitmq.stream.host=localhost --server.port="8080" --spring_rabbitmq_username=guest --spring.rabbitmq.password=guest --spring.cloud.stream.bindings.output.destination="sqlInput" --spring.profiles.active="stream" --rabbitmq.streaming.use.filter="true"

```