package org.tinyjpa.jpa.criteria;

import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Metamodel;

public class CriteriaQueryImpl<T> extends AbstractAbstractQuery<T> implements CriteriaQuery<T> {
//	private List<Selection<? extends T>> selections = new ArrayList<Selection<? extends T>>();

	public CriteriaQueryImpl(Class<T> resultClass, Metamodel metamodel) {
		super(resultClass, metamodel);
	}

	@Override
	public CriteriaQuery<T> select(Selection<? extends T> selection) {
//		selections.clear();
//		selections.add(selection);
		this.selection = selection;
//		Set<Root<?>> roots = getRoots();
//		if (roots.contains(selection) && roots.size() == 1)
//			return this;

		return this;
	}

	@Override
	public CriteriaQuery<T> multiselect(Selection<?>... selections) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> multiselect(List<Selection<?>> selectionList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> where(Expression<Boolean> restriction) {
		super.where(restriction);
		return this;
	}

	@Override
	public CriteriaQuery<T> where(Predicate... restrictions) {
		super.where(restrictions);
		return this;
	}

	@Override
	public CriteriaQuery<T> groupBy(Expression<?>... grouping) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> groupBy(List<Expression<?>> grouping) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> having(Expression<Boolean> restriction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> having(Predicate... restrictions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> orderBy(Order... o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> orderBy(List<Order> o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> distinct(boolean distinct) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Order> getOrderList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ParameterExpression<?>> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

}
