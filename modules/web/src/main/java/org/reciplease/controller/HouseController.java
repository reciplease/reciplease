package org.reciplease.controller;

import static java.util.stream.Collectors.toList;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.reciplease.configuration.CurrentHouse;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.HouseMember;
import org.reciplease.configuration.HouseOwner;
import org.reciplease.dto.CreateInviteRequest;
import org.reciplease.dto.HouseDto;
import org.reciplease.dto.HouseInviteDto;
import org.reciplease.dto.HouseMemberDto;
import org.reciplease.dto.UpdateMemberRoleRequest;
import org.reciplease.repository.HouseRepository;
import org.reciplease.service.InviteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/houses")
@Tag(name = "Houses")
@RequiredArgsConstructor
public class HouseController {

    private final HouseRepository houseRepository;
    private final InviteService inviteService;
    private final HouseAccess houseAccess;

    @GetMapping
    @PreAuthorize("hasRole('RECIPLEASE')")
    @Operation(operationId = "findAllHouses")
    public ResponseEntity<List<HouseDto>> findAll() {
        final var userId = currentUserId();
        final var houses = houseRepository.findAllForUser(userId).stream()
                .map(house -> HouseDto.from(
                        house, houseRepository.roleOf(house.id(), userId).orElse(null)))
                .collect(toList());
        return ResponseEntity.ok(houses);
    }

    @GetMapping("members")
    @HouseMember
    @Operation(operationId = "findHouseMembers")
    public ResponseEntity<List<HouseMemberDto>> findMembers(@CurrentHouse final String houseId) {
        final var members = houseRepository.members(houseId).stream()
                .map(HouseMemberDto::from)
                .collect(toList());
        return ResponseEntity.ok(members);
    }

    @PatchMapping("members/{userId}")
    @HouseOwner
    @Operation(operationId = "updateHouseMemberRole")
    public ResponseEntity<List<HouseMemberDto>> updateMemberRole(
            @CurrentHouse final String houseId,
            @PathVariable final String userId,
            @Valid @RequestBody final UpdateMemberRoleRequest request) {
        houseRepository.addMember(houseId, userId, request.getRole());
        return findMembers(houseId);
    }

    @DeleteMapping("members/{userId}")
    @HouseOwner
    @Operation(operationId = "removeHouseMember")
    public ResponseEntity<List<HouseMemberDto>> removeMember(
            @CurrentHouse final String houseId, @PathVariable final String userId) {
        if (userId.equals(currentUserId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        houseRepository.removeMember(houseId, userId);
        return findMembers(houseId);
    }

    @GetMapping("invites")
    @HouseOwner
    @Operation(operationId = "findPendingHouseInvites")
    public ResponseEntity<List<HouseInviteDto>> findPendingInvites(@CurrentHouse final String houseId) {
        final var invites = inviteService.pendingInvites(houseId).stream()
                .map(HouseInviteDto::from)
                .collect(toList());
        return ResponseEntity.ok(invites);
    }

    @PostMapping("invites")
    @HouseOwner
    @Operation(operationId = "createInvite")
    public ResponseEntity<HouseInviteDto> createInvite(
            @CurrentHouse final String houseId, @Valid @RequestBody final CreateInviteRequest request) {
        final var invite = inviteService.createInvite(houseId, request.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(HouseInviteDto.from(invite));
    }

    @DeleteMapping("invites/{inviteId}")
    @HouseOwner
    @Operation(operationId = "deleteHouseInvite")
    public ResponseEntity<Void> deleteInvite(
            @CurrentHouse final String houseId, @PathVariable final String inviteId) {
        final var deleted = inviteService.deleteInvite(houseId, inviteId);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private String currentUserId() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() ? authentication.getName() : null;
    }
}
