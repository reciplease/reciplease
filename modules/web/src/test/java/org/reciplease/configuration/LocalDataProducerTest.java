package org.reciplease.configuration;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.Ingredient;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.Recipe;
import org.reciplease.repository.IngredientRepository;
import org.reciplease.repository.InventoryRepository;
import org.reciplease.repository.RecipeRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings
public class LocalDataProducerTest {
    @Mock
    private IngredientRepository ingredientRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private RecipeRepository recipeRepository;
    @InjectMocks
    private LocalDataProducer localDataProducer;

    @Test
    public void shouldSaveIngredients() {
        final var mockIngredients = List.of(
                Ingredient.builder().build(),
                Ingredient.builder().build(),
                Ingredient.builder().build()
        );
        when(ingredientRepository.saveAll(anyList())).thenReturn(mockIngredients);
        when(recipeRepository.save(any())).thenReturn(Recipe.builder().build());

        localDataProducer.run(null);

        verify(inventoryRepository).save(any(InventoryItem.class));
    }
}
