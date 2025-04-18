package showcase.event.streaming.account.sink;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.amqp.Connection;
import com.rabbitmq.client.amqp.ConsumerBuilder;
import com.rabbitmq.client.amqp.Environment;
import com.rabbitmq.client.amqp.Management;
import com.rabbitmq.client.amqp.impl.AmqpEnvironmentBuilder;
import lombok.extern.slf4j.Slf4j;
import nyla.solutions.core.patterns.conversion.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import showcase.streaming.event.account.domain.Account;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static com.rabbitmq.client.amqp.ConsumerBuilder.StreamOffsetSpecification.FIRST;

@Configuration
@Slf4j
//@Profile("amqp1.0")
public class RabbitAmqp1_0Config {


    @Value("${spring.cloud.stream.bindings.output.destination}")
    private String streamName;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.rabbitmq.routing.key:}")
    private String routingKey;

    @Value("${spring.rabbitmq.host:localhost}")
    private String host;

    @Value("${spring.application.name:event-account-http-source}")
    private String name;

    @Value("${spring.rabbitmq.username:guest}")
    private String username;

    @Value("${spring.rabbitmq.password:guest}")
    private String password;



    @Value("${spring.cloud.stream.default.contentType:application/json}")
    private String contentType;

    @Value("${account.type:premium}")
    private String accountType;


    @Bean
    Environment amqpEnvironment()
    {
        return new AmqpEnvironmentBuilder()
                .build();
    }

    @Bean
    Connection amqpConnection(Environment environment)
    {
        return environment.connectionBuilder().host(host)
                .name(name)
                .username(username)
                .password(password)
                .build();
    }

    @Bean
     Converter<byte[], Account> converter(ObjectMapper objectMapper)
    {
        return bytes -> {
            try {
                return objectMapper.readValue(new String(bytes, StandardCharsets.UTF_8),Account.class);
            } catch (JsonProcessingException e) {
                log.error("Error {}",e);
                throw new RuntimeException(e);
            }
        };
    }


    @Bean
     com.rabbitmq.client.amqp.Consumer amqpConsumer(Consumer<Account> accountConsumer,
                                               Converter<byte[],Account> converter,
                                               Connection connection) {
        var builder =
                connection
                        .consumerBuilder()
                        .queue(streamName)
                        .messageHandler(
                                (ctx, msg) -> {
                                    accountConsumer.accept(converter.convert(msg.body()));
                                    ctx.accept();
                                });

        Consumer<ConsumerBuilder.StreamFilterOptions> filterOptions = m -> m.property("accountType", accountType);
        filterOptions.accept(builder.stream().offset(FIRST).filter());
        return builder.build();
    }

    @Bean
    Management amqpManagement(Connection connection)
    {
        return connection.management();
    }

    @Bean
    Management.QueueInfo messageAmqpStream(Management management)
    {
        return management
                .queue()
                .name(streamName)
                .stream()
                .queue()
                .declare();
    }
}
