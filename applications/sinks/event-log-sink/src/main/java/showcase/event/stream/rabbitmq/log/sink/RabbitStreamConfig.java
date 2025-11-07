/*
 *
 *  * Copyright 2023 VMware, Inc.
 *  * SPDX-License-Identifier: GPL-3.0
 *
 */

package showcase.event.stream.rabbitmq.log.sink;

import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.OffsetSpecification;
import lombok.extern.slf4j.Slf4j;
import nyla.solutions.core.util.Debugger;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.config.ListenerContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.rabbit.stream.listener.StreamListenerContainer;

import java.util.Arrays;

import static java.lang.String.valueOf;

@Configuration
@Slf4j
@Profile("stream")
public class RabbitStreamConfig {

    private static final String FILTER_PROP_NM = "stateProvince";

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.rabbitmq.username:guest}")
    private String username = "guest";

    @Value("${spring.rabbitmq.password:guest}")
    private String password = "guest";

    @Value("${spring.rabbitmq.host:127.0.0.1}")
    private String hostname = "localhost";

    @Value("${rabbitmq.streaming.offset:last}")
    private String offset;

    @Value("${rabbitmq.streaming.singleActiveConsumer:false}")
    private boolean singleActiveConsumer;

    @Value("${spring.cloud.stream.bindings.input.destination:event-log-sink}")
    private String streamName;


    @Value("${rabbitmq.streaming.filter.values:}")
    private String[] filterValues;

    @Bean
    ListenerContainerCustomizer<MessageListenerContainer> customizer(Environment environment) {
        return (cont, dest, group) -> {
            StreamListenerContainer container = (StreamListenerContainer) cont;
            container.setConsumerCustomizer((name, builder) -> {


                var rabbitOffset = offset(environment);

                //Process Filtering
                log.info("Filtering with values: {}", Debugger.toString(filterValues));
                builder.filter().values(filterValues)
                        .postFilter(msg ->
                                {
                                var mustFilter = Arrays.asList(filterValues)
                                        .contains(valueOf(msg.getApplicationProperties().get(FILTER_PROP_NM)));
                                log.info("Must filter: {} for values: {}",mustFilter,Debugger.toString(filterValues));
                                return mustFilter;
                                }
                        );

                if (singleActiveConsumer)
                {
                    log.info("setting single active consumer with name: {}",applicationName);
                    builder.name(applicationName)
                            .singleActiveConsumer();

                }

                if (OffsetSpecification.last().equals(rabbitOffset))
                {
                    log.info("Setting name in stream: {}",applicationName);
                    builder.name(applicationName);
                }
                else if (OffsetSpecification.first().equals(rabbitOffset)) {
                    log.info("Replay all messages");
                    builder.subscriptionListener(
                            subscriptionContext -> subscriptionContext
                                    .offsetSpecification(OffsetSpecification.first()));
                }

                log.info("Setting Offset: {}", rabbitOffset);
                builder.offset(rabbitOffset);
            });
        };
    }

//    @Bean
//    Queue stream(Environment environment) {
//        log.info("Creating stream: {}",streamName);
////
////        environment.streamCreator().name(streamName)
////                .create();
//
//        return QueueBuilder.durable(streamName)
//                .stream()
//                .build();
//    }
//
//    @Bean
//    Consumer consumer(Environment environment,
//                      java.util.function.Consumer<String> consumerFunction){
//
//        log.info("stream: {}, offset: {}, filterValues: {}, singleActiveConsumer: {}",
//                streamName,offset,filterValues,singleActiveConsumer);
//
//        var builder = environment.consumerBuilder()
//                .stream(streamName);
//
//        if(filterValues != null && filterValues.length > 0)
//                builder = builder.filter().values(filterValues)
//                        .postFilter(msg ->
//                                Arrays.asList(filterValues)
//                                        .contains(valueOf(msg.getApplicationProperties().get(FILTER_PROP_NM))))
//                        .builder();
//
//        var rabbitOffset = offset(environment);
//
//        if(singleActiveConsumer)
//            builder = builder.name(applicationName)
//                    .singleActiveConsumer();
//
//        if(OffsetSpecification.last().equals(rabbitOffset))
//            builder = builder.name(applicationName);
//        else if(OffsetSpecification.first().equals(rabbitOffset))
//        {
//            log.info("Replay all messages");
//            builder.subscriptionListener(
//                    subscriptionContext -> subscriptionContext
//                            .offsetSpecification(OffsetSpecification.first()));
//        }
//        return builder.offset(rabbitOffset)
//                .messageHandler((context, message) -> {
//                    consumerFunction.accept(new String(message.getBodyAsBinary()));
//                })
//                .build();
//    }

    OffsetSpecification offset(Environment environment){
        return switch (offset)
                    {
                        case "first" -> OffsetSpecification.first();
                        case "next" -> OffsetSpecification.next();
                        default ->  OffsetSpecification.last();
                };
    }
}