package org.reciplease.model;

import java.util.Arrays;
import java.util.EnumMap;
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
 *
 * <p>Each measure belongs to a {@link Family} and carries a factor for converting an amount to
 * that family's base unit ({@link #GRAM} for mass, {@link #MILLILITRE} for volume, {@link #ITEM}
 * for count) — see {@link #toBaseUnits(double)}. Measures in different families (e.g. grams and
 * millilitres) are never convertible, since that would require ingredient-specific density.
 */
public enum Measure {
    GRAM(Family.MASS, "gram", "grams", "g", 1d, "GRAMS"),
    KILOGRAM(Family.MASS, "kilogram", "kilograms", "kg", 1000d, "KILOGRAMS"),
    OUNCE(Family.MASS, "ounce", "ounces", "oz", 28.3495),
    POUND(Family.MASS, "pound", "pounds", "lb", 453.592),
    MILLILITRE(Family.VOLUME, "millilitre", "millilitres", "ml", 1d, "MILLILITRES"),
    CENTILITRE(Family.VOLUME, "centilitre", "centilitres", "cl", 10d),
    LITRE(Family.VOLUME, "litre", "litres", "l", 1000d, "LITRES"),
    TEASPOON(Family.VOLUME, "teaspoon", "teaspoons", "tsp", 4.92892),
    TABLESPOON(Family.VOLUME, "tablespoon", "tablespoons", "tbsp", 14.7868),
    CUP(Family.VOLUME, "cup", "cups", "cup", 236.588),
    ITEM(Family.COUNT, "item", "items", "item", 1d, "ITEMS"),
    PIECE(Family.COUNT, "piece", "pieces", "pc", 1d, "PIECES");

    /** A group of measures that can be converted between one another. */
    public enum Family {
        MASS,
        VOLUME,
        COUNT
    }

    private final Family family;
    private final String singular;
    private final String plural;
    private final String shortName;
    private final double factorToBase;
    private final String[] legacyIds;

    Measure(
            final Family family,
            final String singular,
            final String plural,
            final String shortName,
            final double factorToBase,
            final String... legacyIds) {
        this.family = family;
        this.singular = singular;
        this.plural = plural;
        this.shortName = shortName;
        this.factorToBase = factorToBase;
        this.legacyIds = legacyIds;
    }

    public Family getFamily() {
        return family;
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

    /** Converts an amount in this measure to the equivalent amount in {@link #getFamily()}'s base unit. */
    public double toBaseUnits(final double amount) {
        return amount * factorToBase;
    }

    /** Converts an amount in the family's base unit back to this measure. */
    public double fromBaseUnits(final double baseAmount) {
        return baseAmount / factorToBase;
    }

    private static final Map<String, Measure> BY_ID = new HashMap<>();
    private static final Map<Family, Measure> BASE_BY_FAMILY = new EnumMap<>(Family.class);

    static {
        for (final Measure measure : values()) {
            BY_ID.put(measure.shortName.toLowerCase(), measure);
            for (final String legacy : measure.legacyIds) {
                BY_ID.put(legacy.toLowerCase(), measure);
            }
            if (measure.factorToBase == 1d) {
                BASE_BY_FAMILY.putIfAbsent(measure.family, measure);
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

    /** The measure that amounts within {@code family} are converted to/from (factor of 1). */
    public static Measure baseMeasureFor(final Family family) {
        return BASE_BY_FAMILY.get(family);
    }

    public static List<Measure> all() {
        return Arrays.asList(values());
    }
}
