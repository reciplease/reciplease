package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("allowlist")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AllowlistDocument {

    @Id
    private String email;
}
