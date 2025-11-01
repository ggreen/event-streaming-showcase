package showcase.event.stream.rabbitmq.account.http.source.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nyla.solutions.core.patterns.integration.Publisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import showcase.streaming.event.account.domain.Account;

@RestController
@RequiredArgsConstructor
@RequestMapping("accounts")
@Slf4j
public class AccountController {
    private final Publisher<Account> publisher;

    @PostMapping
    public void publish(@RequestBody Account account) {
        log.info("Publishing Account: {}",account);
        publisher.send(account);
    }
}
