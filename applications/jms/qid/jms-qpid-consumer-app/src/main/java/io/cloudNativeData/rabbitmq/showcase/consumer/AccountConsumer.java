package io.cloudNativeData.rabbitmq.showcase.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nyla.solutions.core.patterns.repository.SaveRepository;
import nyla.solutions.core.patterns.repository.memory.ListRepository;
import org.springframework.stereotype.Component;
import showcase.streaming.event.account.domain.Account;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountConsumer implements Consumer<Account> {

    private final ListRepository<Account> repository;

    @Override
    public void accept(Account account) {

        log.info("Received account {}", account);
        repository.save(account);
    }


}
