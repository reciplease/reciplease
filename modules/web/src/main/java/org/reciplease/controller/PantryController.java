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
import org.reciplease.service.PantryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/pantry")
@RequiredArgsConstructor
public class PantryController {

    final PantryService pantryService;
    final HouseAccess houseAccess;

    @PostMapping
    @HouseOwner
    @Operation(operationId = "createPantryItem")
    public ResponseEntity<PantryItemDto> create(@Valid @RequestBody final PantryItemDto itemDto) {
        final var savedItem = pantryService.save(itemDto.toEntity(houseAccess.requireHouseId()));
        final var savedItemDto = PantryItemDto.from(savedItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItemDto);
    }

    @PutMapping("{uuid}")
    @HouseOwner
    @Operation(operationId = "updatePantryItem")
    public ResponseEntity<PantryItemDto> update(
            @PathVariable final String uuid, @Valid @RequestBody final PantryItemDto itemDto) {
        final var existing = pantryService.findById(uuid);
        if (existing.isEmpty() || !houseAccess.belongsToHeaderHouse(existing.get())) {
            return ResponseEntity.notFound().build();
        }

        return pantryService
                .update(uuid, itemDto.toEntity(houseAccess.requireHouseId()))
                .map(PantryItemDto::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @DeleteMapping("{uuid}")
    @HouseOwner
    public ResponseEntity<Void> deleteById(@PathVariable final String uuid) {
        final var existing = pantryService.findById(uuid);
        if (existing.isEmpty() || !houseAccess.belongsToHeaderHouse(existing.get())) {
            return ResponseEntity.notFound().build();
        }

        pantryService.deleteById(uuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{uuid}")
    @HouseMember
    public ResponseEntity<PantryItemDto> findById(@PathVariable final String uuid) {
        final Optional<PantryItemDto> foundItem = pantryService
                .findById(uuid)
                .filter(houseAccess::belongsToHeaderHouse)
                .map(PantryItemDto::from);

        return ResponseEntity.of(foundItem);
    }

    @GetMapping
    @HouseMember
    public ResponseEntity<List<PantryItemDto>> findAll(
            @RequestParam(defaultValue = "false") final boolean excludeFullyConsumed) {
        final List<PantryItemDto> items =
                pantryService.findAll(houseAccess.requireHouseId(), excludeFullyConsumed).stream()
                        .map(PantryItemDto::from)
                        .collect(toList());

        return ResponseEntity.ok(items);
    }

    @GetMapping("/unexpired")
    @HouseMember
    public ResponseEntity<List<PantryItemDto>> findAllUnexpired() {
        final List<PantryItemDto> items = pantryService.findAllUnexpired(houseAccess.requireHouseId()).stream()
                .map(PantryItemDto::from)
                .collect(toList());

        return ResponseEntity.ok(items);
    }

    @GetMapping("/expired")
    @HouseMember
    public ResponseEntity<List<PantryItemDto>> findAllExpired() {
        final List<PantryItemDto> items = pantryService.findAllExpired(houseAccess.requireHouseId()).stream()
                .map(PantryItemDto::from)
                .collect(toList());

        return ResponseEntity.ok(items);
    }
}
