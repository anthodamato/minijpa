/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
