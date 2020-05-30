package org.tinyjpa.metadata;

import javax.persistence.GenerationType;

public class GeneratedValue {
	private GenerationType strategy = GenerationType.AUTO;
	private String generator;

	public GeneratedValue() {
		super();
	}

	public GeneratedValue(GenerationType strategy, String generator) {
		super();
		this.strategy = strategy;
		this.generator = generator;
	}

	public GenerationType getStrategy() {
		return strategy;
	}

	public String getGenerator() {
		return generator;
	}

}
