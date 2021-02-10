package org.minijpa.jdbc;

public class PkGeneration {

    private PkStrategy pkStrategy = PkStrategy.PLAIN;
    private PkGenerationType strategy = PkGenerationType.AUTO;
    private String generator;
    private PkSequenceGenerator pkSequenceGenerator;

    public PkStrategy getPkStrategy() {
	return pkStrategy;
    }

    public void setPkStrategy(PkStrategy pkStrategy) {
	this.pkStrategy = pkStrategy;
    }

    public PkGenerationType getStrategy() {
	return strategy;
    }

    public void setGenerator(String generator) {
	this.generator = generator;
    }

    public String getGenerator() {
	return generator;
    }

    public PkSequenceGenerator getPkSequenceGenerator() {
	return pkSequenceGenerator;
    }

    public void setPkSequenceGenerator(PkSequenceGenerator pkSequenceGenerator) {
	this.pkSequenceGenerator = pkSequenceGenerator;
    }

}
