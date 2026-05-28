package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document("ingredients")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDocument {

    @Id
    private UUID uuid;
    private String name;
    private Measure measure;

    public static IngredientDocument from(final Ingredient ingredient) {
        return IngredientDocument.builder()
                .uuid(ingredient.getUuid() != null ? ingredient.getUuid() : UUID.randomUUID())
                .name(ingredient.getName())
                .measure(ingredient.getMeasure())
                .build();
    }

    public Ingredient toModel() {
        return Ingredient.builder()
                .uuid(uuid)
                .name(name)
                .measure(measure)
                .build();
    }
}
