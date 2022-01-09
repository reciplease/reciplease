package org.reciplease.repository.jpa;

import org.reciplease.model.IngredientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IngredientJpaRepository extends JpaRepository<IngredientEntity, UUID> {
}
