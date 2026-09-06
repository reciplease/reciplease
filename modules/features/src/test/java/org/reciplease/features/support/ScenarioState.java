package org.reciplease.features.support;

import io.cucumber.spring.ScenarioScope;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;

/** Shared, per-scenario state for step definitions (cucumber-spring resets this every scenario). */
@Component
@ScenarioScope
public class ScenarioState {
    private final Map<String, String> houseIdsByName = new HashMap<>();
    private final Map<String, String> recipeIdsByName = new HashMap<>();
    private ResultActions lastResult;
    private String generatedInviteId;
    private String generatedInviteCode;

    public void putHouseId(final String houseName, final String houseId) {
        houseIdsByName.put(houseName, houseId);
    }

    public String houseId(final String houseName) {
        final var houseId = houseIdsByName.get(houseName);
        if (houseId == null) {
            throw new IllegalStateException("No house registered with name: " + houseName);
        }
        return houseId;
    }

    public void putRecipeId(final String recipeName, final String recipeId) {
        recipeIdsByName.put(recipeName, recipeId);
    }

    public String recipeId(final String recipeName) {
        final var recipeId = recipeIdsByName.get(recipeName);
        if (recipeId == null) {
            throw new IllegalStateException("No recipe registered with name: " + recipeName);
        }
        return recipeId;
    }

    public void setLastResult(final ResultActions result) {
        this.lastResult = result;
    }

    public ResultActions lastResult() {
        if (lastResult == null) {
            throw new IllegalStateException("No request has been performed yet in this scenario");
        }
        return lastResult;
    }

    public void setGeneratedInvite(final String id, final String code) {
        this.generatedInviteId = id;
        this.generatedInviteCode = code;
    }

    public String generatedInviteId() {
        if (generatedInviteId == null) {
            throw new IllegalStateException("No invite has been generated yet in this scenario");
        }
        return generatedInviteId;
    }

    public String generatedInviteCode() {
        if (generatedInviteCode == null) {
            throw new IllegalStateException("No invite has been generated yet in this scenario");
        }
        return generatedInviteCode;
    }
}
