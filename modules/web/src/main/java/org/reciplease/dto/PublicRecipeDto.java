package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.reciplease.model.Recipe;

/**
 * The public view of a recipe: safe for anonymous/public browsing and for authenticated
 * callers who aren't the recipe's own owner. Also the request body shape for
 * create/update — the server derives ownerId/createdBy/updatedBy itself, so a client never
 * submits them.
 */
@Value
@AllArgsConstructor
@Builder
@Schema(name = "PublicRecipe")
public class PublicRecipeDto implements RecipeDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
    String recipeId;
    // Without an explicit name, Jackson's getter-based property naming maps the
    // isPublic() accessor to JSON key "public" on serialization, while the
    // constructor parameter is named "isPublic" — pin both to "isPublic" so
    // round-tripping doesn't silently drop the field or fail deserialization.
    // The override has to live on the generated getter (not just the field) or
    // swagger-core's schema scan picks up both names and emits the property twice.
    @Getter(onMethod_ = @JsonProperty("isPublic"))
    @Builder.Default
    @Schema(requiredMode = REQUIRED)
    boolean isPublic = false;

    @Size(max = 200)
    @Schema(requiredMode = REQUIRED, maxLength = 200)
    String name;

    @Schema(requiredMode = REQUIRED)
    String description;

    @Pattern(regexp = "^$|^https?://.+")
    @Schema(requiredMode = REQUIRED, pattern = "^$|^https?://.+")
    String sourceUrl;

    @Schema(requiredMode = REQUIRED)
    List<String> steps;

    // Not read-only: RecipeController.update/RecipeService#update genuinely reads this from
    // the request body and overwrites the recipe's ingredients with it — omitting it on an
    // update would silently wipe them (Recipe#recipeIngredients defaults to an empty set), so
    // it stays required on both directions rather than being marked server-generated-only.
    @Schema(requiredMode = REQUIRED)
    Set<RecipeIngredientDto> ingredients;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
    Instant updatedAt;

    // Jackson doesn't reliably pick up @JsonProperty from a default interface method, so
    // this override — and the matching one on OwnedRecipeDto — restates it explicitly. It's
    // also the EXISTING_PROPERTY discriminant for the RecipeDto polymorphic union (see
    // RecipeDto's @JsonTypeInfo).
    @Override
    @JsonProperty("owned")
    public boolean isOwned() {
        return false;
    }

    public static PublicRecipeDto from(final Recipe recipe) {
        return PublicRecipeDto.builder()
                .recipeId(recipe.id())
                .isPublic(recipe.isPublic())
                .name(recipe.name())
                .description(recipe.description())
                .sourceUrl(recipe.sourceUrl())
                .steps(recipe.steps())
                .ingredients(recipe.recipeIngredients().stream()
                        .map(RecipeIngredientDto::from)
                        .collect(Collectors.toSet()))
                .updatedAt(recipe.updatedAt())
                .build();
    }
}
