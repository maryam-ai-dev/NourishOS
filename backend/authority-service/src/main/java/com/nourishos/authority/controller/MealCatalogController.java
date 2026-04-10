package com.nourishos.authority.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nourishos.authority.domain.MealOption;
import com.nourishos.authority.service.planning.MealOptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/meal-catalog")
@RequiredArgsConstructor
public class MealCatalogController {

    private final MealOptionService mealOptionService;

    @GetMapping
    public List<MealOption> list() {
        return mealOptionService.findAll();
    }

    @GetMapping("/{id}")
    public MealOption get(@PathVariable UUID id) {
        return mealOptionService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MealOption create(@Valid @RequestBody MealOption mealOption) {
        return mealOptionService.create(mealOption);
    }

    @PutMapping("/{id}")
    public MealOption update(@PathVariable UUID id, @Valid @RequestBody MealOption mealOption) {
        return mealOptionService.update(id, mealOption);
    }
}
