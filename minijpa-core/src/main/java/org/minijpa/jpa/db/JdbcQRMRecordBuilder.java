package org.minijpa.jpa.db;

import org.minijpa.jdbc.*;
import org.minijpa.jdbc.mapper.AttributeMapper;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.db.querymapping.ConstructorMapping;
import org.minijpa.jpa.db.querymapping.EntityMapping;
import org.minijpa.jpa.db.querymapping.QueryResultMapping;
import org.minijpa.jpa.db.querymapping.SingleColumnMapping;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.relationship.JoinColumnAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcQRMRecordBuilder implements JdbcRecordBuilder {
    protected Logger log = LoggerFactory.getLogger(JdbcQRMRecordBuilder.class);
    private List<Object> objects;
    private QueryResultMapping queryResultMapping;
    private LockType lockType;
    private final EntityBuilder managedEntityBuilderByValues = new ManagedEntityBuilderByValues();
    private EntityContainer entityContainer;

    public void setQueryResultMapping(QueryResultMapping queryResultMapping) {
        this.queryResultMapping = queryResultMapping;
    }

    public void setLockType(LockType lockType) {
        this.lockType = lockType;
    }

    public void setCollection(List<Object> objects) {
        this.objects = objects;
    }

    public void setEntityContainer(EntityContainer entityContainer) {
        this.entityContainer = entityContainer;
    }

    @Override
    public void collectRecords(ResultSet rs) throws Exception {
        ResultSetMetaData metaData = rs.getMetaData();
        while (rs.next()) {
            Object record = buildRecord(metaData, rs, queryResultMapping);
            objects.add(record);
        }
    }

    private Object buildRecord(
            ResultSetMetaData metaData,
            ResultSet rs,
            QueryResultMapping queryResultMapping) throws Exception {
        ModelValueArray<FetchParameter> modelValueArray = buildEntityMappingFetchParameters(metaData, rs);
        int k = 0;
        Object[] result = new Object[queryResultMapping.size()];
        for (EntityMapping entityMapping : queryResultMapping.getEntityMappings()) {
            Object entityInstance = managedEntityBuilderByValues.build(
                    modelValueArray, entityMapping.getMetaEntity(), lockType);
            result[k] = entityInstance;
            ++k;
        }

        for (ConstructorMapping constructorMapping : queryResultMapping.getConstructorMappings()) {
            List<Object> values = buildConstructorMappingValues(
                    constructorMapping, metaData, rs);
            Object v = buildObjectByConstructorParameters(constructorMapping.getTargetClass(), values);
            Optional<MetaEntity> optionalMetaEntity = entityContainer.isManagedClass(constructorMapping.getTargetClass());
            if (optionalMetaEntity.isPresent()) {
                Object pk = AttributeUtil.getIdValue(optionalMetaEntity.get().getId(), v);
                if (pk != null) {
                    MetaEntityHelper.setEntityStatus(optionalMetaEntity.get(), v, EntityStatus.DETACHED);
                }
            }

            result[k] = v;
            ++k;
        }

        if (!queryResultMapping.getSingleColumnMappings().isEmpty()) {
            List<Object> values = buildSingleColumnMappingValues(queryResultMapping.getSingleColumnMappings(), metaData, rs);
            for (Object v : values) {
                result[k] = v;
                ++k;
            }
        }

        if (result.length == 1) {
            return result[0];
        }

        return result;
    }

    private ModelValueArray<FetchParameter> buildEntityMappingFetchParameters(
            ResultSetMetaData metaData,
            ResultSet rs) throws Exception {
        ModelValueArray<FetchParameter> modelValueArray = new ModelValueArray<>();
        int nc = metaData.getColumnCount();
        for (int i = 0; i < nc; ++i) {
            String columnName = metaData.getColumnName(i + 1);
            String columnAlias = metaData.getColumnLabel(i + 1);
            Optional<FetchParameter> optional = findFetchParameter(columnName, columnAlias,
                    queryResultMapping.getEntityMappings());
            if (optional.isPresent()) {
                Object v = buildAttributeValue(optional.get(), metaData, rs, i + 1);
                modelValueArray.add(optional.get(), v);
            }
        }

        return modelValueArray;
    }

    private Object buildAttributeValue(FetchParameter fetchParameter,
                                       ResultSetMetaData metaData,
                                       ResultSet rs,
                                       int index) throws Exception {
        Optional<AttributeMapper> optionalAM = fetchParameter.getAttributeMapper();
        Integer sqlType = fetchParameter.getSqlType();
        if (sqlType == null) {
            sqlType = metaData.getColumnType(index);
        }

        return JdbcRunner.getValueByAttributeMapper(rs, index, sqlType, optionalAM);
    }

    private Optional<FetchParameter> findFetchParameter(String columnName, String columnAlias,
                                                        List<EntityMapping> entityMappings) {
        for (EntityMapping entityMapping : entityMappings) {
            Optional<FetchParameter> optional = buildFetchParameter(columnName, columnAlias,
                    entityMapping);
            if (optional.isPresent()) {
                return optional;
            }
        }

        return Optional.empty();
    }

    private Optional<FetchParameter> buildFetchParameter(
            String columnName,
            String columnAlias,
            EntityMapping entityMapping) {
        Optional<MetaAttribute> optional = entityMapping.getAttribute(columnAlias);
        if (optional.isPresent()) {
            MetaAttribute metaAttribute = optional.get();
            FetchParameter fetchParameter = new AttributeFetchParameterImpl(
                    metaAttribute.getColumnName(),
                    metaAttribute.getSqlType(),
                    metaAttribute,
                    metaAttribute.getAttributeMapper());
            return Optional.of(fetchParameter);
        }

        Optional<JoinColumnAttribute> optionalJoinColumn = entityMapping.getJoinColumnAttribute(
                columnName);
        if (optionalJoinColumn.isPresent()) {
            JoinColumnAttribute joinColumnAttribute = optionalJoinColumn.get();
            FetchParameter fetchParameter = new AttributeFetchParameterImpl(
                    joinColumnAttribute.getColumnName(),
                    joinColumnAttribute.getSqlType(),
                    joinColumnAttribute.getAttribute(),
                    Optional.empty());
            return Optional.of(fetchParameter);
        }

        return Optional.empty();
    }

    private List<Object> buildConstructorMappingValues(
            ConstructorMapping constructorMapping,
            ResultSetMetaData metaData,
            ResultSet rs) throws Exception {
        List<Object> constructorValues = new ArrayList<>();
        int nc = metaData.getColumnCount();
        for (int i = 0; i < nc; ++i) {
            String columnName = metaData.getColumnName(i + 1);
            String columnAlias = metaData.getColumnLabel(i + 1);
            Optional<SingleColumnMapping> optional = constructorMapping.findColumnMapping(columnAlias);
            if (optional.isPresent()) {
                Class<?> type = optional.get().getType();
                int sqlType = type != void.class ? JdbcTypes.sqlTypeFromClass(type) : metaData.getColumnType(i + 1);
                if (sqlType == Types.NULL)
                    sqlType = metaData.getColumnType(i + 1);

                Object v = JdbcRunner.getValue(rs, i + 1, sqlType);
                constructorValues.add(v);
            }
        }

        return constructorValues;
    }

    private Object buildObjectByConstructorParameters(
            Class<?> targetClass,
            List<Object> params) throws Exception {
        Class<?>[] classes = new Class[params.size()];
        for (int i = 0; i < params.size(); ++i) {
            classes[i] = params.get(i).getClass();
        }

        try {
            Constructor<?> constructor = classes.length > 0 ? targetClass.getConstructor(classes) : targetClass.getConstructor();
            return constructor.newInstance(params.toArray());
        } catch (NoSuchMethodException e) {
            if (classes.length == 0)
                throw new Exception("Constructor not found: targetClass=" + targetClass.getName());

            Class<?>[] types = convertToPrimitiveTypes(params);
            try {
                Constructor<?> constructor = targetClass.getConstructor(types);
                return constructor.newInstance(params.toArray());
            } catch (NoSuchMethodException e2) {
                throw new Exception("Constructor not found: targetClass=" + targetClass.getName());
            }
        }
    }

    private Class<?>[] convertToPrimitiveTypes(List<Object> params) {
        Class<?>[] types = new Class[params.size()];
        for (int i = 0; i < params.size(); ++i) {
            Object obj = params.get(i);
            if (obj instanceof Number) {
                if (obj instanceof Byte) {
                    types[i] = byte.class;
                } else if (obj instanceof Double) {
                    types[i] = double.class;
                } else if (obj instanceof Float) {
                    types[i] = float.class;
                } else if (obj instanceof Integer) {
                    types[i] = int.class;
                } else if (obj instanceof Long) {
                    types[i] = long.class;
                } else if (obj instanceof Short) {
                    types[i] = short.class;
                }
            } else {
                types[i] = obj.getClass();
            }
        }

        return types;
    }

    private List<Object> buildSingleColumnMappingValues(
            List<SingleColumnMapping> singleColumnMappings,
            ResultSetMetaData metaData,
            ResultSet rs) throws Exception {
        List<Object> values = new ArrayList<>();
        int nc = metaData.getColumnCount();
        for (SingleColumnMapping singleColumnMapping : singleColumnMappings) {
            for (int i = 0; i < nc; ++i) {
                String columnName = metaData.getColumnName(i + 1);
                String columnAlias = metaData.getColumnLabel(i + 1);
                if (singleColumnMapping.getName().equalsIgnoreCase(columnAlias)) {
                    Class<?> type = singleColumnMapping.getType();
                    int sqlType = type != void.class ? JdbcTypes.sqlTypeFromClass(type) : metaData.getColumnType(i + 1);
                    if (sqlType == Types.NULL)
                        sqlType = metaData.getColumnType(i + 1);

                    Object v = JdbcRunner.getValue(rs, i + 1, sqlType);
                    values.add(v);
                }
            }
        }

        return values;
    }

    private class ManagedEntityBuilderByValues extends org.minijpa.jpa.db.AbstractEntityBuilderByValues {
        @Override
        public Object build(
                ModelValueArray<FetchParameter> modelValueArray,
                MetaEntity entity,
                LockType lockType) throws Exception {
            Object primaryKey = AttributeUtil.buildPK(entity.getId(), modelValueArray);
            log.debug("buildEntityByValues: primaryKey={}", primaryKey);
            log.debug("buildEntityByValues: entity={}", entity);
            Object entityInstance = entityContainer.find(entity.getEntityClass(), primaryKey);
            if (entityInstance != null) {
                return entityInstance;
            }

            entityInstance = buildInstance(entity, primaryKey);
            buildAttributes(entityInstance, entity, entity.getBasicAttributes(),
                    entity.getRelationshipAttributes(), modelValueArray, lockType);
            entityContainer.addManaged(entityInstance, primaryKey);
            MetaEntityHelper.setEntityStatus(entity, entityInstance, EntityStatus.FLUSHED_LOADED_FROM_DB);
            buildCircularRelationships(entity, entityInstance);
            return entityInstance;
        }

    }
}

