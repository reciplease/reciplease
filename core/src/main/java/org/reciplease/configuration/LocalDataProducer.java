package org.reciplease.configuration;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.Ingredient;
import org.reciplease.model.Measure;
import org.reciplease.repository.IngredientRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDataProducer implements ApplicationRunner {
    final IngredientRepository ingredientRepository;

    @Override
    public void run(final ApplicationArguments args) {
        List<Ingredient> ingredients = List.of(
                Ingredient.builder()
                        .name("milk")
                        .measure(Measure.MILLILITRES)
                        .build(),
                Ingredient.builder()
                        .name("eggs")
                        .measure(Measure.ITEMS)
                        .build(),
                Ingredient.builder()
                        .name("bread")
                        .measure(Measure.ITEMS)
                        .build()
        );

        ingredientRepository.saveAll(ingredients);
    }
}
