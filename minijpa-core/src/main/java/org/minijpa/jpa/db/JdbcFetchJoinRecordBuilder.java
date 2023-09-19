package org.minijpa.jpa.db;

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.JdbcRecordBuilder;
import org.minijpa.jdbc.JdbcRunner;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

public class JdbcFetchJoinRecordBuilder implements JdbcRecordBuilder {
    private final Logger log = LoggerFactory.getLogger(JdbcFetchJoinRecordBuilder.class);

    private List<FetchParameter> fetchParameters;
    private Collection<Object> collectionResult;
    private MetaEntity metaEntity;
    private EntityLoader entityLoader;
    private List<MetaEntity> fetchJoinMetaEntities;
    private List<RelationshipMetaAttribute> fetchJoinMetaAttributes;
    private final Map<RelationshipMetaAttribute, Set<Object>> relationshipAttributeIds = new HashMap<>();
    private boolean distinct = false;

    public void setFetchParameters(List<FetchParameter> fetchParameters) {
        this.fetchParameters = fetchParameters;
    }

    public void setCollectionResult(Collection<Object> collectionResult) {
        this.collectionResult = collectionResult;
    }

    public void setMetaEntity(MetaEntity metaEntity) {
        this.metaEntity = metaEntity;
    }

    public void setEntityLoader(EntityLoader entityLoader) {
        this.entityLoader = entityLoader;
    }

    public void setFetchJoinMetaEntities(
            List<MetaEntity> fetchJoinMetaEntities) {
        this.fetchJoinMetaEntities = fetchJoinMetaEntities;
    }

    public void setFetchJoinMetaAttributes(
            List<RelationshipMetaAttribute> fetchJoinMetaAttributes) {
        this.fetchJoinMetaAttributes = fetchJoinMetaAttributes;
        relationshipAttributeIds.clear();
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    private void addRelationshipEntityId(
            RelationshipMetaAttribute relationshipMetaAttribute,
            Object id) {
        Set<Object> idSet = relationshipAttributeIds.get(relationshipMetaAttribute);
        if (idSet == null) {
            idSet = new HashSet<>();
            relationshipAttributeIds.put(relationshipMetaAttribute, idSet);
        }

        idSet.add(id);
    }

    private boolean isRelationshipEntityIdPresent(
            RelationshipMetaAttribute relationshipMetaAttribute,
            Object id) {
        Set<Object> idSet = relationshipAttributeIds.get(relationshipMetaAttribute);
        return idSet != null && !idSet.isEmpty() && idSet.contains(id);
    }

    @Override
    public void collectRecords(ResultSet rs) throws Exception {
        ResultSetMetaData metaData = rs.getMetaData();
        while (rs.next()) {
            Optional<ModelValueArray<FetchParameter>> optional = JdbcRunner
                    .createModelValueArrayFromResultSetAM(fetchParameters, rs, metaData);
            log.debug("collectRecords: optional={}", optional);
            if (optional.isEmpty())
                continue;

            Object instance = entityLoader.buildEntityNoRelationshipAttributeLoading(optional.get(),
                    metaEntity);
            if (distinct) {
                if (!collectionResult.contains(instance)) {
                    collectionResult.add(instance);
                    // set lazy loaded flag for those attributes
                    for (RelationshipMetaAttribute fetchJoinMetaAttribute : fetchJoinMetaAttributes) {
                        MetaEntityHelper.lazyAttributeLoaded(metaEntity, fetchJoinMetaAttribute,
                                instance, true);
                    }
                }
            } else {
                collectionResult.add(instance);
                // set lazy loaded flag for those attributes
                for (RelationshipMetaAttribute fetchJoinMetaAttribute : fetchJoinMetaAttributes) {
                    MetaEntityHelper.lazyAttributeLoaded(metaEntity, fetchJoinMetaAttribute,
                            instance, true);
                }
            }

            // set relationship attribute values
            for (int i = 0; i < fetchJoinMetaEntities.size(); ++i) {
                Object value = entityLoader.buildEntityNoRelationshipAttributeLoading(optional.get(),
                        fetchJoinMetaEntities.get(i));
                log.debug("collectRecords: Relationship Attribute Values value={}", value);
                Object id = AttributeUtil.getIdValue(fetchJoinMetaEntities.get(i), value);
                if (!isRelationshipEntityIdPresent(fetchJoinMetaAttributes.get(i), id)) {
                    MetaEntityHelper.addElementToCollectionAttribute(instance, metaEntity,
                            fetchJoinMetaAttributes.get(i), value);
                    addRelationshipEntityId(fetchJoinMetaAttributes.get(i), id);
                }
            }
        }
    }
}