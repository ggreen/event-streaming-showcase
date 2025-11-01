package showcase.streaming.event.rabbitmq.consumer;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class ConsumerCmdLine implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerCmdLine.class);

    @Value("${clientName:Receive}")
    private String clientName;

    @Value("${queue:hello-quorum}")
    private String queueName;

    @Value("${exchange:}")
    private String exchangeName;

    @Value("${queueType:quorum}")
    private String queueType;

    @Value("${routingKey:}")
    private String routingKeyValue;

    @Value("${prefetchSize:0}")
    private int prefetchSize;

    @Value("${prefetchCount:1}")
    private int prefetchCount;

    @Value("${autoAck:true}")
    private boolean autoAckFlag;

    @Value("${streamOffset:last}")
    private String streamOffset;

    @Override
    public void run(String... args) throws Exception {
        //Get Settings

        var consumerArguments =  new HashMap<String, Object>();

        if("stream".equals(queueType))
        {
            consumerArguments.put("x-stream-offset", streamOffset);

        }

        //Make connection
        var factory = new ConnectionFactory();
//        factory.setUri("amqp://");
        factory.setClientProperties(Map.of("name",clientName));

        try(var connection = factory.newConnection(clientName)) {

            try (var channel = connection.createChannel()) {
                channel.queueDeclare(queueName,
                        true,
                        false,
                        false,
                        Map.of("x-queue-type", queueType));

                //Declare Qualify of service consumption
                channel.basicQos(prefetchSize, prefetchCount, false);

                //Default Exchange Binding rules
                if (!exchangeName.isEmpty())
                    channel.queueBind(queueName,
                            exchangeName,
                            routingKeyValue);


                System.out.println(" [*] Waiting for messages.");
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    System.out.println(" [x] Received '" + message + "'");

                    if(!autoAckFlag)
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
                };

                channel.basicConsume(queueName, autoAckFlag, consumerArguments, deliverCallback, consumerTag -> {
                });

                System.out.println(" Press [enter] to exit.");
                System.in.read();
            }
            }
        }


}
