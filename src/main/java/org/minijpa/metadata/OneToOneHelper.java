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
import javax.persistence.OneToOne;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.relationship.JoinColumnDataList;
import org.minijpa.jdbc.relationship.JoinColumnMapping;
import org.minijpa.jdbc.relationship.OneToOneRelationship;

/**
 *
 * @author adamato
 */
public class OneToOneHelper extends RelationshipHelper {

    private final JoinColumnMappingFactory joinColumnMappingFactory = new OwningJoinColumnMappingFactory();

    public OneToOneRelationship createOneToOne(
	    OneToOne oneToOne,
	    Optional<JoinColumnDataList> joinColumnDataList) {
	OneToOneRelationship.Builder builder = new OneToOneRelationship.Builder();
	builder.withJoinColumnDataList(joinColumnDataList);

	builder.withMappedBy(getMappedBy(oneToOne));
	builder.withCascades(getCascades(oneToOne.cascade()));

	if (oneToOne.fetch() != null)
	    if (oneToOne.fetch() == FetchType.EAGER)
		builder.withFetchType(org.minijpa.jdbc.relationship.FetchType.EAGER);
	    else if (oneToOne.fetch() == FetchType.LAZY)
		builder.withFetchType(org.minijpa.jdbc.relationship.FetchType.LAZY);

	return builder.build();
    }

    public OneToOneRelationship finalizeRelationship(OneToOneRelationship oneToOneRelationship, MetaAttribute a,
	    MetaEntity entity, MetaEntity toEntity, DbConfiguration dbConfiguration) {
	OneToOneRelationship.Builder builder = new OneToOneRelationship.Builder().with(oneToOneRelationship);
	if (oneToOneRelationship.isOwner()) {
	    JoinColumnMapping joinColumnMapping = joinColumnMappingFactory.buildJoinColumnMapping(
		    dbConfiguration, a, toEntity, oneToOneRelationship.getJoinColumnDataList());
	    entity.getJoinColumnMappings().add(joinColumnMapping);
	    builder.withTargetAttribute(toEntity.findAttributeByMappedBy(a.getName()));
	    builder.withJoinColumnMapping(Optional.of(joinColumnMapping));
	} else {
	    builder.withOwningEntity(toEntity);
	    builder.withOwningAttribute(toEntity.getAttribute(oneToOneRelationship.getMappedBy().get()));
	}

	builder.withAttributeType(toEntity);
	return builder.build();
    }
}
