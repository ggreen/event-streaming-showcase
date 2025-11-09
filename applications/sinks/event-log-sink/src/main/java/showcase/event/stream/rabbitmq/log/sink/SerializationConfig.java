package showcase.event.stream.rabbitmq.log.sink;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.support.MessageBuilder;

@Configuration
public class SerializationConfig {

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
}
