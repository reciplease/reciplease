package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.ShoppingList;

@Value
@AllArgsConstructor
@Builder
@Schema(name = "ShoppingList")
public class ShoppingListDto {

    @Schema(requiredMode = REQUIRED)
    List<RecipeIngredientDto> items;

    public static ShoppingListDto from(final ShoppingList shoppingList) {
        return ShoppingListDto.builder()
                .items(shoppingList.getItems().stream()
                        .map(RecipeIngredientDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
