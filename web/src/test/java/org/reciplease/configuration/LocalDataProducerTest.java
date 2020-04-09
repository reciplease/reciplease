package org.reciplease.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reciplease.repository.IngredientRepository;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;

public class LocalDataProducerTest {
    @Mock
    private IngredientRepository ingredientRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSaveIngredients() {
        final LocalDataProducer localDataProducer = new LocalDataProducer(ingredientRepository);

        localDataProducer.run(null);

        verify(ingredientRepository).saveAll(anyList());
    }
}