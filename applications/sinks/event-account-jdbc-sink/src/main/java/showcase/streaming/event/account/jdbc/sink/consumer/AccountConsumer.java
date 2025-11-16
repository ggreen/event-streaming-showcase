package showcase.streaming.event.account.jdbc.sink.consumer;

import showcase.streaming.event.account.jdbc.sink.domain.AccountEntity;
import showcase.streaming.event.account.jdbc.sink.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

    @Component
    @RequiredArgsConstructor
    @Slf4j
    public class AccountConsumer implements Consumer<AccountEntity> {

        private final AccountRepository repository;
        @Override
        public void accept(AccountEntity account) {
            log.info("Save account: {}",account);
            repository.save(account);

        }
    }

