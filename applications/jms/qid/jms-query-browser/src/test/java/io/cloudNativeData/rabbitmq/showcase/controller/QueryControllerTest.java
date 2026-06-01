package io.cloudNativeData.rabbitmq.showcase.controller;

import jakarta.jms.*;
import lombok.SneakyThrows;
import nyla.solutions.core.patterns.creational.generator.JavaBeanGeneratorCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;
import showcase.streaming.event.account.domain.Account;

import java.util.Enumeration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryControllerTest {

    private QueryController subject;

    @Mock
    private Queue queue;
    @Mock
    private Session session;
    @Mock
    private Converter<Message, Account> converter;

    @Mock
    private QueueBrowser queryBrowser;

    private final static String selector = "id = junit";
    private final Account account = JavaBeanGeneratorCreator.of(Account.class).create();
    @Mock
    private Enumeration messageEnumerator;
    private Message message;

    @BeforeEach
    void setUp() {
        subject = new QueryController(queue, session, converter);
    }

    @SneakyThrows
    @Test
    void query() {

        when(session.createBrowser(any(), anyString())).thenReturn(queryBrowser);
        when(queryBrowser.getEnumeration()).thenReturn(messageEnumerator);

        when(messageEnumerator.hasMoreElements()).thenReturn(true).thenReturn(false);
        when(messageEnumerator.nextElement()).thenReturn(message);
        when(converter.convert(any())).thenReturn(account);

        var actual = subject.query(selector);
        List<Account> expected = List.of(account);

        assertThat(actual).isEqualTo(expected);
    }
}