package com.reciplease.model;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class InventoryItemTest {
    @Mock
    private Ingredient ingredient;
    @Mock
    private Calendar expiration;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldCreateInstance() {
        InventoryItem inventoryItem = new InventoryItem("id", ingredient, 10, expiration);

        assertThat(inventoryItem, is(notNullValue()));
        assertThat(inventoryItem.getAmount(), is(10));
    }
}