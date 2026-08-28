package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.PantryAllocation;

@Value
@AllArgsConstructor
@Builder
@Schema(name = "PantryAllocation")
public class PantryAllocationDto {

    String pantryItemId;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String barcode;
    Double amount;

    public static PantryAllocationDto from(final PantryAllocation allocation) {
        return PantryAllocationDto.builder()
                .pantryItemId(allocation.pantryItemId())
                .barcode(allocation.barcode())
                .amount(allocation.amount())
                .build();
    }

    public PantryAllocation toModel() {
        return new PantryAllocation(pantryItemId, barcode, amount);
    }
}
