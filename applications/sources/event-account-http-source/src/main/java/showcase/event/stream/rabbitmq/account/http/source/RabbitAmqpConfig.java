package showcase.event.stream.rabbitmq.account.http.source;

import lombok.extern.slf4j.Slf4j;
import nyla.solutions.core.patterns.conversion.Converter;
import nyla.solutions.core.patterns.integration.Publisher;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionNameStrategy;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import showcase.streaming.event.account.domain.Account;

@Configuration
@Slf4j
@Profile("amqp")
public class RabbitAmqpConfig {

    @Value("${spring.cloud.stream.bindings.output.destination}")
    private String topicExchange;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.rabbitmq.routing.key:}")
    private String routingKey;

    @Bean
    ConnectionNameStrategy connectionNameStrategy() {
        return (connectionFactory) -> applicationName;
    }

    @Bean
    public Exchange queue() {
        return ExchangeBuilder
                .topicExchange(topicExchange)
                .build();
    }

    @Bean
    Publisher<Account> publisher(AmqpTemplate amqpTemplate, Converter<Account, byte[]> converter)
    {
        return account ->{
            amqpTemplate.convertAndSend(topicExchange,routingKey,account);
        };
    }

    @Bean
    MessageConverter convert(){
        return new Jackson2JsonMessageConverter();
    }

}
