package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.reciplease.model.PendingPantryItem;

@Value
@Schema(name = "PendingPantryReceipt")
public class PendingPantryReceiptDto {

    @Schema(requiredMode = REQUIRED)
    String scanId;

    @Schema(requiredMode = REQUIRED)
    String uuid;

    public static PendingPantryReceiptDto from(final PendingPantryItem item) {
        return new PendingPantryReceiptDto(item.scanId(), item.id());
    }
}
