package io.cloudNativeData.rabbitmq.showcase;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import showcase.streaming.event.account.domain.Account;
import tools.jackson.databind.ObjectMapper;

@Configuration
@Slf4j
public class SerializationConfig {

    @Bean
    Converter<Message,Account> accountConverter(ObjectMapper objectMapper)
    {
        return  msg -> {
            log.info("account:{}",msg);
            try {
                return objectMapper.readValue(((TextMessage)msg).getText(), Account.class);
            } catch (JMSException e) {
                log.warn("failed to read account:{}",msg,e);
                throw new RuntimeException(e);
            }
        };
    }
}
