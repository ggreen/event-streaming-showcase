package com.example.jms.consumer;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JmsConsumer {

    @SneakyThrows
    @JmsListener(destination = "test")
    public void receiveMessage(String message) {
        log.info("Received from RabbitMQ via JMS: " + message);

        Thread.sleep(100);
        throw new RuntimeException("Retry");
    }
}