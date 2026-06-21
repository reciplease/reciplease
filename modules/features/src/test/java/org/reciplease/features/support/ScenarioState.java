package org.reciplease.features.support;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;

/** Shared, per-scenario state for step definitions (cucumber-spring resets this every scenario). */
@Component
@ScenarioScope
public class ScenarioState {
    private final Map<String, String> houseIdsByName = new HashMap<>();
    private ResultActions lastResult;

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

    public void setLastResult(final ResultActions result) {
        this.lastResult = result;
    }

    public ResultActions lastResult() {
        if (lastResult == null) {
            throw new IllegalStateException("No request has been performed yet in this scenario");
        }
        return lastResult;
    }
}
