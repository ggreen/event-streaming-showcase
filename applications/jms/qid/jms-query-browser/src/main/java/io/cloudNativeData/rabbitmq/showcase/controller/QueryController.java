package io.cloudNativeData.rabbitmq.showcase.controller;

import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.Session;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import showcase.streaming.event.account.domain.Account;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("jms/query")
public class QueryController {

    private final Queue queue;
    private final Session session;
    private final Converter<Message, Account> converter;

    @SneakyThrows
    @PostMapping
    public List<Account> query(@RequestBody String selector){

        try(QueueBrowser browser = session.createBrowser(queue, selector))
        {
            var list = new ArrayList<Account>();
            var enumeration = browser.getEnumeration();

            // Iterate through the matching messages
            while (enumeration.hasMoreElements()) {
                var message = (Message) enumeration.nextElement();
                list.add(converter.convert(message));
            }

            return list;
        }
    }
}
