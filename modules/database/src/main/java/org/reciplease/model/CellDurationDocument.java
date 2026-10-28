package org.reciplease.model;

public record CellDurationDocument(int minMinutes, int maxMinutes) {

    public static CellDurationDocument from(final CellDuration duration) {
        return duration == null ? null : new CellDurationDocument(duration.minMinutes(), duration.maxMinutes());
    }

    public CellDuration toModel() {
        return new CellDuration(minMinutes, maxMinutes);
    }
}
