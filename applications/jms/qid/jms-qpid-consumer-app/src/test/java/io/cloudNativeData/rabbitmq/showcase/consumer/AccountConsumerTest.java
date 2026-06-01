package io.cloudNativeData.rabbitmq.showcase.consumer;

import nyla.solutions.core.patterns.creational.generator.JavaBeanGeneratorCreator;
import nyla.solutions.core.patterns.repository.SaveRepository;
import nyla.solutions.core.patterns.repository.memory.ListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import showcase.streaming.event.account.domain.Account;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountConsumerTest {

    private final Account account = JavaBeanGeneratorCreator.of(Account.class).create();
    private AccountConsumer subject;

    @Mock
    private ListRepository<Account> repository;

    @BeforeEach
    void setUp() {
        subject = new AccountConsumer(repository);
    }

    @Test
    void accept() {
        subject.accept(account);

        verify(repository).save(any());
    }
}