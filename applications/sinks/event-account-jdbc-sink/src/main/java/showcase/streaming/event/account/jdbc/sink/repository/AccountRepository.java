package showcase.streaming.event.account.jdbc.sink.repository;

import showcase.streaming.event.account.jdbc.sink.domain.AccountEntity;
import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<AccountEntity,String> {
}
