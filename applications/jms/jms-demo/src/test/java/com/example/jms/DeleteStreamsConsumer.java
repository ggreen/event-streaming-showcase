package com.example.jms;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.amqp.Connection;
import com.rabbitmq.client.amqp.Consumer;
import com.rabbitmq.client.amqp.impl.AmqpEnvironmentBuilder;
import com.rabbitmq.stream.Environment;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class DeleteStreamsConsumer {

    private String username = "vmware";
    private String password = "tanzu";

    @Test
    void consume() throws IOException, TimeoutException, InterruptedException {

        var queueName = "test";
        var host = "localhost";
        var producerName = "streamDump";
        var streamName = "stream-dump";
        var prefetchCount = 10000;
        int subEntrySize = 5000;
        long sleepMs = 5 * 1000; //5 seconds


        //Create AMQP 0.9.1 connect to get message count
        var ampq091ConnectionFactory = new ConnectionFactory();
        ampq091ConnectionFactory.setHost(host);

        //Set Credentials
        ampq091ConnectionFactory.setUsername(username);
        ampq091ConnectionFactory.setPassword(password);
        Consumer amp1Consumer = null;
        try (
                //Get AMQP connection/channel to get message ount
                var ampq091Connection = ampq091ConnectionFactory.newConnection();
                var channel = ampq091Connection.createChannel();
                //Create AMQP 1.0 env to improved throughput
                var amqp1Environment = new AmqpEnvironmentBuilder()
                        .connectionSettings()
                        .username(username)
                        .password(password)
                        .environmentBuilder()
                        .build();
                //Create AMQP 1.0 connection to improved throughput
                var amqp1Connection = amqp1Environment.connectionBuilder().build();
                //Create RabbitMQ Stream connections
                var streamEnv = Environment.builder().host(host)
                        .username(username)
                        .password(password)
                        .build();

                //Create RabbitMQ Stream producer
                var streamProducer = streamEnv.producerBuilder()
                        .name(producerName)
                        .stream(streamName)
                        .batchSize(prefetchCount)
                        .subEntrySize(subEntrySize)
                        .build();

        ) {
            //Get current message count
            var messageCount = channel.queueDeclarePassive(queueName).getMessageCount();

            //Keep a counter of processed messages
            var count = new AtomicInteger(0);

            System.out.println("[*] Waiting for " + messageCount + " messages");

            //Consumer messages
            amp1Consumer = amqp1Connection.consumerBuilder()
                    .queue(queueName)
                    .initialCredits(messageCount)
                    .messageHandler((context, inputMsg) -> {

                                var outMsgBuilder = streamProducer.messageBuilder()
                                        .publishingId(count.getAndIncrement()) //use count a publisherId
                                        .addData(inputMsg.body())
                                        .properties()
                                        .contentType(inputMsg.contentType())
                                        .contentEncoding(inputMsg.contentEncoding())
                                        .messageBuilder();

                                //copy headers to application properties
                                var outMsgAppPropBuilder = outMsgBuilder.applicationProperties();
                                inputMsg.forEachProperty((k, v) -> outMsgAppPropBuilder.entry(k, v.toString()));

                                //Build message to send
                                var msgOutput = outMsgAppPropBuilder.messageBuilder().build();

                                streamProducer.send(msgOutput,
                                        confirmationStatus -> {
                                            if (!confirmationStatus.isConfirmed()) {
                                                {
                                                    //Warn occurred
                                                    System.err.println("WARNING NOT SENT msg:" + confirmationStatus.getMessage());
                                                }
                                            }

                                        });
                            }

                    )
                    .build();

            //Wait for all messages to be processes
            while (count.get() < messageCount) {
                System.out.println("count: " + count + " < " + messageCount);
                Thread.sleep(sleepMs);
            }

            System.out.println("Copied Count: " + count);
        } finally {
            if (amp1Consumer != null) {
                amp1Consumer.close();
            }
        }


    }

}
