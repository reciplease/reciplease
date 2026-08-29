package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.PantryAllocation;

@Value
@AllArgsConstructor
@Builder
@Schema(name = "PantryAllocation")
public class PantryAllocationDto {

    @Schema(requiredMode = REQUIRED)
    String pantryItemId;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String barcode;

    @DecimalMin(value = "0", inclusive = true)
    @Schema(requiredMode = REQUIRED, minimum = "0")
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
