package io.cloudNativeData.rabbitmq.showcase;

import nyla.solutions.core.patterns.repository.SaveRepository;
import nyla.solutions.core.patterns.repository.memory.InMemoryRepository;
import nyla.solutions.core.patterns.repository.memory.ListRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import showcase.streaming.event.account.domain.Account;

import java.util.ArrayList;
import java.util.HashMap;

@Configuration
public class RepositoryConfig {

    @Bean
    ListRepository<Account> repository()
    {
        return new ListRepository<Account>(new ArrayList<>());
    }
}
