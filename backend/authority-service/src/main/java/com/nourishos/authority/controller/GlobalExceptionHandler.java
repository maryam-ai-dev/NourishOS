package com.nourishos.authority.controller;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nourishos.authority.service.HouseholdNotFoundException;
import com.nourishos.authority.service.InvalidNutritionGoalUnitException;
import com.nourishos.authority.service.MemberNotFoundException;
import com.nourishos.authority.service.inventory.InvalidParLevelException;
import com.nourishos.authority.service.inventory.LotAlreadyOpenException;
import com.nourishos.authority.service.inventory.LotNotActiveException;
import com.nourishos.authority.service.inventory.NegativeQuantityException;
import com.nourishos.authority.service.inventory.ParLevelNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                        (a, b) -> a));
        return Map.of("error", "validation_failed", "fields", errors);
    }

    @ExceptionHandler({HouseholdNotFoundException.class, MemberNotFoundException.class,
            ParLevelNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(RuntimeException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(InvalidParLevelException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidParLevel(InvalidParLevelException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(InvalidNutritionGoalUnitException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidUnit(InvalidNutritionGoalUnitException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler({NegativeQuantityException.class, LotNotActiveException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, String> handleUnprocessable(RuntimeException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(LotAlreadyOpenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleConflict(LotAlreadyOpenException ex) {
        return Map.of("error", ex.getMessage());
    }
}
