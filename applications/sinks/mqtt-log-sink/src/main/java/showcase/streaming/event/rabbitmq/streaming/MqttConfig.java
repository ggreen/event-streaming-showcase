package showcase.streaming.event.rabbitmq.streaming;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

/**
 * MqttConfig
 *
 * @author Gregory Green
 */
@Slf4j
@Configuration
public class MqttConfig
{

    @Value("${spring.application.name}")
    private String clientId;

    @Value("${mqtt.connectionUrl:tcp://localhost:1883}")
    private String connectionUrl;

    @Value("${mqtt.userName}")
    private String userName;

    @Value("${mqtt.userPassword}")
    private String userPassword;

    @Value("${mqtt.topic.filter}")
    private String topicFilter;

    @Bean
    IMqttClient mqttClient() throws MqttException
    {
        log.info("Subscribing to topic filter: {}, clientId: {}, connectionUrl: {}",topicFilter,clientId,connectionUrl);
        var mqttClient = new MqttClient(connectionUrl, clientId, new MemoryPersistence());

        var options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
//        options.setConnectionTimeout(10);
        options.setUserName(userName);
        options.setPassword(userPassword.toCharArray());
        mqttClient.connect(options);

        IMqttMessageListener listener = (topic, mqttMessage) -> {
            String body = new String(mqttMessage.getPayload(), StandardCharsets.US_ASCII);
            log.info("TOPIC: {} BODY: {}",topic,body);
        };
        mqttClient.subscribe(topicFilter,listener);


        return mqttClient;
    }

}
