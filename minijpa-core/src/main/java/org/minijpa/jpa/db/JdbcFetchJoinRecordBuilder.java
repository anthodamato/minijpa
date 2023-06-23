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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class JdbcFetchJoinRecordBuilder implements JdbcRecordBuilder {
    private final Logger log = LoggerFactory.getLogger(JdbcFetchJoinRecordBuilder.class);

    private List<FetchParameter> fetchParameters;
    private Collection<Object> collectionResult;
    private MetaEntity metaEntity;
    private EntityLoader entityLoader;
    private List<MetaEntity> fetchJoinMetaEntities;
    private List<RelationshipMetaAttribute> fetchJoinMetaAttributes;
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
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public void collectRecords(ResultSet rs) throws Exception {
        ResultSetMetaData metaData = rs.getMetaData();
        while (rs.next()) {
            Optional<ModelValueArray<FetchParameter>> optional = JdbcRunner
                    .createModelValueArrayFromResultSetAM(fetchParameters, rs, metaData);
            log.debug("collectRecords: optional={}", optional);
            if (optional.isPresent()) {
                Object instance = entityLoader.buildEntityNoRelationshipAttributeLoading(optional.get(),
                        metaEntity);
                if (distinct) {
                    if (!collectionResult.contains(instance)) {
                        collectionResult.add(instance);
                        // set lazy loaded flag for those attributes
                        for (int i = 0; i < fetchJoinMetaAttributes.size(); ++i) {
                            MetaEntityHelper.lazyAttributeLoaded(metaEntity, fetchJoinMetaAttributes.get(i),
                                    instance, true);
                        }
                    }
                } else {
                    collectionResult.add(instance);
                    // set lazy loaded flag for those attributes
                    for (int i = 0; i < fetchJoinMetaAttributes.size(); ++i) {
                        MetaEntityHelper.lazyAttributeLoaded(metaEntity, fetchJoinMetaAttributes.get(i),
                                instance, true);
                    }
                }

                // set relationship attribute values
                for (int i = 0; i < fetchJoinMetaEntities.size(); ++i) {
                    Object value = entityLoader.buildEntityNoRelationshipAttributeLoading(optional.get(),
                            fetchJoinMetaEntities.get(i));
                    MetaEntityHelper.addElementToCollectionAttribute(instance, metaEntity,
                            fetchJoinMetaAttributes.get(i), value);
                }
            }
        }
    }
}