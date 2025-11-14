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
@Profile("outputStream")
@Slf4j
public class PublisherStreamConfig {

    @Value("${spring.cloud.stream.bindings.output.destination:outputFilter}")
    private String outputQueue;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.rabbitmq.host:localhost}")
    private String host;

    @Value("${spring.rabbitmq.username:guest}")
    private String username;

    @Value("${spring.rabbitmq.password:guest}")
    private String password;

    @Bean("outputPublisher")
    Publisher publisher(@Qualifier("outputConnection") Connection connection,
                        @Qualifier("outputQueue") Management.QueueInfo output){

        log.info("output published to {}",output.name());

        return connection.publisherBuilder()
                .queue(output.name())
                .build();
    }

    @Bean("outputConnection")
    Connection outputConnection(Environment environment)
    {
        return environment.connectionBuilder().host(host)
                .name(applicationName)
                .username(username)
                .password(password)
                .build();
    }


    @Bean("outputQueue")
    Management.QueueInfo outputQueue(Management management)
    {
        return management
                .queue()
                .name(outputQueue)
                .stream()
                .queue()
                .declare();
    }
}
