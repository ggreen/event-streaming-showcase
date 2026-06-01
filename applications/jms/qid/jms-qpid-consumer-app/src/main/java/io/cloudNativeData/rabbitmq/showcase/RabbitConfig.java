package io.cloudNativeData.rabbitmq.showcase;

import io.cloudNativeData.rabbitmq.showcase.consumer.AccountConsumer;
import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import showcase.streaming.event.account.domain.Account;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
public class RabbitConfig {

    @Value("${spring.rabbitmq.addresses:amqp://localhost:5672}")
    private String brokerUrl;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${app.queue.name}")
    private String queueName;

    @Value("${app.message.selector}")
    private String messageSelector;

    @Value("#{'${app.queue.selector-fields}'.split(',')}")
    private List<String> selectorFields;

    @Bean
    public Queue jmsQueue(AmqpAdmin amqpAdmin) {
        var queue = QueueBuilder.durable(queueName)
                .withArguments(
                        Map.of("x-queue-type", "jms",
                                "x-selector-fields",selectorFields)
                                )
                .build();

        amqpAdmin.declareQueue(queue);
        return queue;
    }

    @Bean
    Connection connection() throws NamingException, JMSException {
        var context = new InitialContext();
        var factory = (ConnectionFactory) context.lookup("myFactoryLookup");
        return factory.createConnection(username, password);
    }

    @Bean
    Session session(Connection connection, Queue amqQueue, AccountConsumer accountConsumer, Converter<String, Account> converter) throws NamingException, JMSException {

        // Create a session (false = not transacted, AUTO_ACKNOWLEDGE)
        var session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Define a Queue destination
        var queue = session.createQueue("/queues/"+amqQueue.getActualName());

        log.info("Using message selector: {}", messageSelector);

        var consumer = session.createConsumer(queue,messageSelector);

        consumer.setMessageListener(message -> {
            if(message instanceof TextMessage txtMessage) {
                try {
                    accountConsumer.accept(converter.convert(txtMessage.getText()));
                } catch (JMSException | RuntimeException e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        });

        connection.start();
        return session;
    }
}
