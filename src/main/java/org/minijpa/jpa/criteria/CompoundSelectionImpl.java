package org.minijpa.jpa.criteria;

import java.util.List;

import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.Selection;

public class CompoundSelectionImpl<X> implements CompoundSelection<X> {
	private List<Selection<?>> selections;

	public CompoundSelectionImpl(List<Selection<?>> selections) {
		super();
		this.selections = selections;
	}

	@Override
	public Selection<X> alias(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCompoundSelection() {
		return true;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		return selections;
	}

	@Override
	public Class<? extends X> getJavaType() {
		return (Class<? extends X>) Object[].class;
	}

	@Override
	public String getAlias() {
		// TODO Auto-generated method stub
		return null;
	}

}
