package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.User;
import org.reciplease.model.UserDocument;
import org.reciplease.repository.mongo.UserMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserMongoRepository userMongoRepository;

    @Override
    public User save(final User user) {
        return userMongoRepository.save(UserDocument.from(user)).toModel();
    }
}
