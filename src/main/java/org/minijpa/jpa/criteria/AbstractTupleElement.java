package org.minijpa.jpa.criteria;

import javax.persistence.TupleElement;

public abstract class AbstractTupleElement<X> implements TupleElement<X> {
	protected Class<? extends X> javaType;
	protected String alias;

	public AbstractTupleElement(Class<? extends X> javaType) {
		super();
		this.javaType = javaType;
	}

	@Override
	public Class<? extends X> getJavaType() {
		return javaType;
	}

	@Override
	public String getAlias() {
		return alias;
	}

}
