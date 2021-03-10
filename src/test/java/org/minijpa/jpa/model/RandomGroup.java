/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.model;

import java.util.Collection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author adamato
 */
@Entity
@Table(name = "random_group")
public class RandomGroup {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    @OneToMany
    private Collection<RandomData> randomDataValues;

    public Long getId() {
	return id;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public Collection<RandomData> getRandomDataValues() {
	return randomDataValues;
    }

    public void setRandomDataValues(Collection<RandomData> randomDataValues) {
	this.randomDataValues = randomDataValues;
    }

}
