package showcase.event.stream.rabbitmq.account.http.source;

import com.rabbitmq.stream.Environment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.config.ProducerMessageHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.integration.amqp.outbound.RabbitStreamMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;
import showcase.streaming.event.account.domain.Account;

@Configuration
@Slf4j
@Profile("stream")
public class RabbitStreamConfig {

    private static final String FILTER_PROP_NM = "stateProvince";

    @Value("${spring.cloud.stream.bindings.output.destination}")
    private String streamName;

    @Value("${rabbitmq.streaming.use.filter:false}")
    private boolean isUseFilter;

    @Bean
    ProducerMessageHandlerCustomizer<MessageHandler> handlerCustomizer() {
        return (hand, dest) -> {
            RabbitStreamMessageHandler handler = (RabbitStreamMessageHandler) hand;
            handler.setConfirmTimeout(5000);
            ((RabbitStreamTemplate) handler.getStreamOperations()).setProducerCustomizer(
                    (name, builder) -> {
                            builder.stream(streamName);

                        if(isUseFilter){
                            log.info("Using filter");
                            builder = builder.filterValue(msg ->
                                    {
                                        var filterValue = String.valueOf(msg.getApplicationProperties().get(FILTER_PROP_NM));
                                        log.info("filterValue: {}",filterValue);
                                        return filterValue;
                                    });


                        }
                    });
        };
    }
    @Bean
    MessageChannel publisher(RabbitStreamTemplate producer, Converter<Account,byte[]> converter)
    {
        return (msg, timeout) ->{

            var account = (Account)msg.getPayload();
            var state = account.getLocation() != null ? account.getLocation().getStateProvince() : "";

            producer.send(producer.messageBuilder()
                    .addData(converter.convert(account))
                    .properties().messageId(account.getId())
                    .messageBuilder()
                    .applicationProperties().entry(FILTER_PROP_NM,state)
                    .messageBuilder()
                    .build());
            return true;
        };
    }

    @Bean
    RabbitStreamTemplate rabbitStreamTemplate(Environment environment, MessageConverter converter) {
        var template = new RabbitStreamTemplate(environment,streamName);
        template.setMessageConverter(converter);
        template.setProducerCustomizer(
                (name, builder) -> {
                    builder.stream(streamName);

                    if(isUseFilter){
                        log.info("Using filter");
                        builder = builder.filterValue(msg ->
                        {
                            var filterValue = String.valueOf(msg.getApplicationProperties().get(FILTER_PROP_NM));
                            log.info("filterValue: {}",filterValue);
                            return filterValue;
                        });


                    }
                });

        return template;
    }


}
