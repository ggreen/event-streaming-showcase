package com.example.jms;

import com.rabbitmq.client.*;
import com.rabbitmq.stream.Environment;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class DeleteStreams {
//    private String queueName = "test";
//    private int messageCount = 100;
//    private String hostName = "localhost";
    private String  username = "vmware";
    private String password = "tanzu";


    @Test
    void consume() {

        var queueName = "test";
        var streamName = "stream-dump";
        var prefetchCount = 10000;
        int subEntrySize = 5000;
        var host = "localhost";


        var producerName = "streamDump";
        var factory = new ConnectionFactory();

        factory.setHost(host);
        factory.setPort(5672);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost("/");



        try(//Build the AMQP Connection/Channel
            var connection = factory.newConnection();
            var channel = connection.createChannel();
            //Build stream connection/producer
            var env = Environment.builder().host(host)
                    .username(username)
                    .password(password)
                    .port(5552).build();
            var producer = env.producerBuilder()
                    .name(producerName)
                    .stream(streamName)
                    .batchSize(prefetchCount)
                    .subEntrySize(subEntrySize)
                    .build()
        ){
            AMQP.Queue.DeclareOk response = channel.queueDeclarePassive(queueName);

            int messageCount = response.getMessageCount();
            int consumerCount = response.getConsumerCount();
        }
        catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }



    }

}
