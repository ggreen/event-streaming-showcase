package io.cloudNativeData.rabbitmq.showcase.controller;

import lombok.RequiredArgsConstructor;
import nyla.solutions.core.patterns.repository.FindAllRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import showcase.streaming.event.account.domain.Account;
import java.util.List;

@RestController
@RequestMapping("accounts")
@RequiredArgsConstructor
public class AccountController {


    private final FindAllRepository<Account> repository;

    @GetMapping
    public Iterable<Account> getAccounts() {
        return repository.findAll();
    }
}
