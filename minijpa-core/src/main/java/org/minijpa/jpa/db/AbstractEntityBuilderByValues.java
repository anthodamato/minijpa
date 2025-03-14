package org.minijpa.jpa.db;

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;
import org.minijpa.jpa.model.relationship.JoinColumnMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractEntityBuilderByValues extends AbstractEntityBuilder {
    private final Logger LOG = LoggerFactory.getLogger(AbstractEntityBuilderByValues.class);

    @Override
    public void buildBasicAttribute(
            MetaAttribute attribute,
            Object parentInstance,
            MetaEntity metaEntity,
            ModelValueArray<FetchParameter> modelValueArray) throws Exception {
        int index = AttributeUtil.indexOfAttribute(modelValueArray, attribute);
        if (index == -1)
            return;

        metaEntity.writeAttributeValue(
                parentInstance,
                parentInstance.getClass(),
                attribute,
                modelValueArray.getValue(index));
    }

    @Override
    public void buildAttributes(
            Object parentInstance,
            MetaEntity metaEntity,
            List<MetaAttribute> basicAttributes,
            List<RelationshipMetaAttribute> relationshipMetaAttributes,
            ModelValueArray<FetchParameter> modelValueArray,
            LockType lockType) throws Exception {
        for (MetaAttribute attribute : basicAttributes) {
            buildBasicAttribute(attribute, parentInstance, metaEntity, modelValueArray);
        }

        // load embeddables
        for (MetaEntity embeddable : metaEntity.getEmbeddables()) {
            Object parent = embeddable.getEntityClass().getDeclaredConstructor().newInstance();
            buildAttributes(parent, embeddable, embeddable.getBasicAttributes(),
                    embeddable.getRelationshipAttributes(), modelValueArray,
                    lockType);
            metaEntity.writeEmbeddableValue(parentInstance, parentInstance.getClass(), embeddable,
                    parent);
        }

        for (JoinColumnMapping joinColumnMapping : metaEntity.getJoinColumnMappings()) {
            LOG.debug("Build Attributes -> Join Column Mapping Attribute = {}", joinColumnMapping.getAttribute());
            LOG.debug("Build Attributes -> Join Column Mapping Foreign Key = {}", joinColumnMapping.getForeignKey());
            Object fk = joinColumnMapping.getForeignKey().buildValue(modelValueArray);
            if (joinColumnMapping.isLazy()) {
                joinColumnMapping.getAttribute().setForeignKeyValue(parentInstance, fk);
                continue;
            }

            MetaEntity toEntity = joinColumnMapping.getAttribute().getRelationship().getAttributeType();
            Object parent = build(modelValueArray, toEntity, lockType);
            metaEntity.writeAttributeValue(parentInstance, parentInstance.getClass(),
                    joinColumnMapping.getAttribute(), parent);
        }
    }

}
