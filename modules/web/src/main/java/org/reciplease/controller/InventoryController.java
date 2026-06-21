package org.reciplease.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.HouseMember;
import org.reciplease.configuration.HouseOwner;
import org.reciplease.dto.InventoryItemDto;
import org.reciplease.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    final HouseAccess houseAccess;

    @PostMapping
    @HouseOwner
    public ResponseEntity<InventoryItemDto> create(@Valid @RequestBody final InventoryItemDto itemDto) {
        final var savedItem = inventoryService.save(itemDto.toEntity(houseAccess.requireHouseId()));
        final var savedItemDto = InventoryItemDto.from(savedItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItemDto);
    }

    @PutMapping("{uuid}")
    @HouseOwner
    public ResponseEntity<InventoryItemDto> update(@PathVariable final String uuid, @Valid @RequestBody final InventoryItemDto itemDto) {
        final var existing = inventoryService.findById(uuid);
        if (existing.isEmpty() || !houseAccess.belongsToHeaderHouse(existing.get())) {
            return ResponseEntity.notFound().build();
        }

        final var updated = inventoryService.update(uuid, itemDto.toEntity(houseAccess.requireHouseId()));
        return ResponseEntity.ok(InventoryItemDto.from(updated));
    }

    @DeleteMapping("{uuid}")
    @HouseOwner
    public ResponseEntity<Void> deleteById(@PathVariable final String uuid) {
        final var existing = inventoryService.findById(uuid);
        if (existing.isEmpty() || !houseAccess.belongsToHeaderHouse(existing.get())) {
            return ResponseEntity.notFound().build();
        }

        inventoryService.deleteById(uuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{uuid}")
    @HouseMember
    public ResponseEntity<InventoryItemDto> findById(@PathVariable final String uuid) {
        final Optional<InventoryItemDto> foundItem = inventoryService.findById(uuid)
                .filter(houseAccess::belongsToHeaderHouse)
                .map(InventoryItemDto::from);

        return ResponseEntity.of(foundItem);
    }

    @GetMapping
    @HouseMember
    public ResponseEntity<List<InventoryItemDto>> findAll() {
        final List<InventoryItemDto> items = inventoryService.findAll(houseAccess.requireHouseId()).stream()
                .map(InventoryItemDto::from)
                .collect(toList());

        return ResponseEntity.ok(items);
    }

    @GetMapping("/unexpired")
    @HouseMember
    public ResponseEntity<List<InventoryItemDto>> findAllUnexpired() {
        final List<InventoryItemDto> items = inventoryService.findAllUnexpired(houseAccess.requireHouseId()).stream()
                .map(InventoryItemDto::from)
                .collect(toList());

        return ResponseEntity.ok(items);
    }

    @GetMapping("/expired")
    @HouseMember
    public ResponseEntity<List<InventoryItemDto>> findAllExpired() {
        final List<InventoryItemDto> items = inventoryService.findAllExpired(houseAccess.requireHouseId()).stream()
                .map(InventoryItemDto::from)
                .collect(toList());

        return ResponseEntity.ok(items);
    }
}
