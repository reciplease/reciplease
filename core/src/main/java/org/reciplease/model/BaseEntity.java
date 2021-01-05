package org.reciplease.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
@EqualsAndHashCode
public abstract class BaseEntity {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @Column(columnDefinition = "BINARY(16)")
    @GeneratedValue
    private UUID uuid;

    public abstract static class BaseEntityBuilder<C extends BaseEntity, B extends BaseEntity.BaseEntityBuilder<C, B>> {
        protected UUID uuid;

        public B randomUUID() {
            this.uuid = UUID.randomUUID();
            return this.self();
        }
    }
}