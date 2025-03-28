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

import org.minijpa.jpa.criteria.predicate.PredicateUtils;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.criteria.predicate.PredicateType;
import org.minijpa.jpa.criteria.predicate.MultiplePredicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.minijpa.metadata.PersistenceUnitContext;

public class MiniCriteriaQuery<T> implements CriteriaQuery<T> {

    private Class<T> resultClass;
    private final Metamodel metamodel;
    private final PersistenceUnitContext persistenceUnitContext;
    private final Set<Root<?>> roots = new HashSet<>();
    protected Selection<? extends T> selection;
    private Predicate restriction;
    private final List<Order> orders = new ArrayList<>();
    private boolean distinct;

    public MiniCriteriaQuery(Class<T> resultClass, Metamodel metamodel, PersistenceUnitContext persistenceUnitContext) {
        super();
        this.resultClass = resultClass;
        this.metamodel = metamodel;
        this.persistenceUnitContext = persistenceUnitContext;
    }

    public MiniCriteriaQuery(Metamodel metamodel, PersistenceUnitContext persistenceUnitContext) {
        super();
        this.metamodel = metamodel;
        this.persistenceUnitContext = persistenceUnitContext;
    }

    public Metamodel getMetamodel() {
        return metamodel;
    }

    public PersistenceUnitContext getPersistenceUnitContext() {
        return persistenceUnitContext;
    }

    @Override
    public <X> Root<X> from(Class<X> entityClass) {
        EntityType<X> entityType = metamodel.entity(entityClass);
        MetaEntity metaEntity = persistenceUnitContext.getEntities().get(entityClass.getName());
        Root<X> root = new MiniRoot<>(entityType, metaEntity);
        roots.add(root);
        return root;
    }

    @Override
    public <X> Root<X> from(EntityType<X> entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Root<?>> getRoots() {
        return new HashSet<>(roots);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Selection<T> getSelection() {
//        if (selection == null && !roots.isEmpty())
//            return (MiniRoot<T>) roots.iterator().next();

        return (Selection<T>) selection;
    }

    @Override
    public List<Expression<?>> getGroupList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Predicate getGroupRestriction() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isDistinct() {
        return distinct;
    }

    @Override
    public Class<T> getResultType() {
        return resultClass;
    }

    @Override
    public <U> Subquery<U> subquery(Class<U> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Predicate getRestriction() {
        return restriction;
    }

    @Override
    public CriteriaQuery<T> select(Selection<? extends T> selection) {
        this.selection = selection;
        return this;
    }

    @Override
    public CriteriaQuery<T> multiselect(Selection<?>... selections) {
        Class<T> rc = getResultType();
        if (rc == null)
            rc = (Class<T>) Object[].class;

        this.selection = new CompoundSelectionImpl<>(Arrays.asList(selections), rc);
        return this;
    }

    @Override
    public CriteriaQuery<T> multiselect(List<Selection<?>> selectionList) {
        Class<T> rc = getResultType();
        if (rc == null)
            rc = (Class<T>) Object[].class;

        this.selection = new CompoundSelectionImpl<>(Collections.unmodifiableList(selectionList), rc);
        return this;
    }

    @Override
    public CriteriaQuery<T> where(Expression<Boolean> restriction) {
        this.restriction = (Predicate) restriction;
        return this;
    }

    @Override
    public CriteriaQuery<T> where(Predicate... restrictions) {
        if (restrictions.length == 0)
            this.restriction = null;

        this.restriction = new MultiplePredicate(PredicateType.AND, restrictions);
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
        orders.clear();
        if (o == null)
            return this;

        orders.addAll(Arrays.asList(o));
        return this;
    }

    @Override
    public CriteriaQuery<T> orderBy(List<Order> o) {
        orders.clear();
        if (o == null)
            return this;

        orders.addAll(o);
        return this;
    }

    @Override
    public CriteriaQuery<T> distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    @Override
    public List<Order> getOrderList() {
        return Collections.unmodifiableList(orders);
    }

    @Override
    public Set<ParameterExpression<?>> getParameters() {
        if (restriction == null)
            return new HashSet<>();

        Set<ParameterExpression<?>> parameterExpressions = PredicateUtils.findParameters(restriction);
        return parameterExpressions;
    }

}
