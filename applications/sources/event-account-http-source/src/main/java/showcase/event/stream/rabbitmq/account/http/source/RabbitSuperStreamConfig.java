/*
 *
 *  * Copyright 2023 VMware, Inc.
 *  * SPDX-License-Identifier: GPL-3.0
 *
 */

package showcase.event.stream.rabbitmq.account.http.source;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.stream.Environment;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.rabbit.stream.config.SuperStream;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;
import showcase.streaming.event.account.domain.Account;

import java.util.concurrent.ExecutionException;

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
    RabbitStreamTemplate streamTemplate(Environment env, MessageConverter converter, ObjectMapper objectMapper) {
        var template = new RabbitStreamTemplate(env, superStreamName);
        template.setMessageConverter(converter);

        template.setSuperStreamRouting(message -> {
            Data data = (Data) message.getBody();
            var json = data.getValue().toString();
            try {
                var account = objectMapper.readValue(json,Account.class);

                return account.getId();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        return template;
    }

    @Bean
    MessageChannel publisher(Environment environment, RabbitStreamTemplate streamTemplate) {
        return (msg, timeout) -> {
            var reply = streamTemplate.convertAndSend(msg.getPayload());

            try {
                log.info("SENT: {} to stream: {}: msg: {}",reply.get(),streamTemplate.getStreamName(),msg);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            return true;
        };
    }
}