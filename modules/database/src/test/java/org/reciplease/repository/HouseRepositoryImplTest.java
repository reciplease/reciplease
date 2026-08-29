package org.reciplease.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.model.HouseDocument;
import org.reciplease.model.HouseMembership;
import org.reciplease.model.HouseRole;
import org.reciplease.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

@DataMongoTest
@Import({HouseRepositoryImpl.class, UserRepositoryImpl.class})
class HouseRepositoryImplTest {

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Test
    void membersReturnsAnEmptyListForAHouseWithNoMembers() {
        var house = mongoTemplate.save(HouseDocument.builder()
                .name("Empty House")
                .createdAt(Instant.now())
                .build());

        var members = houseRepository.members(house.getId());

        assertThat(members, empty());
    }

    @Test
    void membersResolvesHandlesAndSortsOwnersFirstThenByUserId() {
        userRepository.save(new User("owner-id", "owner-handle"));
        userRepository.save(new User("zed-id", "zed-handle"));
        userRepository.save(new User("amy-id", "amy-handle"));

        var house = mongoTemplate.save(HouseDocument.builder()
                .name("Test House")
                .createdAt(Instant.now())
                .members(Map.of(
                        "zed-id", "READ_ONLY",
                        "amy-id", "READ_ONLY",
                        "owner-id", "OWNER"))
                .build());

        var members = houseRepository.members(house.getId());

        assertThat(
                members,
                contains(
                        new HouseMembership("owner-id", "owner-handle", HouseRole.OWNER),
                        new HouseMembership("amy-id", "amy-handle", HouseRole.READ_ONLY),
                        new HouseMembership("zed-id", "zed-handle", HouseRole.READ_ONLY)));
    }

    @Test
    void membersFallsBackToANullHandleWhenNoUserRecordExists() {
        var house = mongoTemplate.save(HouseDocument.builder()
                .name("Test House")
                .createdAt(Instant.now())
                .members(Map.of("unknown-id", "OWNER"))
                .build());

        var members = houseRepository.members(house.getId());

        assertThat(members, contains(new HouseMembership("unknown-id", null, HouseRole.OWNER)));
    }

    @Test
    void addMemberThenMembersReflectsTheUpdatedRole() {
        userRepository.save(new User("user-1", "user1-handle"));
        var house = mongoTemplate.save(
                HouseDocument.builder().name("House").createdAt(Instant.now()).build());

        houseRepository.addMember(house.getId(), "user-1", HouseRole.READ_ONLY);
        houseRepository.addMember(house.getId(), "user-1", HouseRole.OWNER);

        var members = houseRepository.members(house.getId());

        assertThat(members, contains(new HouseMembership("user-1", "user1-handle", HouseRole.OWNER)));
    }

    @Test
    void removeMemberDropsThatMemberButLeavesOthers() {
        userRepository.save(new User("user-1", "user1-handle"));
        userRepository.save(new User("user-2", "user2-handle"));
        var house = mongoTemplate.save(
                HouseDocument.builder().name("House").createdAt(Instant.now()).build());
        houseRepository.addMember(house.getId(), "user-1", HouseRole.OWNER);
        houseRepository.addMember(house.getId(), "user-2", HouseRole.READ_ONLY);

        houseRepository.removeMember(house.getId(), "user-2");

        var members = houseRepository.members(house.getId());
        assertThat(members, contains(new HouseMembership("user-1", "user1-handle", HouseRole.OWNER)));
        assertThat(houseRepository.roleOf(house.getId(), "user-2").isPresent(), is(false));
    }
}
