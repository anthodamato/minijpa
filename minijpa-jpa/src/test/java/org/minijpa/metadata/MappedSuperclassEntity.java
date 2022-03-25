package org.minijpa.metadata;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
public class MappedSuperclassEntity extends MappedSuperclassExample {

    private Integer prop1;
    private String eS1;
    private String N;
    private String Ns;
    @Transient
    private Integer transientProperty;

    public Integer getProp1() {
	return prop1;
    }

    public void setProp1(Integer prop1) {
	this.prop1 = prop1;
    }

    public String geteS1() {
	return eS1;
    }

    public void seteS1(String eS1) {
	this.eS1 = eS1;
    }

    public String getN() {
	return N;
    }

    public void setN(String n) {
	N = n;
    }

    public String getNs() {
	return Ns;
    }

    public void setNs(String ns) {
	Ns = ns;
    }

    public Integer getTransientProperty() {
	return transientProperty;
    }

    public void setTransientProperty(Integer transientProperty) {
	this.transientProperty = transientProperty;
    }

}
