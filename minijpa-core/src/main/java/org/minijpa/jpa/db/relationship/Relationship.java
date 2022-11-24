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
package org.minijpa.jpa.db.relationship;

import java.util.Optional;
import java.util.Set;
import org.minijpa.jdbc.Cascade;
import org.minijpa.jdbc.relationship.JoinColumnDataList;
import org.minijpa.jdbc.relationship.JoinTableAttributes;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;

public abstract class Relationship {

	protected FetchType fetchType = FetchType.EAGER;
	protected String joinColumnTable;
	protected MetaEntity owningEntity;
	// for bidirectional relationships
	protected MetaAttribute owningAttribute;

	/**
	 * This is the target entity.
	 */
	protected MetaEntity attributeType;
	// for bidirectional relationships
	protected MetaAttribute targetAttribute;
	protected Optional<String> mappedBy;
	protected Set<Cascade> cascades;
	protected RelationshipJoinTable joinTable;
	protected Class<?> targetEntityClass;
	protected JoinTableAttributes joinTableAttributes;
	protected Optional<JoinColumnDataList> joinColumnDataList = Optional.empty();
	protected Optional<JoinColumnMapping> joinColumnMapping = Optional.empty();

	public Relationship() {
		super();
	}

	public FetchType getFetchType() {
		return fetchType;
	}

	public String getJoinColumnTable() {
		return joinColumnTable;
	}

	public MetaEntity getOwningEntity() {
		return owningEntity;
	}

	public MetaAttribute getOwningAttribute() {
		return owningAttribute;
	}

	public MetaEntity getAttributeType() {
		return attributeType;
	}

	public MetaAttribute getTargetAttribute() {
		return targetAttribute;
	}

	public Optional<String> getMappedBy() {
		return mappedBy;
	}

	public Set<Cascade> getCascades() {
		return cascades;
	}

	public boolean isOwner() {
		return mappedBy.isEmpty();
	}

	public RelationshipJoinTable getJoinTable() {
		return joinTable;
	}

	public boolean toMany() {
		return false;
	}

	public boolean toOne() {
		return false;
	}

	public boolean fromOne() {
		return false;
	}

	public Class<?> getTargetEntityClass() {
		return targetEntityClass;
	}

	public JoinTableAttributes getJoinTableAttributes() {
		return joinTableAttributes;
	}

	public Optional<JoinColumnDataList> getJoinColumnDataList() {
		return joinColumnDataList;
	}

	public boolean isLazy() {
		return getFetchType() == FetchType.LAZY;
	}

	public Optional<JoinColumnMapping> getJoinColumnMapping() {
		return joinColumnMapping;
	}

	@Override
	public String toString() {
		return Relationship.class.getName() + ": fetchType=" + fetchType;
	}

	public boolean hasAnyCascades(Cascade... csds) {
		if (cascades == null || cascades.isEmpty())
			return false;

		for (Cascade c : csds) {
			if (cascades.contains(c))
				return true;
		}

		return false;
	}
}
