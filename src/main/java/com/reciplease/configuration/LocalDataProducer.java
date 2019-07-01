package com.reciplease.configuration;

import com.reciplease.model.Ingredient;
import com.reciplease.model.Measure;
import com.reciplease.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDataProducer implements ApplicationRunner {
    final IngredientRepository ingredientRepository;

    @Override
    public void run(final ApplicationArguments args) {
        ingredientRepository.save(new Ingredient("milk", Measure.MILLILITRES));
        ingredientRepository.save(new Ingredient("eggs", Measure.ITEMS));
        ingredientRepository.save(new Ingredient("bread", Measure.ITEMS));
    }
}