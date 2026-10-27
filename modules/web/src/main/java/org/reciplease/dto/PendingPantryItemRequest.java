package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.PendingPantryItem;

@Value
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(name = "PendingPantryItemRequest")
public class PendingPantryItemRequest {

    @Size(max = 64)
    String scanId;

    byte[] barcodeImage;

    byte[] expirationImage;

    byte[] measureImage;

    // houseId is supplied by the controller from the already-validated X-RCPLS-House-Id
    // header, and the id comes from the server on create; neither is accepted from the body.
    public PendingPantryItem toEntity(final String houseId) {
        return new PendingPantryItem(
                null, null, houseId, barcodeImage, null, expirationImage, measureImage, null, null, scanId);
    }
}
