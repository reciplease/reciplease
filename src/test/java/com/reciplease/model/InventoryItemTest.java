package com.reciplease.model;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class InventoryItemTest {
    @Test
    public void shouldCreateInstance() {
        InventoryItem inventoryItem = new InventoryItem("test", "test");

        assertThat(inventoryItem, is(notNullValue()));
        assertThat(inventoryItem.getName(), is("test"));
    }
}