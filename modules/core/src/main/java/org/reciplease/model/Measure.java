package org.reciplease.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Measure {
    String measureId;
    String singular;
    String plural;
}
