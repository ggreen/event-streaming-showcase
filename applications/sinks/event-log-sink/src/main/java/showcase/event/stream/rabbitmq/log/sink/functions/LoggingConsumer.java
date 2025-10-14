package showcase.event.stream.rabbitmq.log.sink.functions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component("loggingConsumer")
public class LoggingConsumer implements Consumer<String> {
    public LoggingConsumer()
    {
        log.info("Created LoggingConsumer");
    }

    @Override
    public void accept(String text) {
        log.info("CONSUMED: {} ",text);
    }
}
