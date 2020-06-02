package org.tinyjpa.metadata;

import javax.persistence.Entity;

@Entity
public class MappedSuperclassSecondEntity extends MappedSuperclassExample {
	private Integer attribute;
	private String eS;
	private String URL;

	public Integer getAttribute() {
		return attribute;
	}

	public void setAttribute(Integer attribute) {
		this.attribute = attribute;
	}

	public String geteS() {
		return eS;
	}

	public void seteS(String eS) {
		this.eS = eS;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

}
