package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.reciplease.model.ListMethod;
import org.reciplease.model.RecipeMethod;

@Schema(name = "ListMethod")
public record ListMethodDto(
        @NotNull @Schema(requiredMode = REQUIRED) List<String> steps) implements RecipeMethodDto {

    @JsonAnySetter
    void rejectUnknownProperty(final String name, final Object value) {
        throw new IllegalArgumentException("unknown list method property: " + name);
    }

    @Override
    public RecipeMethod toModel() {
        return new ListMethod(steps);
    }
}
