package com.example.jms;


import jakarta.jms.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResponseJmsCorrectionIdConfig {


    private String correlationId = "";

    @Bean
    MessageConsumer messageConsumer(Session session, Destination responseQueue) throws JMSException
    {
        String selector = "JMSCorrelationID = '" + correlationId + "'";
        return session.createConsumer(responseQueue, selector);
    }
    @Bean
    ApplicationRunner applicationRunner(MessageConsumer consumer) throws Exception
    {
        return args -> {

            // 3. Sync receive with a timeout (e.g., 5 seconds)
            Message response = consumer.receive(5000);
            if (response instanceof TextMessage) {
                System.out.println("Received: " + ((TextMessage) response).getText());
            } else {
                System.out.println("Response timed out or was null.");
            }

        };
    }
}
