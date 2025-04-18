/*
 *
 *  * Copyright 2023 VMware, Inc.
 *  * SPDX-License-Identifier: GPL-3.0
 *
 */

package showcase.streaming.event.account.jdbc.sink;

import com.rabbitmq.stream.Consumer;
import com.rabbitmq.stream.Environment;
import showcase.streaming.event.account.jdbc.sink.consumer.AccountConsumer;
import showcase.streaming.event.account.jdbc.sink.domain.AccountEntity;
import lombok.extern.slf4j.Slf4j;
import nyla.solutions.core.patterns.conversion.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.rabbit.stream.config.SuperStream;

@Configuration
@Slf4j
@Profile("superStream")
public class RabbitSuperStreamConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${rabbitmq.streaming.offset:last}")
    private String offset;


    @Value("${spring.cloud.stream.bindings.input.destination:event-log-sink}")
    private String superStreamName;

    @Value("${rabbitmq.streaming.partitions:2}")
    private int partitions;

    @Value("${rabbitmq.streaming.concurrency:1}")
    private int concurrency;

    @Value("${spring.cloud.stream.rabbit.bindings.input.consumer.singleActiveConsumer:false}")
    private boolean singleActiveConsumer;

    SuperStream superStream(Environment environment) {

        log.info("Creating super stream: {}", superStreamName);

        environment.streamCreator().name(superStreamName)
                .superStream()
                .partitions(partitions).creator()
                .create();

        return new SuperStream(superStreamName, partitions);
    }

    @Bean
    Consumer consumer(Environment environment,
                      AccountConsumer consumerFunction, Converter<byte[], AccountEntity> converter){

        superStream(environment);

        var builder = environment.consumerBuilder()
                .superStream(superStreamName);

        if(singleActiveConsumer){
            builder = builder.name(applicationName).singleActiveConsumer();
        }


        return builder.messageHandler((context, message) -> {
                    consumerFunction.accept(converter.convert(message.getBodyAsBinary()));
                })
                .build();
    }

}