package com.example.jms;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.admin.RMQDestination;
import jakarta.jms.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

import java.util.UUID;

@Configuration
@EnableJms
@Slf4j
public class JmsConfig {

    @Value("${jms.topic:test}")
    private String topic;

    @Value("${jms.host:localhost}")
    private String host;

    @Value("${jms.username:guest}")
    private String username;

    @Value("${jms.password:guest}")
    private String password;

    @Value("${jms.vhost:jms}")
    private String vhost;


    @Bean
    public ConnectionFactory jmsConnectionFactory() {
        RMQConnectionFactory factory = new RMQConnectionFactory();
        factory.setHost(host);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(vhost);
        factory.setDeclareReplyToDestination(false);
        return factory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);

        factory.setSessionTransacted(true);

        // Ensure the container is started automatically
        factory.setAutoStartup(true);

        // For Streams, usually 1 consumer is preferred to maintain order
        factory.setConcurrency("1-1");

        return factory;
    }

    @Bean
    Connection connection(ConnectionFactory connectionFactory) throws JMSException {
        return connectionFactory.createConnection();
    }


    @Bean
    Session session(Connection connection) throws JMSException {
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    @Bean
    Queue requestQueue() throws JMSException {
        RMQDestination reqeustQueue = new RMQDestination();
        reqeustQueue.setDestinationName("requestQueue"); // The AMQP Queue name
        reqeustQueue.setAmqpQueueName("requestQueue");
        reqeustQueue.setAmqpExchangeName(""); // Empty string for default exchange
        reqeustQueue.setAmqpRoutingKey("requestQueue");

        return reqeustQueue;
    }

    @Bean
    Queue responseQueue() throws JMSException {
        RMQDestination reqeustQueue = new RMQDestination();
        reqeustQueue.setDestinationName("responseQueue"); // The AMQP Queue name
        reqeustQueue.setAmqpQueueName("responseQueue");
        reqeustQueue.setAmqpExchangeName(""); // Empty string for default exchange
        reqeustQueue.setAmqpRoutingKey("responseQueue");

        return reqeustQueue;
    }

    @Bean
    ApplicationRunner sendWithCorrelationId(Session session,
                                            @Qualifier("requestQueue") Queue requestQueue,
                                            @Qualifier("responseQueue") Queue responseQueue)
    {
        return args -> {
            MessageProducer producer = session.createProducer(requestQueue);

            // 1. Create message and set Correlation ID
            TextMessage requestMsg = session.createTextMessage("Hello, I need a response!");
            String correlationId = UUID.randomUUID().toString();
            requestMsg.setJMSCorrelationID(correlationId);
            requestMsg.setJMSReplyTo(responseQueue);

            log.info("Sending request: {}", correlationId);

            producer.send(requestMsg);
        };
    }


//    @Bean
    ApplicationRunner basicSender(JmsTemplate jmsTemplate)
    {
        return args -> {
            log.info("SENDING!!!!");
          jmsTemplate.convertAndSend(topic,"Hello World");

            log.info("SENT!!!!");
        };
    }

}
