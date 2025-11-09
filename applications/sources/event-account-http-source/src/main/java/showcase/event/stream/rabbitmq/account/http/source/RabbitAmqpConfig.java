package showcase.event.stream.rabbitmq.account.http.source;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionNameStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.MessageChannel;
import showcase.streaming.event.account.domain.Account;

@Configuration
@Slf4j
@Profile("amqp")
public class RabbitAmqpConfig {

    @Value("${spring.cloud.stream.bindings.output.destination}")
    private String topicExchange;

    @Value("${spring.application.name}")
    private String applicationName;

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
    MessageChannel publisher(AmqpTemplate amqpTemplate)
    {
        return (msg, timeout) ->{
            var account = (Account)msg.getPayload();
            amqpTemplate.convertAndSend(topicExchange,account.getId(),account);
            return true;
        };
    }

//    @Bean
//    MessageConverter convert(){
//        return new JacksonJsonMessageConverter();
//    }

}
