package org.reciplease.model;

/**
 * What a successfully authenticated API key resolves to: the house it acts for and the role it
 * acts with. Unlike a user JWT, there is no separate membership lookup — the key itself carries
 * its authorization.
 */
public record ApiKeyPrincipal(String apiKeyId, String houseId, HouseRole role) {}
