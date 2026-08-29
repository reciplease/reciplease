package org.reciplease.service;

import java.util.List;
import org.reciplease.model.FoodConsumption;
import org.reciplease.model.LoggedFoodHistoryEntry;

/**
 * Registers food consumption against whatever service actually stores it — core only knows
 * "log this" and "what has this user logged before," never any provider-specific vocabulary.
 */
public interface FoodConsumptionLoggerPort {

    /** @throws FoodConsumptionLoggerNotConnectedException when {@code consumption.userId()} has no linked account. */
    void log(FoodConsumption consumption);

    /**
     * This user's own previously-logged foods, most-recent-first, for fuzzy-matching against a
     * search query without needing a provider-side search capability.
     * @throws FoodConsumptionLoggerNotConnectedException when {@code userId} has no linked account.
     */
    List<LoggedFoodHistoryEntry> history(String userId);

    boolean isConnected(String userId);
}
