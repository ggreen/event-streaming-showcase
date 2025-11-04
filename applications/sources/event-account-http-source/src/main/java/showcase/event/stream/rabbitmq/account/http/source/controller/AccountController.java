package showcase.event.stream.rabbitmq.account.http.source.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import showcase.streaming.event.account.domain.Account;

import static showcase.event.stream.rabbitmq.account.http.source.properties.AccountSourceConstants.ROUTING_KEY;

/**
 * @author gregory green
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("accounts")
@Slf4j
public class AccountController {
    private final MessageChannel publisher;

    @PostMapping
    public void publish(@RequestBody Account account) {
        log.info("Publishing Account: {}",account);
        publisher.send(MessageBuilder.withPayload(account)
                .setHeader(ROUTING_KEY,account.getId())
                        .setHeader(MessageHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build());
    }
}
