package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.PendingPantryItem;

@Value
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(name = "PendingPantryItem")
public class PendingPantryItemDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
    String uuid;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
    String houseId;

    @Size(max = 64)
    String scanId;

    byte[] barcodeImage;
    // Read-only echo of a pre-existing decoded barcode for items captured before barcodeImage
    // existed — see PendingPantryItem.legacyBarcode. Never accepted on create (absent from
    // toEntity below), so this can only ever be non-null on an item this API didn't create.
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String legacyBarcode;

    byte[] expirationImage;
    byte[] measureImage;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
    Instant updatedAt;

    public static PendingPantryItemDto from(final PendingPantryItem item) {
        return PendingPantryItemDto.builder()
                .uuid(item.id())
                .houseId(item.houseId())
                .scanId(item.scanId())
                .barcodeImage(item.barcodeImage())
                .legacyBarcode(item.legacyBarcode())
                .expirationImage(item.expirationImage())
                .measureImage(item.measureImage())
                .updatedAt(item.updatedAt())
                .build();
    }
}
