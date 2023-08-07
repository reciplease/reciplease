package org.reciplease.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.reciplease.model.Ingredient;
import org.reciplease.model.Measure;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_EMPTY)
public class IngredientDto {

    UUID uuid;

    @NotNull
    @NotBlank
    String name;

    @NotNull
    Measure measure;

    public static IngredientDto from(Ingredient ingredient) {
        return IngredientDto.builder()
                .uuid(ingredient.getId())
                .name(ingredient.getName())
                .measure(ingredient.getMeasure())
                .build();
    }

    public Ingredient toModel() {
        return Ingredient.builder()
                .id(getUuid())
                .name(getName())
                .measure(getMeasure())
                .build();
    }
}
