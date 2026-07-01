package org.reciplease.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.InventoryAllocation;
import org.reciplease.model.PlannedIngredient;
import org.reciplease.model.RecipeIngredient;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

class PlannedIngredientDtoTest {

    @Test
    @DisplayName("create DTO from entity")
    void fromEntity() {
        var bread = new RecipeIngredient("bread", "ITEMS", 2d);
        var item = new PlannedIngredient(bread, List.of(new InventoryAllocation("item-1", "111", 2d)));

        var dto = PlannedIngredientDto.from(item);

        assertThat(dto.getIngredient(), is(RecipeIngredientDto.from(bread)));
        assertThat(dto.getAllocations(), contains(InventoryAllocationDto.from(new InventoryAllocation("item-1", "111", 2d))));
    }

    @Test
    @DisplayName("round-trips to model")
    void toModel() {
        var dto = PlannedIngredientDto.builder()
                .ingredient(RecipeIngredientDto.builder().name("bread").measure("item").amount(2d).build())
                .allocations(List.of(InventoryAllocationDto.builder().inventoryItemId("item-1").amount(2d).build()))
                .build();

        var item = dto.toModel();

        assertThat(item.ingredient(), is(new RecipeIngredient("bread", "item", 2d)));
        assertThat(item.allocations(), contains(new InventoryAllocation("item-1", null, 2d)));
    }

    @Test
    @DisplayName("round-trips to model with no allocations")
    void toModelWithoutAllocations() {
        var dto = PlannedIngredientDto.builder()
                .ingredient(RecipeIngredientDto.builder().name("bread").measure("item").amount(2d).build())
                .build();

        var item = dto.toModel();

        assertThat(item.allocations(), is(empty()));
    }
}
