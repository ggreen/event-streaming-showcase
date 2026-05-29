package io.cloudNativeData.rabbitmq.showcase.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import showcase.streaming.event.account.domain.Account;
import showcase.streaming.event.account.domain.Location;

@RestController
@RequestMapping("accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final MessageChannel messageChannel;

    @PostMapping
    public void sendAccount(@RequestBody Account account) {

        log.info("sending account: {}", account);

        var location = account.getLocation();

        var message = MessageBuilder
                .withPayload(account)
                .setHeader("name",account.getName())
                .setHeader("status",account.getStatus())
                .setHeader("city",location.getCityTown())
                .setHeader("state",location.getStateProvince())
                .setHeader("zip",location.getZipPostalCode())
                .build();

        messageChannel.send(message);

    }
}
