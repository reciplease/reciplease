package org.reciplease.repository;

import org.reciplease.model.Measure;

import java.util.List;

public interface MeasureRepository {
    List<Measure> findAll();

    List<Measure> saveAll(List<Measure> measures);
}
