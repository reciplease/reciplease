package org.reciplease.repository;

import java.util.List;
import java.util.Optional;
import org.reciplease.model.House;
import org.reciplease.model.HouseMembership;
import org.reciplease.model.HouseRole;

/**
 * Stores houses and the Google-subject-id-to-role membership relationship for each one.
 * Membership is never loaded onto a {@link House} instance; it is queried and mutated
 * directly here.
 */
public interface HouseRepository {
    Optional<House> findById(String id);

    List<House> findAllForUser(String userId);

    Optional<HouseRole> roleOf(String houseId, String userId);

    void addMember(String houseId, String userId, HouseRole role);

    void removeMember(String houseId, String userId);

    /**
     * All members of {@code houseId}, resolved against their user records and sorted
     * owners-first then alphabetically by email within each role.
     */
    List<HouseMembership> members(String houseId);
}
