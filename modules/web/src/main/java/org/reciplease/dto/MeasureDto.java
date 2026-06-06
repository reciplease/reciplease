package org.reciplease.dto;

import org.reciplease.model.Measure;

/**
 * API representation of a {@link Measure}. The short name doubles as {@code measureId} and is
 * also exposed as {@code short} for display; {@code singular}/{@code plural} are the long forms.
 */
public class MeasureDto {

    private final String shortName;
    private final String singular;
    private final String plural;

    public MeasureDto(final Measure measure) {
        this.shortName = measure.getMeasureId();
        this.singular = measure.getSingular();
        this.plural = measure.getPlural();
    }

    public String getMeasureId() {
        return shortName;
    }

    public String getSingular() {
        return singular;
    }

    public String getPlural() {
        return plural;
    }

    /** Serialized as the JSON property {@code short}. */
    public String getShort() {
        return shortName;
    }
}
