package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.AllowlistDocument;
import org.reciplease.repository.mongo.AllowlistMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AllowlistRepositoryImpl implements AllowlistRepository {
    private final AllowlistMongoRepository allowlistMongoRepository;

    @Override
    public boolean contains(final String email) {
        return allowlistMongoRepository.existsById(normalize(email));
    }

    @Override
    public List<String> findAll() {
        return allowlistMongoRepository.findAll().stream()
                .map(AllowlistDocument::getEmail)
                .collect(Collectors.toList());
    }

    @Override
    public void add(final String email) {
        allowlistMongoRepository.save(AllowlistDocument.builder().email(normalize(email)).build());
    }

    @Override
    public void remove(final String email) {
        allowlistMongoRepository.deleteById(normalize(email));
    }

    private static String normalize(final String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
