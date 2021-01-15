package org.tinyjpa.jpa.criteria;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;

public class OrderImpl implements Order {
	private Expression<?> x;
	private boolean ascending;

	public OrderImpl(Expression<?> x, boolean ascending) {
		super();
		this.x = x;
		this.ascending = ascending;
	}

	@Override
	public Order reverse() {
		return new OrderImpl(x, !ascending);
	}

	@Override
	public boolean isAscending() {
		return ascending;
	}

	@Override
	public Expression<?> getExpression() {
		return x;
	}

}
