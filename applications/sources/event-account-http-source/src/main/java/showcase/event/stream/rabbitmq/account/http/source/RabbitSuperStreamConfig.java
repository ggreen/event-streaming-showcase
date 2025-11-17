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
import org.springframework.core.convert.converter.Converter;
import org.springframework.messaging.MessageChannel;
import org.springframework.rabbit.stream.config.SuperStream;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;
import showcase.streaming.event.account.domain.Account;

import java.util.concurrent.ExecutionException;

import static showcase.event.stream.rabbitmq.account.http.source.properties.AccountSourceConstants.ROUTING_KEY;

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
    MessageChannel publisher(RabbitStreamTemplate streamTemplate, Converter<Account,byte[]> converter) {
        return (msg, timeout) -> {
            try {
                var routingKey = msg.getHeaders().get(ROUTING_KEY,String.class);
                var streamMsg = streamTemplate.messageBuilder()
                        .addData(converter.convert((Account) msg.getPayload()))
                        .applicationProperties().entry(ROUTING_KEY, routingKey)
                        .messageBuilder()
                        .build();

                var reply = streamTemplate.send(streamMsg).get();
                log.info("SENT: {} to stream: {}: msg with key: {}",reply,streamTemplate.getStreamName(),routingKey);
                return reply;
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Bean
    RabbitStreamTemplate streamTemplate(Environment env, MessageConverter converter) {
        var template = new RabbitStreamTemplate(env, superStreamName);
        template.setMessageConverter(converter);

        template.setSuperStreamRouting(message -> {
            var routingKey = message.getApplicationProperties().get(ROUTING_KEY).toString();
            log.info("Routing to partition based on key: {}",routingKey);
            return routingKey;
        });
        return template;
    }


}