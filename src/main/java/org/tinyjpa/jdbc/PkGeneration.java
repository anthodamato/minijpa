package org.tinyjpa.jdbc;

public class PkGeneration {
	private PkGenerationType strategy = PkGenerationType.AUTO;
	private String generator;

	public PkGeneration() {
		super();
	}

	public PkGeneration(PkGenerationType strategy, String generator) {
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
