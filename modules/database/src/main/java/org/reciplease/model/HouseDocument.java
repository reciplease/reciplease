package org.reciplease.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * {@code members} (Google subject id -> role name) is storage-only: it is read and
 * written directly by {@code HouseRepositoryImpl} via {@code MongoTemplate} and is
 * never surfaced through {@link #toModel()} onto the {@link House} domain model.
 */
@Document("houses")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HouseDocument {

    @Id
    private String id;

    private String name;

    @Builder.Default
    private Map<String, String> members = new HashMap<>();

    @CreatedDate
    private Instant createdAt;

    public static HouseDocument from(final House house) {
        return HouseDocument.builder()
                .id(house.id())
                .name(house.name())
                .createdAt(house.createdAt())
                .build();
    }

    public House toModel() {
        return new House(id, name, createdAt);
    }
}
