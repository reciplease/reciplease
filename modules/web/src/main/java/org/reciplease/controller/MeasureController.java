package org.reciplease.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.reciplease.model.Measure;
import org.reciplease.repository.MeasureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/measures")
@RequiredArgsConstructor
public class MeasureController {

    private final MeasureRepository measureRepository;

    @Data
    static class CreateMeasureRequest {
        @NotNull @NotBlank String singular;
        @NotNull @NotBlank String plural;
    }

    @PostMapping
    public ResponseEntity<Measure> create(@Valid @RequestBody final CreateMeasureRequest request) {
        final Measure measure = Measure.builder()
                .singular(request.getSingular())
                .plural(request.getPlural())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(measureRepository.save(measure));
    }

    @GetMapping
    public ResponseEntity<List<Measure>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(measureRepository.findAll());
    }

    @GetMapping("{measureId}")
    public ResponseEntity<Measure> findById(@PathVariable final String measureId) {
        return ResponseEntity.of(measureRepository.findById(measureId));
    }
}
