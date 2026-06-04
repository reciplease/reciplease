package org.reciplease.repository;

import org.reciplease.model.Measure;

import java.util.List;
import java.util.Optional;

public interface MeasureRepository {
    List<Measure> findAll();

    Optional<Measure> findById(String measureId);

    Measure save(Measure measure);

    List<Measure> saveAll(List<Measure> measures);
}
