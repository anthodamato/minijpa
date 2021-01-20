package org.minijpa.jpa.criteria;

import java.util.List;

import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.Selection;

public class CompoundSelectionImpl<X> implements CompoundSelection<X> {
	private List<Selection<?>> selections;
	private Class<? extends X> javaType;

	public CompoundSelectionImpl(List<Selection<?>> selections, Class<? extends X> javaType) {
		super();
		this.selections = selections;
		this.javaType = javaType;
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
		return javaType;
	}

	@Override
	public String getAlias() {
		// TODO Auto-generated method stub
		return null;
	}

}
