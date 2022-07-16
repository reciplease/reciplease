package org.reciplease.configuration;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.Ingredient;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.Measure;
import org.reciplease.model.Recipe;
import org.reciplease.repository.IngredientRepository;
import org.reciplease.repository.InventoryRepository;
import org.reciplease.repository.RecipeRepository;
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
    final RecipeRepository recipeRepository;

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

        final var bread = ingredients.get(2);

        inventoryRepository.save(InventoryItem.builder()
                .ingredient(bread)
                .amount(2d)
                .expiration(LocalDate.of(2020, Month.JANUARY, 1))
                .build());

        final var toast = recipeRepository.save(Recipe.builder()
                .name("Toast")
                .build());

        toast.addIngredient(bread, 1d);

        recipeRepository.save(toast);
    }
}
