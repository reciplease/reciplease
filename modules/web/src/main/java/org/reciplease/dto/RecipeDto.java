package org.reciplease.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.reciplease.model.Recipe;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "owned",
        visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PublicRecipeDto.class, name = "false"),
    @JsonSubTypes.Type(value = OwnedRecipeDto.class, name = "true")
})
// swagger-core would otherwise leave the discriminator mapping's values ("true"/"false", from
// @JsonSubTypes' names) unmapped to the differently-named PublicRecipe/OwnedRecipe schemas —
// spell the mapping out explicitly so generated clients (e.g. openapi-typescript) can narrow
// the union correctly.
@Schema(
        discriminatorProperty = "owned",
        discriminatorMapping = {
            @DiscriminatorMapping(value = "false", schema = PublicRecipeDto.class),
            @DiscriminatorMapping(value = "true", schema = OwnedRecipeDto.class)
        })
public sealed interface RecipeDto permits PublicRecipeDto, OwnedRecipeDto {

    String getRecipeId();

    boolean isPublic();

    String getName();

    String getDescription();

    String getSourceUrl();

    List<String> getSteps();

    Set<RecipeIngredientDto> getIngredients();

    Instant getUpdatedAt();

    /** True for {@link OwnedRecipeDto} — the discriminant clients narrow the union on. */
    default boolean isOwned() {
        return false;
    }

    /** Public view: no owner or user info. */
    static RecipeDto from(final Recipe recipe) {
        return PublicRecipeDto.from(recipe);
    }

    /** Owner view, for the recipe's owner only. */
    static RecipeDto from(final Recipe recipe, final UserSummaryDto createdBy, final UserSummaryDto updatedBy) {
        return OwnedRecipeDto.from(recipe, createdBy, updatedBy);
    }

    default Recipe toEntity() {
        final var builder = Recipe.builder()
                .id(getRecipeId())
                .isPublic(isPublic())
                .name(getName())
                .description(getDescription())
                .sourceUrl(getSourceUrl())
                .steps(getSteps());

        if (getIngredients() != null) {
            builder.recipeIngredients(
                    getIngredients().stream().map(RecipeIngredientDto::toModel).collect(Collectors.toSet()));
        }

        return builder.build();
    }
}
