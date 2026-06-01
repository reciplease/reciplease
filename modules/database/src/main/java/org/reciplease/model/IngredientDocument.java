package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("ingredients")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDocument {

    @Id
    private String id;
    private String name;
    private String measure;

    public static IngredientDocument from(final Ingredient ingredient) {
        return IngredientDocument.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .measure(ingredient.getMeasure())
                .build();
    }

    public Ingredient toModel() {
        return Ingredient.builder()
                .id(id)
                .name(name)
                .measure(measure)
                .build();
    }
}
