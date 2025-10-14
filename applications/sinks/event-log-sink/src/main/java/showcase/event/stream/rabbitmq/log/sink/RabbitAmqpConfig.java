package showcase.event.stream.rabbitmq.log.sink;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import showcase.event.stream.rabbitmq.log.sink.functions.LoggingConsumer;

@Configuration
@Slf4j
@Profile("amq")
public class RabbitAmqpConfig {
    public RabbitAmqpConfig()
    {
        log.info("Configuring RabbitMQ AMQP");
    }

    @Bean("loggingConsumer")
    LoggingConsumer loggingConsumer()
    {
        return new LoggingConsumer();//TODO: not sure why this is being create automatically
    }
}
