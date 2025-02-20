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

import org.minijpa.jpa.criteria.AttributePath;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import java.util.Collection;
import java.util.List;

public class BinaryExpression<N extends Number> implements Expression<N>, BinaryExpressionTypeInfo {

    private final ExpressionOperator expressionOperator;
    private Expression<N> x;
    private Object xValue;
    private Expression<N> y;
    private Object yValue;
    private String alias;

    public BinaryExpression(ExpressionOperator expressionOperator, Expression<N> x, Expression<N> y) {
        super();
        this.expressionOperator = expressionOperator;
        this.x = x;
        this.y = y;
    }

    public BinaryExpression(ExpressionOperator expressionOperator, Expression<N> x, Object yValue) {
        super();
        this.expressionOperator = expressionOperator;
        this.x = x;
        this.yValue = yValue;
    }

    public BinaryExpression(ExpressionOperator expressionOperator, Object xValue, Expression<N> y) {
        super();
        this.expressionOperator = expressionOperator;
        this.xValue = xValue;
        this.y = y;
    }

    @Override
    public ExpressionOperator getExpressionOperator() {
        return expressionOperator;
    }

    public Expression<N> getX() {
        return x;
    }

    public Object getxValue() {
        return xValue;
    }

    public Expression<N> getY() {
        return y;
    }

    public Object getyValue() {
        return yValue;
    }

    @Override
    public Selection<N> alias(String name) {
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
        throw new IllegalStateException(this + " is not a compound selection");
    }

    @Override
    public Class<? extends N> getJavaType() {
        if (x != null && x instanceof AttributePath)
            return ((AttributePath) x).getJavaType();

        if (y != null && y instanceof AttributePath)
            return ((AttributePath) y).getJavaType();

        return null;
    }

    @Override
    public String getAlias() {
        return alias;
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

}
