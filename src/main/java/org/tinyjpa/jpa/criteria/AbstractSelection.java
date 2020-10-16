package org.tinyjpa.jpa.criteria;

import java.util.List;

import javax.persistence.criteria.Selection;

public abstract class AbstractSelection<X> extends AbstractTupleElement<X> implements Selection<X> {

	public AbstractSelection(Class<? extends X> javaType) {
		super(javaType);
	}

	@Override
	public Selection<X> alias(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCompoundSelection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
