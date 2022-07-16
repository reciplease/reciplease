package org.reciplease.repository.jpa;

import org.reciplease.model.PlannedRecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PlannedRecipeJpaRepository extends JpaRepository<PlannedRecipeEntity, UUID> {
    List<PlannedRecipeEntity> findByDateIsBetween(LocalDate start, LocalDate end);
}
