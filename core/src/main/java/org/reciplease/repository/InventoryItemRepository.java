package org.reciplease.repository;

import org.reciplease.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "inventory_items", path = "inventory_items")
public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {
}