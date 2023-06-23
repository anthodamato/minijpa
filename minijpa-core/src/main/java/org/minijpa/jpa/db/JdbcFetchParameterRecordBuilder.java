package org.minijpa.jpa.db;

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.JdbcRecordBuilder;
import org.minijpa.jdbc.JdbcRunner;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jpa.model.MetaEntity;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class JdbcFetchParameterRecordBuilder implements JdbcRecordBuilder {

    private List<FetchParameter> fetchParameters;
    private Collection<Object> collectionResult;
    private MetaEntity metaEntity;
    private EntityLoader entityLoader;

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

    @Override
    public void collectRecords(ResultSet rs) throws Exception {
        ResultSetMetaData metaData = rs.getMetaData();
        while (rs.next()) {
            Optional<ModelValueArray<FetchParameter>> optional = JdbcRunner
                    .createModelValueArrayFromResultSetAM(fetchParameters, rs, metaData);
            if (optional.isPresent()) {
                Object instance = entityLoader.build(optional.get(), metaEntity);
                collectionResult.add(instance);
            }
        }
    }
}