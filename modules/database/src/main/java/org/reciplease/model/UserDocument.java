package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * TODO one-off production migration (not implemented yet — see {@code org.reciplease.migration}
 * for the Mongock changeset mechanism this would now use): existing {@code users}/{@code allowlist}
 * documents predate this change and are keyed by the old Google subject id with an
 * {@code email} field — they are now inert under the new schema (no {@code id}/{@code handle} as
 * expected here, no {@code user_identities} entry). Before this ships to users who signed in
 * under the old scheme, someone needs to: (1) re-key each {@code allowlist} document from email
 * to a newly-minted user id, (2) create a matching {@code user_identities} document linking
 * {@code google:<old-subject>} to that new user id, and (3) create a {@code users} document with
 * that id (handle left {@code null}).
 */
@Document("users")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDocument {

    @Id
    private String id;
    @Indexed(unique = true, sparse = true)
    private String handle;

    public static UserDocument from(final User user) {
        return UserDocument.builder()
                .id(user.id())
                .handle(user.handle())
                .build();
    }

    public User toModel() {
        return new User(id, handle);
    }
}
