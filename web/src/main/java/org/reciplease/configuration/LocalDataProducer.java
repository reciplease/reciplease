package org.reciplease.configuration;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.Ingredient;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.Measure;
import org.reciplease.repository.IngredientRepository;
import org.reciplease.repository.InventoryRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDataProducer implements ApplicationRunner {
    final IngredientRepository ingredientRepository;
    final InventoryRepository inventoryRepository;

    @Override
    public void run(final ApplicationArguments args) {
        final var ingredients = ingredientRepository.saveAll(List.of(
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
        ));

        inventoryRepository.save(InventoryItem.builder()
                .ingredient(ingredients.get(2))
                .amount(2d)
                .expiration(LocalDate.of(2020, Month.JANUARY, 1))
                .build());
    }
}
