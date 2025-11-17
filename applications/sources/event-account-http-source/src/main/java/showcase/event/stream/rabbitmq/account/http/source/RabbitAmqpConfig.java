package showcase.event.stream.rabbitmq.account.http.source;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionNameStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.MessageChannel;
import showcase.streaming.event.account.domain.Account;

import static showcase.event.stream.rabbitmq.account.http.source.properties.AccountSourceConstants.ROUTING_KEY;

@Configuration
@Slf4j
@Profile("amqp")
public class RabbitAmqpConfig {

    @Value("${spring.cloud.stream.bindings.output.destination}")
    private String outputExchange;

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    ConnectionNameStrategy connectionNameStrategy() {
        return (connectionFactory) -> applicationName;
    }


    @Bean
    @ConditionalOnProperty(name = "amqp.exchange.direct", havingValue="true")
    public Exchange exchangeDirect() {
        log.info("Creating direct exchange: {}",outputExchange);
        return ExchangeBuilder
                .directExchange(outputExchange)
                .build();
    }

    @Bean
    @ConditionalOnProperty
            (name = "amqp.exchange.direct", havingValue = "false", matchIfMissing = true)
    public Exchange exchangeDTopic() {
        log.info("Creating direct topic: {}",outputExchange);
        return ExchangeBuilder
                .topicExchange(outputExchange)
                .build();
    }



    @Bean
    MessageChannel publisher(AmqpTemplate amqpTemplate)
    {
        return (msg, timeout) ->{
            var account = (Account)msg.getPayload();
            amqpTemplate.convertAndSend(outputExchange,
                    msg.getHeaders()
                    .get(ROUTING_KEY, String.class),account);
            return true;
        };
    }
}
