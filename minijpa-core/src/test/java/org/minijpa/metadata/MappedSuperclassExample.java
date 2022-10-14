package org.minijpa.metadata;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class MappedSuperclassExample {
	@Id
	protected Integer id;

	String superProperty1;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	protected String getSuperProperty1() {
		return superProperty1;
	}

	protected void setSuperProperty1(String superProperty1) {
		this.superProperty1 = superProperty1;
	}

}
