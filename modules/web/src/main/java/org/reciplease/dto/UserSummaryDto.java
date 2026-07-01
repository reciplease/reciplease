package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.reciplease.model.User;

/** Public-safe subset of {@link User} for display — no email or provider identity. */
@Value
@AllArgsConstructor
@Builder
@Schema(name = "UserSummary")
public class UserSummaryDto {
    String userId;
    // Users who haven't set a display handle yet are represented with a null handle on
    // the wire, not an empty string.
    @Schema(nullable = true)
    String handle;

    public static UserSummaryDto from(final User user) {
        return UserSummaryDto.builder()
                .userId(user.id())
                .handle(user.handle())
                .build();
    }
}
