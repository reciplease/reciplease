package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.model.Measure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

@DataMongoTest
@Import(MeasureRepositoryImpl.class)
class MeasureRepositoryTest {
    @Autowired
    private MeasureRepository measureRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    private final Measure grams = Measure.builder().measureId("GRAMS").singular("gram").plural("grams").build();
    private final Measure items = Measure.builder().measureId("ITEMS").singular("item").plural("items").build();

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Test
    void shouldSaveAndFindAllMeasures() {
        measureRepository.saveAll(List.of(grams, items));

        assertThat(measureRepository.findAll(), containsInAnyOrder(grams, items));
    }

    @Test
    void shouldFindMeasureById() {
        measureRepository.saveAll(List.of(grams, items));

        assertThat(measureRepository.findById("GRAMS"), is(Optional.of(grams)));
    }

    @Test
    void shouldReturnEmptyForUnknownId() {
        measureRepository.saveAll(List.of(grams));

        assertThat(measureRepository.findById("SPOONFULS").isEmpty(), is(true));
    }

    @Test
    void shouldReturnSavedMeasures() {
        assertThat(measureRepository.saveAll(List.of(grams)), contains(grams));
    }
}
