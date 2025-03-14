package org.minijpa.jpa.db;

import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEntityBuilder implements EntityBuilder {
    private final Logger LOG = LoggerFactory.getLogger(AbstractEntityBuilder.class);

    @Override
    public Object buildInstance(MetaEntity metaEntity, Object primaryKey) throws Exception {
        return metaEntity.buildInstance(primaryKey);
    }


    @Override
    public void buildCircularRelationships(MetaEntity entity, Object entityInstance) throws Exception {
        LOG.debug("Building Relationships -> Entity = {}", entity);
        LOG.debug("Building Relationships -> Entity Instance = {}", entityInstance);
        for (RelationshipMetaAttribute a : entity.getRelationshipAttributes()) {
            if (!a.isEager()) {
                continue;
            }

            if (a.getRelationship().toOne() && a.getRelationship().isOwner()) {
                LOG.debug("Building Relationships -> Attribute = {}", a);
                Object value = a.getValue(entityInstance);
                LOG.debug("Building Relationships -> Value = {}", value);
                if (value == null) {
                    continue;
                }

                RelationshipMetaAttribute targetAttribute = a.getRelationship().getTargetAttribute();
                LOG.debug("Building Relationships -> Target Attribute = {}", targetAttribute);
                LOG.debug("Building Relationships -> Attribute Type = {}",
                        a.getRelationship().getAttributeType());
                MetaEntity toEntity = a.getRelationship().getAttributeType();
                if (toEntity == null) {
                    continue;
                }

                RelationshipMetaAttribute attribute = toEntity.findAttributeByMappedBy(a.getName());
                LOG.debug("Building Relationships -> Attribute = {}", attribute);
                if (attribute == null) {
                    continue;
                }

                // it's bidirectional
                if (attribute.getRelationship().toOne()) {
                    Object v = attribute.getValue(value);
                    LOG.debug("Building Relationships -> Attribute Value = {}", v);
                    if (v == null) {
                        entity.writeAttributeValue(value, value.getClass(), attribute,
                                entityInstance);
                    }
                }
            }
        }
    }
}
