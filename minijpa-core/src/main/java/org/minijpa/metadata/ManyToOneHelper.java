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
package org.minijpa.metadata;

import java.util.Optional;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.minijpa.jdbc.relationship.JoinColumnDataList;
import org.minijpa.jpa.db.DbConfiguration;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.relationship.JoinColumnMapping;
import org.minijpa.jpa.model.relationship.ManyToOneRelationship;

/**
 *
 * @author adamato
 */
public class ManyToOneHelper extends RelationshipHelper {

	private final JoinColumnMappingFactory joinColumnMappingFactory = new OwningJoinColumnMappingFactory();

	public ManyToOneRelationship createManyToOne(
			ManyToOne manyToOne,
			Optional<JoinColumnDataList> joinColumnDataList) {
		ManyToOneRelationship.Builder builder = new ManyToOneRelationship.Builder();
		builder.withJoinColumnDataList(joinColumnDataList);

		builder.withCascades(getCascades(manyToOne.cascade()));
		if (manyToOne.fetch() != null)
			if (manyToOne.fetch() == FetchType.EAGER)
				builder = builder.withFetchType(org.minijpa.jpa.model.relationship.FetchType.EAGER);
			else if (manyToOne.fetch() == FetchType.LAZY)
				builder = builder.withFetchType(org.minijpa.jpa.model.relationship.FetchType.LAZY);

		return builder.build();
	}

	public ManyToOneRelationship finalizeRelationship(
			ManyToOneRelationship manyToOneRelationship,
			MetaAttribute a,
			MetaEntity entity,
			MetaEntity toEntity,
			DbConfiguration dbConfiguration) {
		ManyToOneRelationship.Builder builder = new ManyToOneRelationship.Builder().with(manyToOneRelationship);
		if (manyToOneRelationship.isOwner()) {
			JoinColumnMapping joinColumnMapping = joinColumnMappingFactory.buildJoinColumnMapping(
					dbConfiguration, a, toEntity, manyToOneRelationship.getJoinColumnDataList());
			entity.getJoinColumnMappings().add(joinColumnMapping);
			builder.withJoinColumnMapping(Optional.of(joinColumnMapping));
		}

		builder.withAttributeType(toEntity);
		return builder.build();
	}
}
