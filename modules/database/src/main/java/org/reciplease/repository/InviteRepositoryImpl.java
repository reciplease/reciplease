package org.reciplease.repository;

import static java.util.stream.Collectors.toList;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.reciplease.model.Invite;
import org.reciplease.model.InviteDocument;
import org.reciplease.repository.mongo.InviteMongoRepository;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

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

    @Override
    public Invite create(final Invite invite) {
        return inviteMongoRepository.save(InviteDocument.from(invite)).toModel();
    }

    @Override
    public List<Invite> findAllForHouse(final String houseId) {
        return inviteMongoRepository.findAllByHouseId(houseId).stream()
                .map(InviteDocument::toModel)
                .collect(toList());
    }

    @Override
    public Optional<Invite> findById(final String id) {
        return inviteMongoRepository.findById(id).map(InviteDocument::toModel);
    }

    @Override
    public void delete(final String id) {
        inviteMongoRepository.deleteById(id);
    }
}
