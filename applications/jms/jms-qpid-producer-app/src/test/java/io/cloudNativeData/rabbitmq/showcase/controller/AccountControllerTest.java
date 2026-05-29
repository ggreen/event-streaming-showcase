package io.cloudNativeData.rabbitmq.showcase.controller;

import nyla.solutions.core.patterns.creational.generator.JavaBeanGeneratorCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageChannel;
import showcase.streaming.event.account.domain.Account;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private final Account account = JavaBeanGeneratorCreator.of(Account.class).create();
    private AccountController subject;

    @Mock
    private MessageChannel messageChannel;

    @BeforeEach
    void setUp() {
        subject = new AccountController(messageChannel);
    }

    @Test
    void sendAccount() {

        subject.sendAccount(account);

        verify(messageChannel).send(any());
    }
}