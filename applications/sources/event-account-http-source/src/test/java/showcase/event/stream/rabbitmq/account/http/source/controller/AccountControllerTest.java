package showcase.event.stream.rabbitmq.account.http.source.controller;

import nyla.solutions.core.patterns.creational.generator.JavaBeanGeneratorCreator;
import nyla.solutions.core.patterns.integration.Publisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import showcase.streaming.event.account.domain.Account;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    Account account;
    private AccountController subject;

    @Mock
    private MessageChannel publisher;

    @BeforeEach
    void setUp() {
        subject = new AccountController(publisher);
    }

    @Test
    void publish() {

        subject.publish(account);

        verify(publisher).send(any());

    }
}