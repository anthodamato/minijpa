package org.minijpa.jpa;

import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.Selection;

public class TupleImpl implements Tuple {
	private Object[] result;
	private CompoundSelection<?> compoundSelection;

	public TupleImpl(Object[] result, CompoundSelection<?> compoundSelection) {
		super();
		this.result = result;
		this.compoundSelection = compoundSelection;
	}

	@Override
	public <X> X get(TupleElement<X> tupleElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X> X get(String alias, Class<X> type) {
		// TODO Auto-generated method stub
		return null;
	}

	private Object findByAlias(String alias) {
		List<Selection<?>> selections = compoundSelection.getCompoundSelectionItems();
		for (int i = 0; i < selections.size(); ++i) {
			if (selections.get(i).getAlias() != null && selections.get(i).getAlias().equals(alias))
				return result[i];
		}

		throw new IllegalArgumentException("Element not found for alias '" + alias + "'");
	}

	@Override
	public Object get(String alias) {
		return findByAlias(alias);
	}

	@Override
	public <X> X get(int i, Class<X> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(int i) {
		if (i >= result.length)
			throw new IllegalArgumentException("Position '" + i + "' exceeds the length of result tuple");

		return result[i];
	}

	@Override
	public Object[] toArray() {
		return result;
	}

	@Override
	public List<TupleElement<?>> getElements() {
		// TODO Auto-generated method stub
		return null;
	}

}
