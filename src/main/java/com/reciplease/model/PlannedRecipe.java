package com.reciplease.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Calendar;

@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class PlannedRecipe {
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(columnDefinition = "CHAR(32)")
    @Id
    @NonNull
    private String id;
    @ManyToOne
    @NonNull
    private Recipe recipe;
    @Temporal(TemporalType.DATE)
    @NonNull
    private Calendar date;
}
