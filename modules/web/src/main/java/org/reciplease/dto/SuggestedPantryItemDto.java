package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
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
@Schema(name = "SuggestedPantryItem")
public class SuggestedPantryItemDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
    String uuid;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
    String houseId;

    @Size(max = 200)
    @Schema(requiredMode = REQUIRED, maxLength = 200)
    String name;

    @Size(max = 200)
    @Schema(maxLength = 200)
    String brand;

    @Schema(requiredMode = REQUIRED)
    String measure;

    @DecimalMin(value = "0", inclusive = false)
    @Schema(requiredMode = REQUIRED, minimum = "0", exclusiveMinimum = true)
    Double amount;

    @DecimalMin(value = "0", inclusive = true)
    @Schema(requiredMode = REQUIRED, minimum = "0")
    Double remaining;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate expiration;

    @Pattern(regexp = "^$|^\\d{8}$|^\\d{12,14}$")
    @Schema(pattern = "^$|^\\d{8}$|^\\d{12,14}$")
    String barcode;

    byte[] image;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
    Instant createdAt;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, requiredMode = REQUIRED)
    Instant updatedAt;

    @DecimalMin(value = "0", inclusive = true)
    @Schema(requiredMode = REQUIRED, minimum = "0")
    Double available;

    public static SuggestedPantryItemDto from(final PantryItem pantryItem, final double available) {
        return SuggestedPantryItemDto.builder()
                .uuid(pantryItem.id())
                .houseId(pantryItem.houseId())
                .name(pantryItem.name())
                .brand(pantryItem.brand())
                .measure(Measure.normalizeId(pantryItem.measure()))
                .amount(pantryItem.amount())
                .remaining(pantryItem.remaining())
                .expiration(pantryItem.expiration())
                .barcode(pantryItem.barcode())
                .image(pantryItem.image())
                .createdAt(pantryItem.createdAt())
                .updatedAt(pantryItem.updatedAt())
                .available(available)
                .build();
    }
}
