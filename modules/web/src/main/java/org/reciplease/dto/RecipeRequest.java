package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.reciplease.model.Recipe;

@Value
@AllArgsConstructor
@Builder
@Schema(name = "RecipeRequest")
public class RecipeRequest {

    @Size(max = 200)
    @Schema(requiredMode = REQUIRED, maxLength = 200)
    String name;

    @Schema(requiredMode = REQUIRED)
    String description;

    @Pattern(regexp = "^$|^https?://.+")
    @Schema(requiredMode = REQUIRED, pattern = "^$|^https?://.+")
    String sourceUrl;

    @Getter(onMethod_ = @JsonProperty("isPublic"))
    @Builder.Default
    @Schema(requiredMode = REQUIRED)
    boolean isPublic = false;

    @PositiveOrZero
    @Schema(nullable = true)
    Integer prepTimeMinutes;

    @PositiveOrZero
    @Schema(nullable = true)
    Integer cookTimeMinutes;

    @Positive
    @Schema(nullable = true)
    Integer servings;

    @Schema(requiredMode = REQUIRED)
    List<String> steps;

    @Schema(requiredMode = REQUIRED)
    Set<RecipeIngredientDto> ingredients;

    public Recipe toEntity() {
        final var builder = Recipe.builder()
                .isPublic(isPublic)
                .name(name)
                .description(description)
                .sourceUrl(sourceUrl)
                .prepTimeMinutes(prepTimeMinutes)
                .cookTimeMinutes(cookTimeMinutes)
                .servings(servings)
                .steps(steps);

        if (ingredients != null) {
            builder.recipeIngredients(
                    ingredients.stream().map(RecipeIngredientDto::toModel).collect(Collectors.toSet()));
        }

        return builder.build();
    }
}
