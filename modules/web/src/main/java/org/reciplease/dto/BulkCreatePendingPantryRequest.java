package org.reciplease.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Value;

@Value
public class BulkCreatePendingPantryRequest {
    @NotEmpty
    @Size(max = 50)
    List<@Valid PendingPantryItemRequest> items;
}
