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
package org.minijpa.jpa.criteria;

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
