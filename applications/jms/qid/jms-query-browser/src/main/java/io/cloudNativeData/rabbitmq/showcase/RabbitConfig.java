package io.cloudNativeData.rabbitmq.showcase;

import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import showcase.streaming.event.account.domain.Account;

import javax.naming.InitialContext;
import javax.naming.NamingException;

@Configuration
@Slf4j
public class RabbitConfig {


    @Value("${spring.rabbitmq.addresses:amqp://localhost:5672}")
    private String brokerUrl;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${app.queue.name:accounts}")
    private String queueName;

    @Value("${app.message.selector}")
    private String messageSelector;


    @Bean
    Connection connection() throws NamingException, JMSException {
        var context = new InitialContext();
        var factory = (ConnectionFactory) context.lookup("myFactoryLookup");
        return factory.createConnection(username, password);
    }

    @Bean
    Session session(Connection connection) throws NamingException, JMSException {

        // Create a session (false = not transacted, AUTO_ACKNOWLEDGE)
        var session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        connection.start();
        return session;
    }

    @Bean
    Queue queue( Session session) throws JMSException {

        return  session.createQueue("/queues/"+queueName);
    }
}
