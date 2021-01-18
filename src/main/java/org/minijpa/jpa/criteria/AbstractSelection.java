package org.minijpa.jpa.criteria;

import java.util.List;

import javax.persistence.criteria.Selection;

public abstract class AbstractSelection<X> extends AbstractTupleElement<X> implements Selection<X> {
	private boolean compoundSelection = false;

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
		return compoundSelection;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		if (!compoundSelection)
			throw new IllegalStateException("Not a compound selection");

		return null;
	}

}
