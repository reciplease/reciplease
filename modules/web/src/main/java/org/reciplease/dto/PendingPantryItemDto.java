package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
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
                .barcodeImage(item.barcodeImage())
                .legacyBarcode(item.legacyBarcode())
                .expirationImage(item.expirationImage())
                .measureImage(item.measureImage())
                .updatedAt(item.updatedAt())
                .build();
    }

    // houseId is supplied by the controller from the already-validated X-RCPLS-House-Id
    // header, not trusted from the request body, so a member can't write into a house
    // they don't belong to by spoofing the field. The body's uuid is untrusted for the
    // same reason: READ_ONLY only affects the OpenAPI schema, Jackson still binds it, and
    // Mongo saves are upserts — a caller-chosen id could overwrite an arbitrary document
    // (and, via complete()'s id reuse, even another house's pantry item).
    public PendingPantryItem toEntity(final String houseId) {
        return new PendingPantryItem(null, null, houseId, barcodeImage, expirationImage, measureImage);
    }
}
