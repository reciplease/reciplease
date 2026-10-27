package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.Measure;
import org.reciplease.model.PantryItem;
import org.springframework.format.annotation.DateTimeFormat;

@Value
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(name = "PantryItemRequest")
public class PantryItemRequest {

    @NotNull
    @Size(max = 200)
    @Schema(requiredMode = REQUIRED, maxLength = 200)
    String name;

    @Size(max = 200)
    @Schema(maxLength = 200)
    String brand;

    @NotNull
    @Schema(requiredMode = REQUIRED)
    String measure;

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @Schema(requiredMode = REQUIRED, minimum = "0", exclusiveMinimum = true)
    Double amount;

    @DecimalMin(value = "0", inclusive = true)
    @Schema(requiredMode = NOT_REQUIRED, nullable = true, minimum = "0")
    Double remaining;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(requiredMode = REQUIRED)
    LocalDate expiration;

    @Pattern(regexp = "^$|^\\d{8}$|^\\d{12,14}$")
    @Schema(pattern = "^$|^\\d{8}$|^\\d{12,14}$")
    String barcode;

    byte[] image;

    // houseId is supplied by the controller from the already-validated X-RCPLS-House-Id
    // header, and ids come from the server (create) or the already-checked path variable
    // (update, complete); neither is accepted from the body.
    public PantryItem toEntity(final String houseId) {
        return new PantryItem(
                null,
                null,
                houseId,
                name,
                brand,
                Measure.normalizeId(measure),
                amount,
                remaining,
                expiration,
                barcode,
                image);
    }
}
