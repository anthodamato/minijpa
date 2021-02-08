package org.minijpa.jpa.criteria.predicate;

import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

public abstract class AbstractPredicate implements Predicate {

    private boolean not = false;
    private boolean negated = false;
    private boolean compoundSelection = false;

    public AbstractPredicate(boolean not, boolean negated) {
	this.not = not;
	this.negated = negated;
    }

    @Override
    public boolean isNegated() {
	return negated;
    }

    public boolean isNot() {
	return not;
    }

    @Override
    public Predicate isNull() {
	return null;
    }

    @Override
    public Predicate isNotNull() {
	return null;
    }

    @Override
    public Predicate in(Object... values) {
	return null;
    }

    @Override
    public Predicate in(Expression<?>... values) {
	return null;
    }

    @Override
    public Predicate in(Collection<?> values) {
	return null;
    }

    @Override
    public Predicate in(Expression<Collection<?>> values) {
	return null;
    }

    @Override
    public <X> Expression<X> as(Class<X> type) {
	return null;
    }

    @Override
    public Selection<Boolean> alias(String name) {
	return this;
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

    @Override
    public Class<? extends Boolean> getJavaType() {
	return Boolean.class;
    }

    @Override
    public String getAlias() {
	return null;
    }

}
