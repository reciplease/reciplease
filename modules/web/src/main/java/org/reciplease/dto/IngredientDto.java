package org.reciplease.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.reciplease.model.Ingredient;
import org.reciplease.model.Measure;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_EMPTY)
public class IngredientDto {
    @Nullable
    UUID uuid;

    @NotNull
    @NotBlank
    String name;

    @NotNull
    Measure measure;

    public static IngredientDto from(Ingredient ingredient) {
        return IngredientDto.builder()
            .uuid(ingredient.getUuid())
            .name(ingredient.getName())
            .measure(ingredient.getMeasure())
            .build();
    }

    public Ingredient toModel() {
        return Ingredient.builder()
            .uuid(getUuid())
            .name(getName())
            .measure(getMeasure())
            .build();
    }
}
