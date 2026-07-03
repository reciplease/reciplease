package org.reciplease.service;

/** Thrown when a {@link FoodConsumptionLoggerPort} call is attempted for a user with no linked account. */
public class FoodConsumptionLoggerNotConnectedException extends RuntimeException {
    public FoodConsumptionLoggerNotConnectedException(final String userId) {
        super("User " + userId + " has no linked food-consumption-logging account");
    }
}
