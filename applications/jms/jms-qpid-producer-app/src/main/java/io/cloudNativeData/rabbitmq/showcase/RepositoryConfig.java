package io.cloudNativeData.rabbitmq.showcase;

import nyla.solutions.core.patterns.repository.SaveRepository;
import nyla.solutions.core.patterns.repository.memory.InMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import showcase.streaming.event.account.domain.Account;

import java.util.HashMap;

@Configuration
public class RepositoryConfig {

    @Bean
    SaveRepository<Account> repository()
    {
        return new InMemoryRepository<Account,String >(
                new HashMap<String,Account>(),"id");
    }
}
