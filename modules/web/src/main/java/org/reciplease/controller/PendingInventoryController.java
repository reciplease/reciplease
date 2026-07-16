package org.reciplease.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.HouseMember;
import org.reciplease.configuration.HouseOwner;
import org.reciplease.dto.InventoryItemDto;
import org.reciplease.dto.PendingInventoryItemDto;
import org.reciplease.service.PendingInventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * The shopping-trip capture backlog: barcode + photos captured in the fast "add a whole shop"
 * loop, digitised later via {@link #complete}. The literal {@code pending} segment takes
 * precedence over {@link InventoryController}'s {@code api/inventory/{uuid}} mapping, so the two
 * controllers don't clash.
 */
@RestController
@RequestMapping("api/inventory/pending")
@RequiredArgsConstructor
public class PendingInventoryController {

    final PendingInventoryService pendingInventoryService;
    final HouseAccess houseAccess;

    @PostMapping
    @HouseOwner
    public ResponseEntity<PendingInventoryItemDto> create(@Valid @RequestBody final PendingInventoryItemDto itemDto) {
        final var savedItem = pendingInventoryService.save(itemDto.toEntity(houseAccess.requireHouseId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(PendingInventoryItemDto.from(savedItem));
    }

    @GetMapping
    @HouseMember
    public ResponseEntity<List<PendingInventoryItemDto>> findAll() {
        final List<PendingInventoryItemDto> items = pendingInventoryService.findAll(houseAccess.requireHouseId()).stream()
                .map(PendingInventoryItemDto::from)
                .collect(toList());

        return ResponseEntity.ok(items);
    }

    @GetMapping("{uuid}")
    @HouseMember
    public ResponseEntity<PendingInventoryItemDto> findById(@PathVariable final String uuid) {
        final Optional<PendingInventoryItemDto> foundItem = pendingInventoryService.findById(uuid)
                .filter(houseAccess::belongsToHeaderHouse)
                .map(PendingInventoryItemDto::from);

        return ResponseEntity.of(foundItem);
    }

    @DeleteMapping("{uuid}")
    @HouseOwner
    public ResponseEntity<Void> deleteById(@PathVariable final String uuid) {
        final var existing = pendingInventoryService.findById(uuid);
        if (existing.isEmpty() || !houseAccess.belongsToHeaderHouse(existing.get())) {
            return ResponseEntity.notFound().build();
        }

        pendingInventoryService.deleteById(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("{uuid}/complete")
    @HouseOwner
    public ResponseEntity<InventoryItemDto> complete(@PathVariable final String uuid, @Valid @RequestBody final InventoryItemDto itemDto) {
        final var existing = pendingInventoryService.findById(uuid);
        if (existing.isEmpty() || !houseAccess.belongsToHeaderHouse(existing.get())) {
            return ResponseEntity.notFound().build();
        }

        final var savedItem = pendingInventoryService.complete(uuid, itemDto.toEntity(houseAccess.requireHouseId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(InventoryItemDto.from(savedItem));
    }
}
