package com.nourishos.authority.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nourishos.authority.domain.SupermarketAccount;
import com.nourishos.authority.repository.SupermarketAccountRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/households/{householdId}/supermarkets")
@RequiredArgsConstructor
public class SupermarketController {

    private final SupermarketAccountRepository repository;

    @GetMapping
    public List<SupermarketAccount> list(@PathVariable UUID householdId) {
        return repository.findByHouseholdId(householdId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupermarketAccount create(@PathVariable UUID householdId,
                                      @RequestBody SupermarketAccount account) {
        repository.findByHouseholdIdAndSupermarketName(householdId, account.getSupermarketName())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Account already exists for this supermarket");
                });
        account.setHouseholdId(householdId);
        account.setConnected(true);
        return repository.save(account);
    }

    @DeleteMapping("/{supermarketId}")
    public SupermarketAccount disconnect(@PathVariable UUID householdId,
                                          @PathVariable UUID supermarketId) {
        SupermarketAccount account = repository.findById(supermarketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        account.setConnected(false);
        return repository.save(account);
    }
}
