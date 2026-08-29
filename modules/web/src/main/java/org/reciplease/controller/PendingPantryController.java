package org.reciplease.controller;

import static java.util.stream.Collectors.toList;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.HouseMember;
import org.reciplease.configuration.HouseOwner;
import org.reciplease.dto.PantryItemDto;
import org.reciplease.dto.PendingPantryItemDto;
import org.reciplease.service.PendingPantryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The shopping-trip capture backlog: barcode + photos captured in the fast "add a whole shop"
 * loop, digitised later via {@link #complete}. The literal {@code pending} segment takes
 * precedence over {@link PantryController}'s {@code api/pantry/{uuid}} mapping, so the two
 * controllers don't clash.
 */
@RestController
@RequestMapping("api/pantry/pending")
@RequiredArgsConstructor
public class PendingPantryController {

    final PendingPantryService pendingPantryService;
    final HouseAccess houseAccess;

    @PostMapping
    @HouseOwner
    @Operation(operationId = "createPendingPantryItem")
    public ResponseEntity<PendingPantryItemDto> create(@Valid @RequestBody final PendingPantryItemDto itemDto) {
        final var savedItem = pendingPantryService.save(itemDto.toEntity(houseAccess.requireHouseId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(PendingPantryItemDto.from(savedItem));
    }

    @GetMapping
    @HouseMember
    public ResponseEntity<List<PendingPantryItemDto>> findAll() {
        final List<PendingPantryItemDto> items = pendingPantryService.findAll(houseAccess.requireHouseId()).stream()
                .map(PendingPantryItemDto::from)
                .collect(toList());

        return ResponseEntity.ok(items);
    }

    @GetMapping("{uuid}")
    @HouseMember
    public ResponseEntity<PendingPantryItemDto> findById(@PathVariable final String uuid) {
        final Optional<PendingPantryItemDto> foundItem = pendingPantryService
                .findById(uuid)
                .filter(houseAccess::belongsToHeaderHouse)
                .map(PendingPantryItemDto::from);

        return ResponseEntity.of(foundItem);
    }

    @DeleteMapping("{uuid}")
    @HouseOwner
    public ResponseEntity<Void> deleteById(@PathVariable final String uuid) {
        final var existing = pendingPantryService.findById(uuid);
        if (existing.isEmpty() || !houseAccess.belongsToHeaderHouse(existing.get())) {
            return ResponseEntity.notFound().build();
        }

        pendingPantryService.deleteById(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("{uuid}/complete")
    @HouseOwner
    public ResponseEntity<PantryItemDto> complete(
            @PathVariable final String uuid, @Valid @RequestBody final PantryItemDto itemDto) {
        final var existing = pendingPantryService.findById(uuid);
        if (existing.isEmpty() || !houseAccess.belongsToHeaderHouse(existing.get())) {
            return ResponseEntity.notFound().build();
        }

        final var savedItem = pendingPantryService.complete(uuid, itemDto.toEntity(houseAccess.requireHouseId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(PantryItemDto.from(savedItem));
    }
}
