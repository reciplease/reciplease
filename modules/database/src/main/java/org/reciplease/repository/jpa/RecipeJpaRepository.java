package org.reciplease.repository.jpa;

import org.reciplease.model.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecipeJpaRepository extends JpaRepository<RecipeEntity, UUID> {
}
