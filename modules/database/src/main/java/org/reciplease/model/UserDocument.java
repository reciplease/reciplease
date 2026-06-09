package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDocument {

    @Id
    private String googleId;
    private String email;

    public static UserDocument from(final User user) {
        return UserDocument.builder()
                .googleId(user.id())
                .email(user.email())
                .build();
    }

    public User toModel() {
        return new User(googleId, email);
    }
}
