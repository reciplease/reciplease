package org.reciplease.repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.reciplease.model.WebAuthnChallengeDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WebAuthnChallengeLedgerImpl implements WebAuthnChallengeLedger {

    private final MongoTemplate mongoTemplate;

    @Override
    public void issue(final String challenge, final String userId) {
        mongoTemplate.save(
                WebAuthnChallengeDocument.builder().id(challenge).userId(userId).build());
    }

    @Override
    public Optional<String> consumeForRegistration(final String challenge) {
        return Optional.ofNullable(remove(challenge)).map(WebAuthnChallengeDocument::getUserId);
    }

    @Override
    public boolean consumeForLogin(final String challenge) {
        return remove(challenge) != null;
    }

    private WebAuthnChallengeDocument remove(final String challenge) {
        return mongoTemplate.findAndRemove(query(where("_id").is(challenge)), WebAuthnChallengeDocument.class);
    }
}
