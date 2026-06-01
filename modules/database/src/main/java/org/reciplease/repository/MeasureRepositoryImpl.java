package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.Measure;
import org.reciplease.model.MeasureDocument;
import org.reciplease.repository.mongo.MeasureMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MeasureRepositoryImpl implements MeasureRepository {
    private final MeasureMongoRepository measureMongoRepository;

    @Override
    public List<Measure> findAll() {
        return measureMongoRepository.findAll().stream()
                .map(MeasureDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Measure> findById(final String measureId) {
        return measureMongoRepository.findById(measureId).map(MeasureDocument::toModel);
    }

    @Override
    public List<Measure> saveAll(final List<Measure> measures) {
        return measureMongoRepository.saveAll(
                measures.stream().map(MeasureDocument::from).collect(Collectors.toList())
        ).stream().map(MeasureDocument::toModel).collect(Collectors.toList());
    }
}
