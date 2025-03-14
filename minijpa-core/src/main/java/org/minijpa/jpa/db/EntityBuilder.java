package org.minijpa.jpa.db;

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;

import java.util.List;

public interface EntityBuilder {
    /**
     * Create the entity instance.
     *
     * @return
     */
    Object buildInstance(MetaEntity metaEntity, Object primaryKey) throws Exception;


    void buildBasicAttribute(
            MetaAttribute attribute,
            Object parentInstance,
            MetaEntity metaEntity,
            ModelValueArray<FetchParameter> modelValueArray) throws Exception;

    /**
     * Build basic and relationship attributes.
     *
     * @param parentInstance
     * @param metaEntity
     * @param basicAttributes
     * @param relationshipMetaAttributes
     * @param modelValueArray
     * @param lockType
     */
    void buildAttributes(
            Object parentInstance,
            MetaEntity metaEntity,
            List<MetaAttribute> basicAttributes,
            List<RelationshipMetaAttribute> relationshipMetaAttributes,
            ModelValueArray<FetchParameter> modelValueArray,
            LockType lockType) throws Exception;

    void buildCircularRelationships(MetaEntity entity, Object entityInstance)
            throws Exception;

    Object build(
            ModelValueArray<FetchParameter> modelValueArray,
            MetaEntity entity,
            LockType lockType) throws Exception;

}
