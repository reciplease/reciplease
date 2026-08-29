package org.reciplease.configuration;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.reciplease.model.HouseRole;
import org.reciplease.model.HouseScoped;
import org.reciplease.repository.HouseRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Backs the {@code @houseAccess.isMember()} / {@code @houseAccess.isOwner()}
 * {@code @PreAuthorize} expressions used by house-scoped controllers. Resolves the
 * active house from the {@code X-RCPLS-House-Id} request header and the current user
 * from the security context, so authorization decisions stay declarative on the
 * controller methods instead of branching inside them.
 */
@Component("houseAccess")
@RequiredArgsConstructor
public class HouseAccess {

    public static final String HOUSE_HEADER = "X-RCPLS-House-Id";

    private final HouseRepository houseRepository;

    public boolean isMember() {
        return resolveRole().isPresent();
    }

    public boolean isOwner() {
        return resolveRole().filter(role -> role == HouseRole.OWNER).isPresent();
    }

    /**
     * True if {@code resourceHouseId} (the house actually recorded on a fetched
     * resource) matches the request's asserted {@code X-RCPLS-House-Id} header.
     * {@link #isMember()}/{@link #isOwner()} only prove the caller belongs to
     * <em>some</em> house named by the header; callers must additionally check this
     * once a resource is loaded, so a member of house A can't reach into house B's
     * data by sending B's id in the path while asserting A's id in the header.
     */
    public boolean belongsToHeaderHouse(final String resourceHouseId) {
        final var houseId = houseIdHeader();
        return houseId != null && houseId.equals(resourceHouseId);
    }

    /** As {@link #belongsToHeaderHouse(String)}, taking the fetched resource directly. */
    public boolean belongsToHeaderHouse(final HouseScoped resource) {
        return belongsToHeaderHouse(resource.houseId());
    }

    /**
     * Returns the {@code X-RCPLS-House-Id} header value. Only safe to call after
     * {@link #isMember()}/{@link #isOwner()} has already passed for this request.
     */
    public String requireHouseId() {
        final var houseId = houseIdHeader();
        if (houseId == null) {
            throw new IllegalStateException("No " + HOUSE_HEADER + " header on the current request");
        }
        return houseId;
    }

    /**
     * Returns the {@code X-RCPLS-House-Id} header value, or null if absent — for
     * endpoints (like public recipe browsing) that work fine with no active house and
     * don't gate access on one via {@code @PreAuthorize}.
     */
    public String currentHouseIdOrNull() {
        return houseIdHeader();
    }

    /**
     * An {@link ApiKeyAuthenticationToken} carries its own house/role assignment — that
     * assignment <em>is</em> the authorization, not a lookup key into {@link HouseRepository} —
     * so it's resolved directly rather than via {@link #currentUserId()}/{@code roleOf}. Still
     * requires the request's house header to match the key's own house, mirroring the
     * {@link #belongsToHeaderHouse} defense used elsewhere.
     */
    private Optional<HouseRole> resolveRole() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof ApiKeyAuthenticationToken apiKeyAuthentication) {
            final var principal = apiKeyAuthentication.getPrincipal();
            return principal.houseId().equals(houseIdHeader()) ? Optional.of(principal.role()) : Optional.empty();
        }

        final var houseId = houseIdHeader();
        final var userId = currentUserId();
        if (houseId == null || userId == null) {
            return Optional.empty();
        }
        return houseRepository.roleOf(houseId, userId);
    }

    private String currentUserId() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() ? authentication.getName() : null;
    }

    private String houseIdHeader() {
        final var attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }
        return servletAttributes.getRequest().getHeader(HOUSE_HEADER);
    }
}
