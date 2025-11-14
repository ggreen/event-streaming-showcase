package showcase.event.stream.sql;

import com.rabbitmq.client.amqp.Connection;
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
public class OutputStreamConfig {

    @Value("${spring.cloud.stream.bindings.output.destination:outputFilter}")
    private String outputQueue;

    @Bean
    Publisher publisher(Connection connection,
                        @Qualifier("output") Management.QueueInfo output){

        log.info("output published to {}",output.name());

        return connection.publisherBuilder()
                .queue(output.name())
                .build();
    }


    @Bean("output")
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
