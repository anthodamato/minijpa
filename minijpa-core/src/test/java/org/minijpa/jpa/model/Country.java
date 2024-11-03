package org.minijpa.jpa.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Getter
@Setter
@Entity
public class Country {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    private Continent continent;
}
