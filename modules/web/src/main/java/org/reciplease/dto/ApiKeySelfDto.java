package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.HouseRole;

/**
 * What an API-key-authenticated caller can discover about itself: the raw key is opaque and
 * carries no house id a client can read directly, so a service account has to ask the backend
 * which house/role it resolves to before it can start sending {@code X-RCPLS-House-Id}
 * on subsequent house-scoped calls.
 */
@Value
@AllArgsConstructor
@Builder
@Schema(name = "ApiKeySelf")
public class ApiKeySelfDto {
    String houseId;
    String houseName;
    HouseRole role;
}
