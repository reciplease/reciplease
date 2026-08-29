package org.reciplease.controller;

import static java.util.stream.Collectors.toList;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class HouseController {

    private final HouseRepository houseRepository;
    private final InviteService inviteService;
    private final HouseAccess houseAccess;

    @GetMapping
    @PreAuthorize("hasRole('RECIPLEASE')")
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
    public ResponseEntity<List<HouseMemberDto>> findMembers() {
        final var members = houseRepository.members(houseAccess.requireHouseId()).stream()
                .map(HouseMemberDto::from)
                .collect(toList());
        return ResponseEntity.ok(members);
    }

    @PatchMapping("members/{userId}")
    @HouseOwner
    public ResponseEntity<List<HouseMemberDto>> updateMemberRole(
            @PathVariable final String userId, @Valid @RequestBody final UpdateMemberRoleRequest request) {
        houseRepository.addMember(houseAccess.requireHouseId(), userId, request.getRole());
        return findMembers();
    }

    @DeleteMapping("members/{userId}")
    @HouseOwner
    public ResponseEntity<List<HouseMemberDto>> removeMember(@PathVariable final String userId) {
        // An owner can remove anyone but themselves — self-removal could orphan the
        // house (and is better expressed as a future "leave house" action).
        if (userId.equals(currentUserId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        houseRepository.removeMember(houseAccess.requireHouseId(), userId);
        return findMembers();
    }

    @GetMapping("invites")
    @HouseOwner
    public ResponseEntity<List<HouseInviteDto>> findPendingInvites() {
        final var invites = inviteService.pendingInvites(houseAccess.requireHouseId()).stream()
                .map(HouseInviteDto::from)
                .collect(toList());
        return ResponseEntity.ok(invites);
    }

    @PostMapping("invites")
    @HouseOwner
    @Operation(operationId = "createInvite")
    public ResponseEntity<HouseInviteDto> createInvite(@Valid @RequestBody final CreateInviteRequest request) {
        final var invite = inviteService.createInvite(houseAccess.requireHouseId(), request.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(HouseInviteDto.from(invite));
    }

    @DeleteMapping("invites/{inviteId}")
    @HouseOwner
    public ResponseEntity<Void> deleteInvite(@PathVariable final String inviteId) {
        final var deleted = inviteService.deleteInvite(houseAccess.requireHouseId(), inviteId);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private String currentUserId() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() ? authentication.getName() : null;
    }
}
