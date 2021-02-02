package org.minijpa.jpa.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class City {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private Integer population;

    @OneToOne
    private Region region;

    public Long getId() {
	return id;
    }

    public void setId(Long id) {
	this.id = id;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public Region getRegion() {
	return region;
    }

    public void setRegion(Region region) {
	this.region = region;
    }

    public Integer getPopulation() {
	return population;
    }

    public void setPopulation(Integer population) {
	this.population = population;
    }

}
