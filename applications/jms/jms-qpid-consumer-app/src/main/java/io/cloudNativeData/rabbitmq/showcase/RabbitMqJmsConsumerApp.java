package io.cloudNativeData.rabbitmq.showcase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RabbitMqJmsConsumerApp {
    public static void main(String[] args) {
        SpringApplication.run(RabbitMqJmsConsumerApp.class, args);
    }
}
