package org.minijpa.jpa.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Stove {
	@Id
	@GeneratedValue
	private Long id;

	private String model;
	private Integer numberOfBurners;
	private Boolean induction;

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Integer getNumberOfBurners() {
		return numberOfBurners;
	}

	public void setNumberOfBurners(Integer numberOfBurners) {
		this.numberOfBurners = numberOfBurners;
	}

	public Boolean getInduction() {
		return induction;
	}

	public void setInduction(Boolean induction) {
		this.induction = induction;
	}

	public Long getId() {
		return id;
	}

}
