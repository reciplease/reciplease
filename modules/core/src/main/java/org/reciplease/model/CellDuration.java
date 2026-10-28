package org.reciplease.model;

public record CellDuration(int minMinutes, int maxMinutes) {
    public CellDuration {
        if (minMinutes < 0) {
            throw new IllegalArgumentException("minMinutes must not be negative");
        }
        if (maxMinutes < minMinutes) {
            throw new IllegalArgumentException("maxMinutes must not be below minMinutes");
        }
    }
}
