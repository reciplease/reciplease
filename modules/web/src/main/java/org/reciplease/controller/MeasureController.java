package org.reciplease.controller;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.Measure;
import org.reciplease.repository.MeasureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/measures")
@RequiredArgsConstructor
public class MeasureController {

    private final MeasureRepository measureRepository;

    @GetMapping
    public ResponseEntity<List<Measure>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(measureRepository.findAll());
    }

    @GetMapping("{measureId}")
    public ResponseEntity<Measure> findById(@PathVariable final String measureId) {
        return ResponseEntity.of(measureRepository.findById(measureId));
    }
}
