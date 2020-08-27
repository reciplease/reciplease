package org.reciplease.controller;

import lombok.RequiredArgsConstructor;
import org.reciplease.dto.InventoryItemDto;
import org.reciplease.service.InventoryService;
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
    final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryItemDto> create(@Valid @RequestBody final InventoryItemDto item) {
        final InventoryItemDto savedItem = inventoryService.save(item);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    @GetMapping("{uuid}")
    public ResponseEntity<InventoryItemDto> findById(@PathVariable final UUID uuid) {
        final Optional<InventoryItemDto> foundItem = inventoryService.findById(uuid);

        return ResponseEntity.of(foundItem);
    }

    @GetMapping
    public ResponseEntity<List<InventoryItemDto>> findAll() {
        final List<InventoryItemDto> items = inventoryService.findAll();

        return ResponseEntity.status(HttpStatus.OK).body(items);
    }
}
