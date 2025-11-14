package showcase.event.stream.sql;

import com.rabbitmq.client.amqp.*;
import com.rabbitmq.client.amqp.impl.AmqpEnvironmentBuilder;
import lombok.extern.slf4j.Slf4j;
import nyla.solutions.core.util.Debugger;
import nyla.solutions.core.util.Text;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
class ConsumerConfig {

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

    @Value("${stream.filter.sql}")
    private String sqlFilter;


    @Bean
    Environment amqpEnvironment()
    {
        return new AmqpEnvironmentBuilder()
                .build();
    }

    @Bean("inputConnection")
    Connection amqpConnection(Environment environment)
    {
        return environment.connectionBuilder().host(host)
                .name(applicationName)
                .username(username)
                .password(password)
                .build();
    }



    @Bean
    Consumer consumer(@Qualifier("inputConnection") Connection connection, Publisher publisher,
                      @Qualifier("input") Management.QueueInfo input){

        log.info("input consumed with SQL '{}' from input stream {}",sqlFilter,input.name());

        return connection.consumerBuilder()
                .queue(input.name())
                .stream()
                .offset(ConsumerBuilder.StreamOffsetSpecification.valueOf(offsetName))
                .filter()
                .sql(sqlFilter)
                .stream()
                .builder().messageHandler((ctx,inputMessage) -> {
                    //Processing input message
                    log.info("Processing input: {}, msg id: {}", inputQueue, inputMessage.messageId());

                    var outputMap = new HashMap<String,String>();
                    inputMessage.forEachProperty((name,value)-> {
                        var valueText = Text.toString(value);
                        log.info("Setting property: {}, value: {}",name,valueText);
                        outputMap.put(name, valueText);
                    });

                    if(outputMap.isEmpty())
                    {
                        log.warn("No properties provided to filter Nothing to filter");
                        return; //exit
                    }

                    Map.Entry<String,String> entry =  outputMap.entrySet().iterator().next();
                    log.info("entry: {}",entry);

                    var outputMsg = publisher.message(inputMessage.body())
                            .annotation("x-stream-filter-value", entry.getValue())
                            .subject(entry.getValue())
                                    .property(entry.getKey(),entry.getValue());

                    //Publish output
                    publisher.publish(outputMsg, outCtx ->{

                        if(outCtx.status() == Publisher.Status.ACCEPTED)
                        {
                            //confirm message
                            ctx.accept();
                            log.warn("Message Sent");
                        }
                        else
                            log.warn("Status {} != ACCEPTED",outCtx.status()); // do not accept message
                    });


                })
                .build();
    }


    @Bean("inputManagement")
    Management amqpManagement(@Qualifier("inputConnection") Connection connection)
    {
        return connection.management();
    }

    @Bean("input")
    Management.QueueInfo inputQueue(@Qualifier("inputManagement") Management management)
    {
        return management
                .queue()
                .name(inputQueue)
                .stream()
                .queue()
                .declare();
    }

}
