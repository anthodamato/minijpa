package org.tinyjpa.jdbc;

public class GeneratedValue {
	private PkGenerationType strategy = PkGenerationType.AUTO;
	private String generator;

	public GeneratedValue() {
		super();
	}

	public GeneratedValue(PkGenerationType strategy, String generator) {
		super();
		this.strategy = strategy;
		this.generator = generator;
	}

	public PkGenerationType getStrategy() {
		return strategy;
	}

	public String getGenerator() {
		return generator;
	}

}
