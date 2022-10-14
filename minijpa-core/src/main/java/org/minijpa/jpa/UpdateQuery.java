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
package org.minijpa.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TransactionRequiredException;
import javax.persistence.criteria.CriteriaUpdate;

import org.minijpa.jpa.db.JdbcEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateQuery extends AbstractQuery {

	private final Logger LOG = LoggerFactory.getLogger(UpdateQuery.class);
	private final CriteriaUpdate<?> criteriaUpdate;
	private final EntityManager entityManager;

	public UpdateQuery(CriteriaUpdate<?> criteriaUpdate, EntityManager entityManager,
			JdbcEntityManager jdbcEntityManager) {
		super();
		this.criteriaUpdate = criteriaUpdate;
		this.entityManager = entityManager;
		this.jdbcEntityManager = jdbcEntityManager;
	}

	public CriteriaUpdate<?> getCriteriaUpdate() {
		return criteriaUpdate;
	}

	@Override
	public List getResultList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getSingleResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int executeUpdate() {
		if (!entityManager.getTransaction().isActive())
			throw new TransactionRequiredException("Update requires an active transaction");

		try {
			return jdbcEntityManager.update(this);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw new PersistenceException(e.getMessage());
		}
	}

}
