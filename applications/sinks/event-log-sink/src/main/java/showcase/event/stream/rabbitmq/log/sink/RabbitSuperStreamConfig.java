package showcase.event.stream.rabbitmq.log.sink;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.support.MessageBuilder;

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


    @Bean
    MessageConverter mc(ObjectMapper mapper)
    {
        return new MessageConverter() {
            @Override
            public Object fromMessage(Message<?> message, Class<?> targetClass) {
                var payload = message.getPayload();
                if(payload instanceof byte[] payloadBytes)
                    return new String(payloadBytes);

                return payload;
            }

            @Override
            public Message<?> toMessage(Object payload, MessageHeaders headers) {
                return MessageBuilder.withPayload(payload).build();
            }
        };
    }

//    @Bean
//    SuperStream superStream(Environment environment) {
//
//        log.info("Creating super stream: {}", superStreamName);
//
//        environment.streamCreator().name(superStreamName)
//                .superStream()
//                .partitions(partitions).creator()
//                .create();
//
//        return new SuperStream(superStreamName, partitions);
//    }

//    @Bean
//    Consumer consumer(Environment environment,
//                      java.util.function.Consumer<String> consumerFunction){
//        var builder = environment.consumerBuilder()
//                .superStream(superStreamName);
//
//        if(singleActiveConsumer){
//            builder = builder.name(applicationName).singleActiveConsumer();
//        }
//
//
//        return builder.messageHandler((context, message) -> {
//                    consumerFunction.accept(new String(message.getBodyAsBinary()));
//                })
//                .build();
//    }

}