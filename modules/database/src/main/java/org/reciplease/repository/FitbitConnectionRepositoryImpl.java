package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.FitbitConnection;
import org.reciplease.model.FitbitConnectionDocument;
import org.reciplease.repository.mongo.FitbitConnectionMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FitbitConnectionRepositoryImpl implements FitbitConnectionRepository {
    private final FitbitConnectionMongoRepository fitbitConnectionMongoRepository;

    @Override
    public Optional<FitbitConnection> findByUserId(final String userId) {
        return fitbitConnectionMongoRepository.findById(userId).map(FitbitConnectionDocument::toModel);
    }

    @Override
    public FitbitConnection save(final FitbitConnection connection) {
        return fitbitConnectionMongoRepository.save(FitbitConnectionDocument.from(connection)).toModel();
    }

    @Override
    public void deleteByUserId(final String userId) {
        fitbitConnectionMongoRepository.deleteById(userId);
    }
}
