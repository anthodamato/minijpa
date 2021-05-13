/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.metadata;

import java.util.Optional;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.relationship.JoinColumnDataList;
import org.minijpa.jdbc.relationship.JoinColumnMapping;
import org.minijpa.jdbc.relationship.ManyToOneRelationship;

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

	if (manyToOne.fetch() != null)
	    if (manyToOne.fetch() == FetchType.EAGER)
		builder = builder.withFetchType(org.minijpa.jdbc.relationship.FetchType.EAGER);
	    else if (manyToOne.fetch() == FetchType.LAZY)
		builder = builder.withFetchType(org.minijpa.jdbc.relationship.FetchType.LAZY);

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
	    JoinColumnMapping joinColumnMapping = joinColumnMappingFactory.buildJoinColumnMapping(dbConfiguration, a, toEntity, manyToOneRelationship.getJoinColumnDataList());
	    entity.getJoinColumnMappings().add(joinColumnMapping);
	    builder.withJoinColumnMapping(Optional.of(joinColumnMapping));
	}

	builder.withAttributeType(toEntity);
	return builder.build();
    }
}
