package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.House;
import org.reciplease.model.HouseDocument;
import org.reciplease.model.HouseRole;
import org.reciplease.repository.mongo.HouseMongoRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class HouseRepositoryImpl implements HouseRepository {

    private final HouseMongoRepository houseMongoRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public Optional<House> findById(final String id) {
        return houseMongoRepository.findById(id).map(HouseDocument::toModel);
    }

    @Override
    public List<House> findAllForUser(final String userId) {
        final var query = query(where("members." + userId).exists(true));
        return mongoTemplate.find(query, HouseDocument.class).stream()
                .map(HouseDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<HouseRole> roleOf(final String houseId, final String userId) {
        final var query = query(where("_id").is(houseId));
        query.fields().include("members." + userId);

        final var document = mongoTemplate.findOne(query, HouseDocument.class);
        if (document == null || document.getMembers() == null) {
            return Optional.empty();
        }

        final var role = document.getMembers().get(userId);
        return role == null ? Optional.empty() : Optional.of(HouseRole.valueOf(role));
    }

    @Override
    public void addMember(final String houseId, final String userId, final HouseRole role) {
        final Query query = query(where("_id").is(houseId));
        final Update update = new Update().set("members." + userId, role.name());
        mongoTemplate.updateFirst(query, update, HouseDocument.class);
    }
}
