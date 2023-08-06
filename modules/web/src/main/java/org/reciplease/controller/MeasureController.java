package org.reciplease.controller;

import org.reciplease.model.Measure;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/measures")
public class MeasureController {

    @GetMapping
    public ResponseEntity<List<Measure>> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(List.of(Measure.values()));
    }
}
