package org.reciplease.dto;

/** A single linked sign-in provider, as exposed to its own owner. */
public record LinkedIdentityDto(String provider, String email) {}
