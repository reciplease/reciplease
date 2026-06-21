package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.Invite;
import org.reciplease.model.InviteDocument;
import org.reciplease.repository.mongo.InviteMongoRepository;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class InviteRepositoryImpl implements InviteRepository {

    private final InviteMongoRepository inviteMongoRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public Optional<Invite> findByCode(final String code) {
        return inviteMongoRepository.findByCode(code).map(InviteDocument::toModel);
    }

    @Override
    public Optional<Invite> claim(final String code, final String userId) {
        final var query = query(where("code").is(code).and("usedAt").isNull());
        final var update = new Update().set("usedAt", Instant.now()).set("usedByUserId", userId);

        final InviteDocument claimed = mongoTemplate.findAndModify(
                query, update, FindAndModifyOptions.options().returnNew(true), InviteDocument.class);

        return Optional.ofNullable(claimed).map(InviteDocument::toModel);
    }
}
