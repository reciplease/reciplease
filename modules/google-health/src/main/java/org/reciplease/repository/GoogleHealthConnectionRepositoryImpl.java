package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.GoogleHealthConnection;
import org.reciplease.model.GoogleHealthConnectionDocument;
import org.reciplease.repository.mongo.GoogleHealthConnectionMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GoogleHealthConnectionRepositoryImpl implements GoogleHealthConnectionRepository {
    private final GoogleHealthConnectionMongoRepository googleHealthConnectionMongoRepository;

    @Override
    public Optional<GoogleHealthConnection> findByUserId(final String userId) {
        return googleHealthConnectionMongoRepository.findById(userId).map(GoogleHealthConnectionDocument::toModel);
    }

    @Override
    public GoogleHealthConnection save(final GoogleHealthConnection connection) {
        return googleHealthConnectionMongoRepository.save(GoogleHealthConnectionDocument.from(connection)).toModel();
    }

    @Override
    public void deleteByUserId(final String userId) {
        googleHealthConnectionMongoRepository.deleteById(userId);
    }
}
