package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("measures")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MeasureDocument {

    @Id
    private String measureId;
    private String singular;
    private String plural;

    public static MeasureDocument from(final Measure measure) {
        return MeasureDocument.builder()
                .measureId(measure.getMeasureId())
                .singular(measure.getSingular())
                .plural(measure.getPlural())
                .build();
    }

    public Measure toModel() {
        return Measure.builder()
                .measureId(measureId)
                .singular(singular)
                .plural(plural)
                .build();
    }
}
