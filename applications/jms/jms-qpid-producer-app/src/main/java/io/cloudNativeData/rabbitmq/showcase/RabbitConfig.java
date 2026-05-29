package io.cloudNativeData.rabbitmq.showcase;

import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.jms.support.SimpleJmsHeaderMapper;
import org.springframework.messaging.MessageChannel;
import showcase.streaming.event.account.domain.Account;

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

    @Value("${app.delivery.delay.ms:0}")
    private long deliverDelayMs;


    @Bean
    Connection connection() throws NamingException, JMSException {
        var context = new InitialContext();
        var factory = (ConnectionFactory) context.lookup("myFactoryLookup");
        return factory.createConnection(username, password);
    }

    @Bean
    Session session(Connection connection) throws NamingException, JMSException {

        // Create a session (false = not transacted, AUTO_ACKNOWLEDGE)
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    @Bean
    MessageProducer messageProducer(Session session) throws JMSException {
        // Define a Queue destination
        var queue = session.createQueue(queueName);
        var producer = session.createProducer(queue);

        if(deliverDelayMs > 0)
        {
            log.info("Setting deliver delay to {} ms", deliverDelayMs);
            producer.setDeliveryDelay(deliverDelayMs);
        }
        return producer;
    }

    @Bean
    MessageChannel channel(Session session, MessageProducer messageProducer, Converter<Account,String> converter) throws JMSException {

        return (springMessage, timeout) -> {

            log.info("Sending spring message: {} as JMS Message: ",springMessage);
            var account = (Account)springMessage.getPayload();

            TextMessage jmsTextMessage = null;
            try {
                jmsTextMessage = session.createTextMessage(converter.convert(account));

                var mapper = new SimpleJmsHeaderMapper();
                mapper.fromHeaders(springMessage.getHeaders(), jmsTextMessage);

                messageProducer.send(jmsTextMessage);
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }

            return true;
        };
    }
}
