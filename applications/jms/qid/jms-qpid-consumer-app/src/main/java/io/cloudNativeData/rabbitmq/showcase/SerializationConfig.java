package io.cloudNativeData.rabbitmq.showcase;

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
    Converter<String,Account> accountConverter(ObjectMapper objectMapper)
    {
        return  text -> {
            log.info("account:{}",text);
            return objectMapper.readValue(text, Account.class);
        };
    }
}
