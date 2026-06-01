package io.cloudNativeData.rabbitmq.showcase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RabbitMqProducerJmsApp {
    public static void main(String[] args) {
        SpringApplication.run(RabbitMqProducerJmsApp.class, args);
    }
}
