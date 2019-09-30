package org.reciplease.configuration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reciplease.model.Ingredient;
import org.reciplease.repository.IngredientRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LocalDataProducerTest {
    @Mock
    private IngredientRepository ingredientRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSaveIngredients() {
        final LocalDataProducer localDataProducer = new LocalDataProducer(ingredientRepository);

        localDataProducer.run(null);

        verify(ingredientRepository, times(3)).save(any(Ingredient.class));
    }
}