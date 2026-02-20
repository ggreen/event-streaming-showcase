package com.example.jms;

import com.rabbitmq.client.ConnectionFactory;
import jakarta.jms.JMSException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {


    @Bean
    Queue rabbitRequestQueue(org.springframework.amqp.rabbit.connection.ConnectionFactory cf) throws JMSException {


        var queue = QueueBuilder.durable("requestQueue")
                .quorum()
                .build();

        var admin =  new RabbitAdmin(cf);
        admin.declareQueue(
                queue);
        return queue;
    }

    @Bean
    Queue rabbitResponseQueue(org.springframework.amqp.rabbit.connection.ConnectionFactory cf) throws JMSException {


        var queue = QueueBuilder.durable("responseQueue")
                .quorum()
                .build();

        var admin =  new RabbitAdmin(cf);
        admin.declareQueue(
                queue);
        return queue;
    }
}
