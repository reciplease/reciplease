package org.reciplease.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The fixed catalog of units an item or ingredient can be measured in. Static reference data
 * (no longer persisted): the short name doubles as the stable id stored on items/ingredients
 * and exposed over the API (e.g. {@code g}, {@code kg}, {@code cl}).
 *
 * <p>{@link #fromId(String)} also resolves the legacy uppercase ids that predate this change
 * (e.g. {@code GRAMS} -> {@link #GRAM}), so existing stored data keeps working without a
 * database migration.
 */
public enum Measure {
    GRAM("gram", "grams", "g", "GRAMS"),
    KILOGRAM("kilogram", "kilograms", "kg", "KILOGRAMS"),
    MILLILITRE("millilitre", "millilitres", "ml", "MILLILITRES"),
    CENTILITRE("centilitre", "centilitres", "cl"),
    LITRE("litre", "litres", "l", "LITRES"),
    TEASPOON("teaspoon", "teaspoons", "tsp"),
    TABLESPOON("tablespoon", "tablespoons", "tbsp"),
    ITEM("item", "items", "item", "ITEMS"),
    PIECE("piece", "pieces", "pc", "PIECES"),
    OUNCE("ounce", "ounces", "oz"),
    CUP("cup", "cups", "cup"),
    POUND("pound", "pounds", "lb");

    private final String singular;
    private final String plural;
    private final String shortName;
    private final String[] legacyIds;

    Measure(final String singular, final String plural, final String shortName, final String... legacyIds) {
        this.singular = singular;
        this.plural = plural;
        this.shortName = shortName;
        this.legacyIds = legacyIds;
    }

    public String getSingular() {
        return singular;
    }

    public String getPlural() {
        return plural;
    }

    public String getShortName() {
        return shortName;
    }

    /** The stable id stored and exposed over the API — the short name. */
    public String getMeasureId() {
        return shortName;
    }

    private static final Map<String, Measure> BY_ID = new HashMap<>();

    static {
        for (final Measure measure : values()) {
            BY_ID.put(measure.shortName.toLowerCase(), measure);
            for (final String legacy : measure.legacyIds) {
                BY_ID.put(legacy.toLowerCase(), measure);
            }
        }
    }

    /** Resolves a measure from its short id or a legacy id (case-insensitive). */
    public static Optional<Measure> fromId(final String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(BY_ID.get(id.trim().toLowerCase()));
    }

    /**
     * Returns the canonical short id for a stored measure value, mapping legacy ids forward.
     * Unknown values are returned unchanged so unexpected data is never silently dropped.
     */
    public static String normalizeId(final String id) {
        return fromId(id).map(Measure::getMeasureId).orElse(id);
    }

    public static List<Measure> all() {
        return Arrays.asList(values());
    }
}
