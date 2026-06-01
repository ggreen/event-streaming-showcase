package io.cloudNativeData.rabbitmq.showcase.controller;

import nyla.solutions.core.patterns.creational.generator.JavaBeanGeneratorCreator;
import nyla.solutions.core.patterns.repository.FindAllRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import showcase.streaming.event.account.domain.Account;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private AccountController subject;
    @Mock
    private FindAllRepository<Account> repository;
    private final Account account = JavaBeanGeneratorCreator.of(Account.class).create();

    @BeforeEach
    void setUp() {
        subject = new AccountController(repository);
    }

    @Test
    void getAccounts() {
        List<Account> expected = List.of(account);
        when(repository.findAll()).thenReturn(expected);

        var actual = subject.getAccounts();

        assertThat(actual).isEqualTo(expected);
    }
}