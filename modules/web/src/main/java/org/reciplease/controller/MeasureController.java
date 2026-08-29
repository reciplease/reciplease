package org.reciplease.controller;

import java.util.List;
import org.reciplease.dto.MeasureDto;
import org.reciplease.model.Measure;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the static {@link Measure} catalog. Measures are reference data baked into the
 * enum, so there is no create endpoint and no persistence.
 */
@RestController
@RequestMapping("api/measures")
public class MeasureController {

    @GetMapping
    public List<MeasureDto> findAll() {
        return Measure.all().stream().map(MeasureDto::new).toList();
    }

    @GetMapping("{measureId}")
    public ResponseEntity<MeasureDto> findById(@PathVariable final String measureId) {
        return ResponseEntity.of(Measure.fromId(measureId).map(MeasureDto::new));
    }
}
