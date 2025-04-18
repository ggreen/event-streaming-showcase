package showcase.event.streaming.account.sink.repository;


import org.springframework.data.keyvalue.repository.KeyValueRepository;
import showcase.streaming.event.account.domain.Account;

public interface AccountRepository extends KeyValueRepository<Account,String> {
}
