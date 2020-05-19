package org.reciplease.controller;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.InventoryItem;
import org.reciplease.repository.InventoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    final InventoryRepository inventoryRepository;

    @PostMapping
    public ResponseEntity<InventoryItem> create(@Valid @RequestBody final InventoryItem item) {
        final InventoryItem savedItem = inventoryRepository.save(item);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    @GetMapping("{uuid}")
    public ResponseEntity<InventoryItem> findById(@PathVariable final UUID uuid) {
        final Optional<InventoryItem> foundItem = inventoryRepository.findById(uuid);

        return ResponseEntity.of(foundItem);
    }

    @GetMapping
    public ResponseEntity<List<InventoryItem>> findAll() {
        final List<InventoryItem> items = inventoryRepository.findAll();

        return ResponseEntity.status(HttpStatus.OK).body(items);
    }
}
