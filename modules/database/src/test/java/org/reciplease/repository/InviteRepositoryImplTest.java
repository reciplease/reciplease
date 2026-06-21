package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.model.HouseRole;
import org.reciplease.model.Invite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@DataMongoTest
@Import(InviteRepositoryImpl.class)
class InviteRepositoryImplTest {

    @Autowired
    private InviteRepository inviteRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Test
    void createPersistsANewInvite() {
        var invite = new Invite(null, "abc123", "house-1", HouseRole.OWNER, Instant.now(), null, null);

        var created = inviteRepository.create(invite);

        assertThat(created.id(), is(notNullValue()));
        assertThat(inviteRepository.findById(created.id()).orElseThrow().code(), is("abc123"));
    }

    @Test
    void findAllForHouseReturnsOnlyThatHousesInvites() {
        inviteRepository.create(new Invite(null, "code1", "house-1", HouseRole.OWNER, Instant.now(), null, null));
        inviteRepository.create(new Invite(null, "code2", "house-1", HouseRole.READ_ONLY, Instant.now(), null, null));
        inviteRepository.create(new Invite(null, "code3", "house-2", HouseRole.OWNER, Instant.now(), null, null));

        var invites = inviteRepository.findAllForHouse("house-1");

        assertThat(invites.stream().map(Invite::code).toList(), containsInAnyOrder("code1", "code2"));
    }

    @Test
    void deleteRemovesTheInvite() {
        var created = inviteRepository.create(new Invite(null, "code1", "house-1", HouseRole.OWNER, Instant.now(), null, null));

        inviteRepository.delete(created.id());

        assertThat(inviteRepository.findById(created.id()).isEmpty(), is(true));
        assertThat(inviteRepository.findAllForHouse("house-1"), empty());
    }
}
