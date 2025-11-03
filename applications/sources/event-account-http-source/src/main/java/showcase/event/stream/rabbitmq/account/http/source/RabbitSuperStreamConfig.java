/*
 *
 *  * Copyright 2023 VMware, Inc.
 *  * SPDX-License-Identifier: GPL-3.0
 *
 */

package showcase.event.stream.rabbitmq.account.http.source;

import com.rabbitmq.stream.Environment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.rabbit.stream.config.SuperStream;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;

import java.util.Objects;

@Configuration
@Slf4j
@Profile("superStream")
public class RabbitSuperStreamConfig {

    @Value("${spring.application.name}")
    private String applicationName;


    @Value("${spring.cloud.stream.bindings.output.destination}")
    private String superStreamName;

    @Value("${rabbitmq.streaming.partitions:2}")
    private int partitions;

    private static final String routingKeyName = "id";


    @Bean
    SuperStream superStream(Environment environment) {

        log.info("Creating super stream: {}", superStreamName);

        environment.streamCreator().name(superStreamName)
                .superStream()
                .partitions(partitions).creator()
                .create();

        return new SuperStream(superStreamName, partitions);
    }

    @Bean
    RabbitStreamTemplate streamTemplate(Environment env, MessageConverter converter) {
        RabbitStreamTemplate template = new RabbitStreamTemplate(env, superStreamName);
        template.setMessageConverter(converter);
        template.setSuperStreamRouting(message -> {
            var routingKey = ((Message<?>)message.getBody()).getHeaders().get("ROUTING_KEY", String.class);

            log.info("ROUTING KEY: {}",routingKey);
            return Objects.requireNonNull(routingKey);
        });
        return template;
    }

    @Bean
    MessageChannel publisher(Environment environment, RabbitStreamTemplate streamTemplate) {
        return (msg, timeout) -> {
            streamTemplate.convertAndSend(msg);
            log.info("SENT to stream: {}: msg: {}",streamTemplate.getStreamName(),msg);
            return true;
        };
    }
}