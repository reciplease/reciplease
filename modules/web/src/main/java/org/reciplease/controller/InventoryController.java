package org.reciplease.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.reciplease.dto.InventoryItemDto;
import org.reciplease.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryItemDto> create(@Valid @RequestBody final InventoryItemDto itemDto) {
        final var savedItem = inventoryService.save(itemDto.toEntity());
        final var savedItemDto = InventoryItemDto.from(savedItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItemDto);
    }

    @PutMapping("{uuid}")
    public ResponseEntity<InventoryItemDto> update(@PathVariable final String uuid, @Valid @RequestBody final InventoryItemDto itemDto) {
        if (inventoryService.findById(uuid).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        final var updated = inventoryService.update(uuid, itemDto.toEntity());
        return ResponseEntity.ok(InventoryItemDto.from(updated));
    }

    @GetMapping("{uuid}")
    public ResponseEntity<InventoryItemDto> findById(@PathVariable final String uuid) {
        final Optional<InventoryItemDto> foundItem = inventoryService.findById(uuid)
                .map(InventoryItemDto::from);

        return ResponseEntity.of(foundItem);
    }

    @GetMapping
    public ResponseEntity<List<InventoryItemDto>> findAll() {
        final List<InventoryItemDto> items = inventoryService.findAll().stream()
                .map(InventoryItemDto::from)
                .collect(toList());

        return ResponseEntity.ok(items);
    }

    @GetMapping("/unexpired")
    public ResponseEntity<List<InventoryItemDto>> findAllUnexpired() {
        final List<InventoryItemDto> items = inventoryService.findAllUnexpired().stream()
                .map(InventoryItemDto::from)
                .collect(toList());

        return ResponseEntity.ok(items);
    }

    @GetMapping("/expired")
    public ResponseEntity<List<InventoryItemDto>> findAllExpired() {
        final List<InventoryItemDto> items = inventoryService.findAllExpired().stream()
                .map(InventoryItemDto::from)
                .collect(toList());

        return ResponseEntity.ok(items);
    }
}
