package showcase.event.streaming.account.sink.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import showcase.event.streaming.account.sink.repository.AccountRepository;
import showcase.streaming.event.account.domain.Account;

import java.util.function.Consumer;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccountConsumer implements Consumer<Account> {

    private final AccountRepository repository;


    @Override
    public void accept(Account account) {
        log.info("Received: {}",account);

        repository.save(account);
    }
}
