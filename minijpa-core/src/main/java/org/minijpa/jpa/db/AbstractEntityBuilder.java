package org.minijpa.jpa.db;

import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEntityBuilder implements EntityBuilder {
    private final Logger LOG = LoggerFactory.getLogger(AbstractEntityBuilder.class);

    @Override
    public Object buildInstance(MetaEntity metaEntity, Object primaryKey) throws Exception {
        return MetaEntityHelper.build(metaEntity, primaryKey);
    }

    @Override
    public Object buildInstance(MetaEntity metaEntity) throws Exception {
        return MetaEntityHelper.build(metaEntity);
    }

    @Override
    public void buildCircularRelationships(MetaEntity entity, Object entityInstance) throws Exception {
        LOG.debug("buildCircularRelationships: entity={}", entity);
        LOG.debug("buildCircularRelationships: entityInstance={}", entityInstance);
        for (RelationshipMetaAttribute a : entity.getRelationshipAttributes()) {
            if (!a.isEager()) {
                continue;
            }

            if (a.getRelationship().toOne() && a.getRelationship().isOwner()) {
                LOG.debug("buildCircularRelationships: a={}", a);
                Object value = MetaEntityHelper.getAttributeValue(entityInstance, a);
                LOG.debug("buildCircularRelationships: value={}", value);
                if (value == null) {
                    continue;
                }

                RelationshipMetaAttribute targetAttribute = a.getRelationship().getTargetAttribute();
                LOG.debug("buildCircularRelationships: targetAttribute={}", targetAttribute);
                LOG.debug("buildCircularRelationships: a.getRelationship().getAttributeType()={}",
                        a.getRelationship().getAttributeType());
                MetaEntity toEntity = a.getRelationship().getAttributeType();
                if (toEntity == null) {
                    continue;
                }

                RelationshipMetaAttribute attribute = toEntity.findAttributeByMappedBy(a.getName());
                LOG.debug("buildCircularRelationships: attribute={}", attribute);
                if (attribute == null) {
                    continue;
                }

                // it's bidirectional
                if (attribute.getRelationship().toOne()) {
                    Object v = MetaEntityHelper.getAttributeValue(value, attribute);
                    LOG.debug("buildCircularRelationships: v={}", v);
                    if (v == null) {
                        MetaEntityHelper.writeMetaAttributeValue(value, value.getClass(), attribute,
                                entityInstance,
                                toEntity);
                    }
                }
            }
        }
    }
}
