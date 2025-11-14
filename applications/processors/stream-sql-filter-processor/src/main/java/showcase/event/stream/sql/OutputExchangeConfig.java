package showcase.event.stream.sql;

import com.rabbitmq.client.amqp.Connection;
import com.rabbitmq.client.amqp.Environment;
import com.rabbitmq.client.amqp.Management;
import com.rabbitmq.client.amqp.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("outputExchange")
@Slf4j
public class OutputExchangeConfig {
    @Value("${spring.cloud.stream.bindings.output.destination:sql.filter}")
    private String outputExchange;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.rabbitmq.host:localhost}")
    private String host;

    @Value("${spring.rabbitmq.username:guest}")
    private String username;

    @Value("${spring.rabbitmq.password:guest}")
    private String password;

    @Bean("outputConnection")
    Connection outputConnection(Environment environment)
    {
        return environment.connectionBuilder().host(host)
                .name(applicationName)
                .username(username)
                .password(password)
                .build();
    }

    @Bean
    Publisher publisher(@Qualifier("outputConnection")
                        Connection connection,
                        Management.ExchangeSpecification exchangeSpecification){

        log.info("Output will be published to exchange: {}",outputExchange);

        return connection.publisherBuilder()
                .exchange(outputExchange)
                .build();
    }

    @Bean("outputManagement")
    Management amqpManagement(@Qualifier("outputConnection") Connection connection)
    {
        return connection.management();
    }

    @Bean
    Management.ExchangeSpecification outputQueue(@Qualifier("outputManagement") Management management)
    {
        var exchange = management.exchange()
                .name(outputExchange)
                .type(Management.ExchangeType.FANOUT);

                exchange.declare();

         return exchange;

    }
}
