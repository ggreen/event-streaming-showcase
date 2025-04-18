package showcase.event.stream.rabbitmq.account.http.source;

import com.rabbitmq.client.amqp.Connection;
import com.rabbitmq.client.amqp.Environment;
import com.rabbitmq.client.amqp.Management;
import com.rabbitmq.client.amqp.impl.AmqpEnvironmentBuilder;
import lombok.extern.slf4j.Slf4j;
import nyla.solutions.core.patterns.conversion.Converter;
import nyla.solutions.core.patterns.integration.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import showcase.streaming.event.account.domain.Account;

@Configuration
@Slf4j
@Profile("amqp1.0")
public class RabbitAmqp1_0Config {

    @Value("${spring.cloud.stream.bindings.output.destination}")
    private String topicExchange;

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

    @Value("${spring.cloud.stream.bindings.output.destination:accounts}")
    private String streamName;

    @Value("${spring.cloud.stream.default.contentType:application/json}")
    private String contentType;

    @Bean
    Publisher<Account> publisher(Converter<Account,byte[]> converter, Connection connection, Management.QueueInfo streamInfo)
    {
        var publisher =
                connection
                        .publisherBuilder()
                        .queue(streamInfo.name())
                        .build();

        Publisher<Account> sender = account -> {
            var msg = publisher
                    .message(converter.convert(account))
                    .contentType(contentType)
                    .property("accountType", account.getAccountType());

            publisher.publish(msg, context -> {});

            log.info("published accountType:{}", account.getAccountType());
        };

        return  sender;
    }

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
