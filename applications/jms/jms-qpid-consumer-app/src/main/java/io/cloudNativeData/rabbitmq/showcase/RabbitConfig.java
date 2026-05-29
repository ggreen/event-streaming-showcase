package io.cloudNativeData.rabbitmq.showcase;

import io.cloudNativeData.rabbitmq.showcase.consumer.AccountConsumer;
import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;import org.apache.qpid.jms.JmsConnectionFactory;import org.springframework.beans.factory.annotation.Value;import org.springframework.boot.ApplicationRunner;import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import showcase.streaming.event.account.domain.Account;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@Configuration
@Slf4j
public class RabbitConfig {


    @Value("${app.broker.url:amqp://localhost:5672}")
    private String brokerUrl;

    @Value("${app.broker.username}")
    private String username;

    @Value("${app.broker.password}")
    private String password;

    @Value("${app.queue.name:/queues/accounts}")
    private String queueName;


    @Bean
    Connection connection() throws NamingException, JMSException {
        var context = new InitialContext();
        var factory = (ConnectionFactory) context.lookup("myFactoryLookup");
        return factory.createConnection(username, password);
    }

    @Bean
    Session session(Connection connection, AccountConsumer accountConsumer, Converter<String, Account> converter) throws NamingException, JMSException {

        // Create a session (false = not transacted, AUTO_ACKNOWLEDGE)
        var session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Define a Queue destination
        var queue = session.createQueue(queueName);

        MessageConsumer consumer = session.createConsumer(queue);

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
