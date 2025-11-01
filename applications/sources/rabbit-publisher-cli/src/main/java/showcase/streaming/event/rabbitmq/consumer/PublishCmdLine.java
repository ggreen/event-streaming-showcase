package showcase.streaming.event.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class PublishCmdLine implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(PublishCmdLine.class);

    @Value("${clientName:Send}")
    private String clientName ;

    @Value("${routingKey:hello-quorum}")
    private String routingKeyValue ;

    @Value("${exchange:}")
    private String exchangeName ;

    @Value("${message:Hello World!}")
    private String message ;

    @Override
    public void run(String... args) throws Exception {

        var factory = new ConnectionFactory();
        try (Connection connection = factory.newConnection(clientName);
             Channel channel = connection.createChannel()) {

            channel.basicPublish(exchangeName,
                    routingKeyValue,
                    null,
                    message.getBytes(StandardCharsets.UTF_8));
            logger.info(" [x] Sent {}", message);
        }
    }
}
