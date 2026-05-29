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
    Converter<Account,String> accountConverter(ObjectMapper objectMapper)
    {
        return account -> {
            log.info("account:{}",account);

            return objectMapper.writeValueAsString(account);
        };
    }
}
