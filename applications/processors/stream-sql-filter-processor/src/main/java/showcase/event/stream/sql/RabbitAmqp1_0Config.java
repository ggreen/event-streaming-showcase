package showcase.event.stream.sql;

import com.rabbitmq.client.amqp.*;
import com.rabbitmq.client.amqp.impl.AmqpEnvironmentBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
class RabbitAmqp1_0Config {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.rabbitmq.host:localhost}")
    private String host;

    @Value("${spring.rabbitmq.username:guest}")
    private String username;

    @Value("${spring.rabbitmq.password:guest}")
    private String password;

//
//    @Value("${spring.cloud.stream.default.contentType:application/json}")
//    private String contentType;

    @Value("${stream.filter.offset:FIRST}")
    private String offsetName;

    @Value("${spring.cloud.stream.bindings.input.destination:inputFilter}")
    private String inputQueue;

    @Value("${spring.cloud.stream.bindings.output.destination:outputFilter}")
    private String outputQueue;


    @Value("${stream.filter.sql}")
    private String sqlFilter;


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
                .name(applicationName)
                .username(username)
                .password(password)
                .build();
    }

    @Bean
    Publisher publisher(Connection connection,
                        @Qualifier("output") Management.QueueInfo output){

        log.info("output published to {}",output.name());

        return connection.publisherBuilder()
                .queue(output.name())
                .build();
    }

    @Bean
    Consumer consumer(Connection connection, Publisher publisher, @Qualifier("input") Management.QueueInfo input){

        log.info("input consumed with SQL '{}' from input stream {}",sqlFilter,input.name());

        return connection.consumerBuilder()
                .queue(input.name())
                .stream()
                .offset(ConsumerBuilder.StreamOffsetSpecification.valueOf(offsetName))
                .filter()
                .sql(sqlFilter)
                .stream()
                .builder().messageHandler((ctx,msg) -> {
                    //Processing input message
                    log.info("Processing msg id: {}",
                            msg.messageId());

                    var outputMsg = publisher.message(msg.body());

                    //Publish output
                    publisher.publish(outputMsg, outCtx ->{
                        log.info("Sent output status: {}",outCtx.status());
                    });
                })
                .build();
    }


    @Bean
    Management amqpManagement(Connection connection)
    {
        return connection.management();
    }

    @Bean("input")
    Management.QueueInfo inputQueue(Management management)
    {
        return management
                .queue()
                .name(inputQueue)
                .stream()
                .queue()
                .declare();
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
