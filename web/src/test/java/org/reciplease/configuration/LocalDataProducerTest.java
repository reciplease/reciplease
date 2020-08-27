package org.reciplease.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reciplease.model.Ingredient;
import org.reciplease.model.InventoryItem;
import org.reciplease.repository.IngredientRepository;
import org.reciplease.repository.InventoryRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LocalDataProducerTest {
    @Mock
    private IngredientRepository ingredientRepository;
    @Mock
    private InventoryRepository inventoryRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSaveIngredients() {
        final LocalDataProducer localDataProducer = new LocalDataProducer(ingredientRepository, inventoryRepository);
        when(ingredientRepository.saveAll(anyList())).thenReturn(List.of(new Ingredient(), new Ingredient(), new Ingredient()));

        localDataProducer.run(null);

        verify(ingredientRepository).saveAll(anyList());
        verify(inventoryRepository).save(any(InventoryItem.class));
    }
}