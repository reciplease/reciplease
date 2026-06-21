package org.reciplease.controller;

import lombok.RequiredArgsConstructor;
import org.reciplease.dto.HouseDto;
import org.reciplease.repository.HouseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("api/houses")
@RequiredArgsConstructor
public class HouseController {

    private final HouseRepository houseRepository;

    @GetMapping
    @PreAuthorize("hasRole('RECIPLEASE')")
    public ResponseEntity<List<HouseDto>> findAll() {
        final var userId = currentUserId();
        final var houses = houseRepository.findAllForUser(userId).stream()
                .map(house -> HouseDto.from(house, houseRepository.roleOf(house.id(), userId).orElse(null)))
                .collect(toList());
        return ResponseEntity.ok(houses);
    }

    private String currentUserId() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() ? authentication.getName() : null;
    }
}
