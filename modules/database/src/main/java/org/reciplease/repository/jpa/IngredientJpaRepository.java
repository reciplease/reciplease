package org.reciplease.repository.jpa;

import org.reciplease.model.IngredientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IngredientJpaRepository extends JpaRepository<IngredientEntity, UUID> {
    List<IngredientEntity> findByNameContains(String searchName);
}
