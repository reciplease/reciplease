package org.reciplease.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

class MeasureTest {

    @Test
    void convertsKilogramsToGrams() {
        assertThat(Measure.KILOGRAM.toBaseUnits(1), is(1000d));
    }

    @Test
    void convertsGramsBackFromBaseUnits() {
        assertThat(Measure.KILOGRAM.fromBaseUnits(1000d), is(1d));
    }

    @Test
    void convertsLitresToMillilitres() {
        assertThat(Measure.LITRE.toBaseUnits(1), is(1000d));
    }

    @Test
    void convertsTablespoonsToMillilitresApproximately() {
        assertThat(Measure.TABLESPOON.toBaseUnits(1), closeTo(14.7868, 0.001));
    }

    @Test
    void massAndVolumeAreDifferentFamilies() {
        assertThat(Measure.GRAM.getFamily(), is(Measure.Family.MASS));
        assertThat(Measure.MILLILITRE.getFamily(), is(Measure.Family.VOLUME));
    }

    @Test
    void baseMeasureForMassIsGram() {
        assertThat(Measure.baseMeasureFor(Measure.Family.MASS), is(Measure.GRAM));
    }

    @Test
    void baseMeasureForVolumeIsMillilitre() {
        assertThat(Measure.baseMeasureFor(Measure.Family.VOLUME), is(Measure.MILLILITRE));
    }

    @Test
    void baseMeasureForCountIsItem() {
        assertThat(Measure.baseMeasureFor(Measure.Family.COUNT), is(Measure.ITEM));
    }
}
