package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.PantryItem;
import org.reciplease.model.Measure;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDate;

@Value
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(name = "PantryItem")
public class PantryItemDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String uuid;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String houseId;
    String name;
    String brand;
    String measure;
    Double amount;
    Double remaining;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate expiration;
    String barcode;
    byte[] image;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    Instant createdAt;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    Instant updatedAt;

    public static PantryItemDto from(final PantryItem pantryItem) {
        return PantryItemDto.builder()
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
                .build();
    }

    // houseId is supplied by the controller from the already-validated X-RCPLS-House-Id
    // header, not trusted from the request body, so a member can't write into a house
    // they don't belong to by spoofing the field. The body's uuid is untrusted for the
    // same reason: READ_ONLY only affects the OpenAPI schema, Jackson still binds it, and
    // Mongo saves are upserts — a caller-chosen id could overwrite an arbitrary document.
    // Callers that need an id (update, complete) supply the path's already-checked one.
    public PantryItem toEntity(final String houseId) {
        return new PantryItem(null, null, houseId, name, brand, Measure.normalizeId(measure), amount, remaining, expiration, barcode, image);
    }
}
