package org.reciplease.repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.reciplease.model.RefreshTokenDocument;
import org.reciplease.model.RefreshTokenRecord;
import org.reciplease.repository.mongo.RefreshTokenMongoRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenMongoRepository refreshTokenMongoRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public RefreshTokenRecord save(final RefreshTokenRecord record) {
        return refreshTokenMongoRepository
                .save(RefreshTokenDocument.from(record))
                .toModel();
    }

    @Override
    public Optional<RefreshTokenRecord> findByPrefix(final String tokenPrefix) {
        return refreshTokenMongoRepository.findByTokenPrefix(tokenPrefix).map(RefreshTokenDocument::toModel);
    }

    @Override
    public void markUsed(final String id, final Instant usedAt) {
        mongoTemplate.updateFirst(
                query(where("_id").is(id)), new Update().set("usedAt", usedAt), RefreshTokenDocument.class);
    }

    @Override
    public void revokeFamily(final String familyId, final Instant revokedAt) {
        mongoTemplate.updateMulti(
                query(where("familyId").is(familyId)),
                new Update().set("revokedAt", revokedAt),
                RefreshTokenDocument.class);
    }

    @Override
    public void revokeAllForUser(final String userId, final Instant revokedAt) {
        mongoTemplate.updateMulti(
                query(where("userId").is(userId)),
                new Update().set("revokedAt", revokedAt),
                RefreshTokenDocument.class);
    }
}
