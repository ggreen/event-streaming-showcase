package showcase.event.stream.rabbitmq.log.sink;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Slf4j
@Profile("superStream")
public class RabbitSuperStreamConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${rabbitmq.streaming.offset:last}")
    private String offset;


    @Value("${spring.cloud.stream.bindings.input.destination:event-log-sink}")
    private String superStreamName;

    @Value("${rabbitmq.streaming.partitions:2}")
    private int partitions;

    @Value("${rabbitmq.streaming.concurrency:1}")
    private int concurrency;

    @Value("${spring.cloud.stream.rabbit.bindings.input.consumer.singleActiveConsumer:false}")
    private boolean singleActiveConsumer;


}