package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.model.Email;
import org.reciplease.model.HouseDocument;
import org.reciplease.model.HouseMembership;
import org.reciplease.model.HouseRole;
import org.reciplease.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

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
        var house = mongoTemplate.save(HouseDocument.builder().name("Empty House").createdAt(Instant.now()).build());

        var members = houseRepository.members(house.getId());

        assertThat(members, empty());
    }

    @Test
    void membersResolvesEmailsAndSortsOwnersFirstThenByEmail() {
        userRepository.save(new User("owner-id", "owner@example.com"));
        userRepository.save(new User("zed-id", "zed@example.com"));
        userRepository.save(new User("amy-id", "amy@example.com"));

        var house = mongoTemplate.save(HouseDocument.builder()
                .name("Test House")
                .createdAt(Instant.now())
                .members(Map.of(
                        "zed-id", "READ_ONLY",
                        "amy-id", "READ_ONLY",
                        "owner-id", "OWNER"))
                .build());

        var members = houseRepository.members(house.getId());

        assertThat(members, contains(
                new HouseMembership("owner-id", new Email("owner@example.com"), HouseRole.OWNER),
                new HouseMembership("amy-id", new Email("amy@example.com"), HouseRole.READ_ONLY),
                new HouseMembership("zed-id", new Email("zed@example.com"), HouseRole.READ_ONLY)));
    }

    @Test
    void membersFallsBackToTheUserIdWhenNoUserRecordExists() {
        var house = mongoTemplate.save(HouseDocument.builder()
                .name("Test House")
                .createdAt(Instant.now())
                .members(Map.of("unknown-id", "OWNER"))
                .build());

        var members = houseRepository.members(house.getId());

        assertThat(members, contains(new HouseMembership("unknown-id", new Email("unknown-id"), HouseRole.OWNER)));
    }

    @Test
    void addMemberThenMembersReflectsTheUpdatedRole() {
        userRepository.save(new User("user-1", "user1@example.com"));
        var house = mongoTemplate.save(HouseDocument.builder().name("House").createdAt(Instant.now()).build());

        houseRepository.addMember(house.getId(), "user-1", HouseRole.READ_ONLY);
        houseRepository.addMember(house.getId(), "user-1", HouseRole.OWNER);

        var members = houseRepository.members(house.getId());

        assertThat(members, contains(new HouseMembership("user-1", new Email("user1@example.com"), HouseRole.OWNER)));
    }
}
