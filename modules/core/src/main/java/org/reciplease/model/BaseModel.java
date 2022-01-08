package org.reciplease.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder(toBuilder = true)
public abstract class BaseModel {
    private UUID uuid;

    public abstract static class BaseModelBuilder<C extends BaseModel, B extends BaseModel.BaseModelBuilder<C, B>> {
        private UUID uuid;

        // Used for testing only
        public B randomUUID() {
            this.uuid = UUID.randomUUID();
            return this.self();
        }
    }
}
