/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jpa.criteria.expression;

import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

public class MiniParameterExpression<T> implements ParameterExpression<T> {

	private final Class<T> paramClass;
	private String name;
	private String alias;

	public MiniParameterExpression(Class<T> paramClass) {
		super();
		this.paramClass = paramClass;
	}

	public MiniParameterExpression(Class<T> paramClass, String name) {
		super();
		this.paramClass = paramClass;
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Integer getPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<T> getParameterType() {
		return paramClass;
	}

	@Override
	public Predicate isNull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate isNotNull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate in(Object... values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate in(Expression<?>... values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate in(Collection<?> values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate in(Expression<Collection<?>> values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X> Expression<X> as(Class<X> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selection<T> alias(String name) {
		if (this.alias != null)
			return this;

		this.alias = name;
		return this;
	}

	@Override
	public boolean isCompoundSelection() {
		return false;
	}

	@Override
	public List<Selection<?>> getCompoundSelectionItems() {
		throw new IllegalStateException("Not a compound selection");
	}

	@Override
	public Class<? extends T> getJavaType() {
		return paramClass;
	}

	@Override
	public String getAlias() {
		return alias;
	}

}
