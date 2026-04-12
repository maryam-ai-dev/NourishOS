package com.nourishos.authority.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.nourishos.authority.domain.IngredientLot;
import com.nourishos.authority.domain.LotStatus;
import com.nourishos.authority.domain.ReplenishmentRequest;
import com.nourishos.authority.repository.IngredientLotRepository;
import com.nourishos.authority.repository.ReplenishmentRequestRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/replenishment/requests")
@RequiredArgsConstructor
public class OnlineOrderController {

    private final ReplenishmentRequestRepository requestRepository;
    private final IngredientLotRepository lotRepository;

    @PostMapping("/{id}/confirm-online-order")
    @Transactional
    public ReplenishmentRequest confirmOnline(@PathVariable UUID id) {
        ReplenishmentRequest req = requestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        req.setStatus("ORDERED");
        return requestRepository.save(req);
    }

    @PostMapping("/{id}/delivery-received")
    @Transactional
    public Map<String, Object> deliveryReceived(@PathVariable UUID id) {
        ReplenishmentRequest req = requestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Transition INCOMING lots to ACTIVE for this request
        List<IngredientLot> incomingLots = lotRepository.findAll().stream()
                .filter(l -> l.getStatus() == LotStatus.INCOMING)
                .toList();

        int transitioned = 0;
        for (IngredientLot lot : incomingLots) {
            lot.setStatus(LotStatus.ACTIVE);
            lotRepository.save(lot);
            transitioned++;
        }

        req.setStatus("DELIVERED");
        requestRepository.save(req);

        return Map.of(
                "requestId", id,
                "status", "DELIVERED",
                "lotsActivated", transitioned,
                "receivedAt", Instant.now().toString()
        );
    }

    @GetMapping("/inventory/incoming")
    public List<IngredientLot> incomingLots() {
        return lotRepository.findAll().stream()
                .filter(l -> l.getStatus() == LotStatus.INCOMING)
                .toList();
    }
}
