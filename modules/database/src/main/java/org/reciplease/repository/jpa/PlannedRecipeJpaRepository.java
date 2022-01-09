package org.reciplease.repository.jpa;

import org.reciplease.model.PlannedRecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlannedRecipeJpaRepository extends JpaRepository<PlannedRecipeEntity, UUID> {
}
