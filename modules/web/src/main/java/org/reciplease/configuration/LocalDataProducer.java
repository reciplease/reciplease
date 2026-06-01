package org.reciplease.configuration;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.Ingredient;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.Measure;
import org.reciplease.model.Recipe;
import org.reciplease.repository.IngredientRepository;
import org.reciplease.repository.InventoryRepository;
import org.reciplease.repository.MeasureRepository;
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
    final MeasureRepository measureRepository;

    @Override
    public void run(final ApplicationArguments args) {
        measureRepository.saveAll(List.of(
                Measure.builder().measureId("ITEMS").singular("item").plural("items").build(),
                Measure.builder().measureId("PIECES").singular("piece").plural("pieces").build(),
                Measure.builder().measureId("KILOGRAMS").singular("kilogram").plural("kilograms").build(),
                Measure.builder().measureId("GRAMS").singular("gram").plural("grams").build(),
                Measure.builder().measureId("LITRES").singular("litre").plural("litres").build(),
                Measure.builder().measureId("MILLILITRES").singular("millilitre").plural("millilitres").build(),
                Measure.builder().measureId("tbsp").singular("tbsp").plural("tbsp").build(),
                Measure.builder().measureId("tsp").singular("tsp").plural("tsp").build()
        ));

        final var ingredients = ingredientRepository.saveAll(List.of(
                Ingredient.builder()
                        .name("milk")
                        .measure("MILLILITRES")
                        .build(),
                Ingredient.builder()
                        .name("eggs")
                        .measure("ITEMS")
                        .build(),
                Ingredient.builder()
                        .name("bread")
                        .measure("ITEMS")
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
                .description("A staple and classic")
                .steps(List.of(
                        "Toast the bread",
                        "Optionally: Wait for toast to cool down",
                        "Spread butter on toast"
                ))
                .build());

        toast.addIngredient(bread, 1d);

        recipeRepository.save(toast);
    }
}
