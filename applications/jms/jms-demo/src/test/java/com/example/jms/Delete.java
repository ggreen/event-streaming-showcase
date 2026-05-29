package com.example.jms;

import com.rabbitmq.client.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class Delete {
    private static ConnectionFactory factory;
    private static String userName = "vmware";
    private static String password = "tanzu";
    private String queueName = "test";
    private int messageCount = 1000000;
    private String hostName = "localhost";

    @BeforeAll
    static void setUp() {
        factory = new ConnectionFactory();
        // 1. Configure the connection factory

        factory.setHost("localhost");
        factory.setUsername(userName);
        factory.setPassword(password);// Change to your RabbitMQ IP if needed
    }

    @Test
    void publish() throws IOException, TimeoutException {


        // 2. Establish connection and create a channel
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // 3. Declare the queue (durable = true)
            Map<String, Object> quorum = Map.of("x-queue-type","quorum");
            channel.queueDeclare(queueName, true,
                    false, false, quorum);

            // 4. Loop to publish 100 messages
            for (int i = 1; i <= messageCount; i++) {
                String message = "Hello RabbitMQ - Mentssage " + i;

                channel.basicPublish("", queueName,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes(StandardCharsets.UTF_8));

                System.out.println(" [x] Sent '" + message + "'");
            }
        }
    }


    @Test
    void consume() {


        String queueName = "test";
        int prefetchCount = 1;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");

        try(Connection connection = factory.newConnection(); var channel = connection.createChannel();)
        {
            // Use prefetch of 1 to maintain order for message file names
            channel.basicQos(prefetchCount);
            GetResponse response = null;

            int count = 0;
            //Perform a get and do not acknowledge messages
            while((response = channel.basicGet(queueName, false)) != null){
                //File format: /tmp/<queue><count>.msg
                var filePath = Paths.get("/tmp/"+queueName+count+".msg");
                Files.write(filePath,response.getBody());
                count ++;
            }

            System.out.println(" [x] Received '" + count + "'");

        }
        catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

}
