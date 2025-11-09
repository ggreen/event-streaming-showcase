package showcase.event.stream.rabbitmq.account.http.source;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import showcase.streaming.event.account.domain.Account;

@Configuration
public class SerializationConfig {

    @Bean
    ObjectMapper objectMapper()
    {
        return  new ObjectMapper();
    }

    @Bean
    Jackson2JsonMessageConverter conversion(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    Converter<Account,byte[]> converter(ObjectMapper objectMapper)
    {
        return account -> {
            try {
                return objectMapper.writeValueAsBytes(account);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
