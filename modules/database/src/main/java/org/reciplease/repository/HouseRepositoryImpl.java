package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.House;
import org.reciplease.model.HouseDocument;
import org.reciplease.model.HouseMembership;
import org.reciplease.model.HouseRole;
import org.reciplease.model.UserDocument;
import org.reciplease.repository.mongo.HouseMongoRepository;
import org.reciplease.repository.mongo.UserMongoRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class HouseRepositoryImpl implements HouseRepository {

    private final HouseMongoRepository houseMongoRepository;
    private final UserMongoRepository userMongoRepository;
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

    @Override
    public List<HouseMembership> members(final String houseId) {
        final var document = houseMongoRepository.findById(houseId).orElse(null);
        if (document == null || document.getMembers() == null || document.getMembers().isEmpty()) {
            return List.of();
        }

        final Map<String, String> members = document.getMembers();
        final Map<String, UserDocument> usersById = userMongoRepository.findAllById(members.keySet()).stream()
                .collect(Collectors.toMap(UserDocument::getId, user -> user));

        return members.entrySet().stream()
                .map(entry -> {
                    final var user = usersById.get(entry.getKey());
                    final var handle = user != null ? user.getHandle() : null;
                    return new HouseMembership(entry.getKey(), handle, HouseRole.valueOf(entry.getValue()));
                })
                .sorted(Comparator.comparing(HouseMembership::role)
                        .thenComparing(membership -> membership.userId()))
                .collect(Collectors.toList());
    }
}
