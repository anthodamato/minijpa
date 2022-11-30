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

import org.minijpa.jpa.criteria.predicate.PredicateType;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.criteria.predicate.MultiplePredicate;

import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.minijpa.metadata.PersistenceUnitContext;

public class MiniCriteriaDelete<T> implements CriteriaDelete<T> {

    private final Class<T> resultClass;
    private final Metamodel metamodel;
    private final PersistenceUnitContext persistenceUnitContext;
    private Root<T> root;
    private Predicate restriction;

    public MiniCriteriaDelete(Class<T> resultClass, Metamodel metamodel, PersistenceUnitContext persistenceUnitContext) {
	super();
	this.resultClass = resultClass;
	this.metamodel = metamodel;
	this.persistenceUnitContext = persistenceUnitContext;
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
    public Root<T> from(Class<T> entityClass) {
	EntityType<T> entityType = metamodel.entity(entityClass);
//		Map<String, MetaEntity> entities = ((MiniEntityManager) em).getEntities();
	MetaEntity metaEntity = persistenceUnitContext.getEntities().get(entityClass.getName());
	root = new MiniRoot<T>(entityType, metaEntity);
	return root;
    }

    @Override
    public Root<T> from(EntityType<T> entity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Root<T> getRoot() {
	return root;
    }

    @Override
    public CriteriaDelete<T> where(Expression<Boolean> restriction) {
	this.restriction = (Predicate) restriction;
	return this;
    }

    @Override
    public CriteriaDelete<T> where(Predicate... restrictions) {
	if (restrictions.length == 0)
	    this.restriction = null;

	this.restriction = new MultiplePredicate(PredicateType.AND, restrictions);
	return this;
    }

}
