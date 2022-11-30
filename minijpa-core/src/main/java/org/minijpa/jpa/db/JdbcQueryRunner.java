package org.minijpa.jpa.db;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.db.SqlSelectData;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.model.AbstractAttribute;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.Pk;
import org.minijpa.jpa.model.relationship.Relationship;
import org.minijpa.jpa.model.relationship.RelationshipJoinTable;
import org.minijpa.metadata.AliasGenerator;
import org.minijpa.sql.model.FromTable;
import org.minijpa.sql.model.SqlDelete;
import org.minijpa.sql.model.SqlInsert;
import org.minijpa.sql.model.SqlUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcQueryRunner {
    private Logger LOG = LoggerFactory.getLogger(JdbcQueryRunner.class);
    private ConnectionHolder connectionHolder;
    private DbConfiguration dbConfiguration;
    private SqlStatementFactory sqlStatementFactory;
    private AliasGenerator aliasGenerator;
    private final JpaJdbcRunner.JdbcValueBuilderById jdbcValueBuilderById = new JpaJdbcRunner.JdbcValueBuilderById();
    private JpaJdbcRunner.JdbcFPRecordBuilder jdbcFPRecordBuilder = new JpaJdbcRunner.JdbcFPRecordBuilder();

    public JdbcQueryRunner(ConnectionHolder connectionHolder, DbConfiguration dbConfiguration,
            SqlStatementFactory sqlStatementFactory, AliasGenerator tableAliasGenerator) {
        super();
        this.connectionHolder = connectionHolder;
        this.dbConfiguration = dbConfiguration;
        this.sqlStatementFactory = sqlStatementFactory;
        this.aliasGenerator = tableAliasGenerator;
    }

    public Optional<ModelValueArray<FetchParameter>> findById(MetaEntity entity, Object primaryKey, LockType lockType)
            throws Exception {
        SqlSelectData sqlSelectData = sqlStatementFactory.generateSelectById(entity, lockType, aliasGenerator);
        List<QueryParameter> parameters = MetaEntityHelper.convertAVToQP(entity.getId(), primaryKey);
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelectData);
        jdbcValueBuilderById.setFetchParameters(sqlSelectData.getFetchParameters());
        return dbConfiguration.getJdbcRunner().findById(sql, connectionHolder.getConnection(), parameters,
                jdbcValueBuilderById);
    }

    public Optional<ModelValueArray<FetchParameter>> runVersionQuery(MetaEntity entity, Object primaryKey,
            LockType lockType) throws Exception {
        SqlSelectData sqlSelectData = sqlStatementFactory.generateSelectVersion(entity, lockType, aliasGenerator);
        List<QueryParameter> parameters = MetaEntityHelper.convertAVToQP(entity.getId(), primaryKey);
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelectData);
        jdbcValueBuilderById.setFetchParameters(sqlSelectData.getFetchParameters());
        return dbConfiguration.getJdbcRunner().findById(sql, connectionHolder.getConnection(), parameters,
                jdbcValueBuilderById);
    }

    public Object selectByForeignKey(MetaEntity entity, MetaAttribute foreignKeyAttribute, Object foreignKey,
            LockType lockType, EntityHandler entityLoader) throws Exception {
        List<QueryParameter> parameters = MetaEntityHelper.convertAVToQP(foreignKeyAttribute, foreignKey);
        List<String> columns = parameters.stream().map(p -> p.getColumnName()).collect(Collectors.toList());
        SqlSelectData sqlSelectData = sqlStatementFactory.generateSelectByForeignKey(entity, foreignKeyAttribute,
                columns, aliasGenerator);
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelectData);
        Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(null,
                CollectionUtils.findCollectionImplementationClass(List.class));
        entityLoader.setLockType(lockType);

        jdbcFPRecordBuilder.setCollectionResult(collectionResult);
        jdbcFPRecordBuilder.setEntityLoader(entityLoader);
        jdbcFPRecordBuilder.setMetaEntity(entity);
        jdbcFPRecordBuilder.setFetchParameters(sqlSelectData.getFetchParameters());
        dbConfiguration.getJdbcRunner().runQuery(connectionHolder.getConnection(), sql, parameters,
                jdbcFPRecordBuilder);
        return (List<Object>) collectionResult;
    }

    public Object selectByJoinTable(Object primaryKey, Pk id, Relationship relationship, MetaAttribute metaAttribute,
            EntityHandler entityLoader) throws Exception {
        ModelValueArray<AbstractAttribute> modelValueArray = null;
        SqlSelectData sqlSelectData = null;
        if (relationship.isOwner()) {
            modelValueArray = sqlStatementFactory.expandJoinColumnAttributes(id, primaryKey,
                    relationship.getJoinTable().getOwningJoinColumnMapping().getJoinColumnAttributes());
            List<AbstractAttribute> attributes = modelValueArray.getModels();
            sqlSelectData = sqlStatementFactory.generateSelectByJoinTable(relationship.getAttributeType(),
                    relationship.getJoinTable(), attributes, aliasGenerator);
        } else {
            modelValueArray = sqlStatementFactory.expandJoinColumnAttributes(id, primaryKey,
                    relationship.getJoinTable().getTargetJoinColumnMapping().getJoinColumnAttributes());
            List<AbstractAttribute> attributes = modelValueArray.getModels();
            sqlSelectData = sqlStatementFactory.generateSelectByJoinTableFromTarget(relationship.getAttributeType(),
                    relationship.getJoinTable(), attributes, aliasGenerator);
        }

        List<QueryParameter> parameters = MetaEntityHelper.convertAbstractAVToQP(modelValueArray);
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelectData);
        Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(null,
                metaAttribute.getCollectionImplementationClass());
        entityLoader.setLockType(LockType.NONE);
        jdbcFPRecordBuilder.setCollectionResult(collectionResult);
        jdbcFPRecordBuilder.setEntityLoader(entityLoader);
        jdbcFPRecordBuilder.setMetaEntity(relationship.getAttributeType());
        jdbcFPRecordBuilder.setFetchParameters(sqlSelectData.getFetchParameters());
        dbConfiguration.getJdbcRunner().runQuery(connectionHolder.getConnection(), sql, parameters,
                jdbcFPRecordBuilder);
        return collectionResult;
    }

    public Object insertWithIdentityColumn(MetaEntity entity, Object entityInstance, List<QueryParameter> parameters,
            boolean isIdentityColumnNull) throws Exception {
        List<String> columns = parameters.stream().map(p -> {
            return p.getColumnName();
        }).collect(Collectors.toList());
        Pk pk = entity.getId();
        SqlInsert sqlInsert = sqlStatementFactory.generateInsert(entity, columns, true, isIdentityColumnNull,
                Optional.of(entity), aliasGenerator);
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlInsert);
        return dbConfiguration.getJdbcRunner().insertReturnGeneratedKeys(connectionHolder.getConnection(), sql,
                parameters, pk.getAttribute().getColumnName());
    }

    public void insert(MetaEntity entity, Object entityInstance, List<QueryParameter> parameters) throws Exception {
        List<String> columns = parameters.stream().map(p -> {
            return p.getColumnName();
        }).collect(Collectors.toList());

        SqlInsert sqlInsert = sqlStatementFactory.generateInsert(entity, columns, false, false, Optional.empty(),
                aliasGenerator);
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlInsert);
        dbConfiguration.getJdbcRunner().insert(connectionHolder.getConnection(), sql, parameters);
    }

    public void deleteById(MetaEntity e, List<QueryParameter> idParameters) throws Exception {
        List<String> idColumns = idParameters.stream().map(p -> p.getColumnName()).collect(Collectors.toList());
        SqlDelete sqlDelete = sqlStatementFactory.generateDeleteById(e, idColumns, aliasGenerator);
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlDelete);
        dbConfiguration.getJdbcRunner().delete(sql, connectionHolder.getConnection(), idParameters);
    }

    public int update(MetaEntity entity, List<QueryParameter> parameters, List<String> columns, List<String> idColumns)
            throws Exception {
        SqlUpdate sqlUpdate = sqlStatementFactory.generateUpdate(entity, columns, idColumns, aliasGenerator);
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlUpdate);
        return dbConfiguration.getJdbcRunner().update(connectionHolder.getConnection(), sql, parameters);
    }

    public void insertJoinTableAttribute(RelationshipJoinTable relationshipJoinTable, Object entityInstance,
            Object instance) throws Exception {
        List<QueryParameter> parameters = sqlStatementFactory
                .createRelationshipJoinTableParameters(relationshipJoinTable, entityInstance, instance);
        List<String> columnNames = parameters.stream().map(p -> p.getColumnName()).collect(Collectors.toList());
        SqlInsert sqlInsert = sqlStatementFactory.generateJoinTableInsert(relationshipJoinTable, columnNames);
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlInsert);
        dbConfiguration.getJdbcRunner().insert(connectionHolder.getConnection(), sql, parameters);
    }

    public void removeJoinTableRecords(MetaEntity entity, Object primaryKey, List<QueryParameter> idParameters,
            RelationshipJoinTable relationshipJoinTable) throws Exception {
        if (relationshipJoinTable.getOwningEntity() == entity) {
            ModelValueArray<AbstractAttribute> modelValueArray = sqlStatementFactory.expandJoinColumnAttributes(
                    entity.getId(), primaryKey,
                    relationshipJoinTable.getOwningJoinColumnMapping().getJoinColumnAttributes());
            List<AbstractAttribute> attributes = modelValueArray.getModels();

            List<String> idColumns = attributes.stream().map(p -> p.getColumnName()).collect(Collectors.toList());
            FromTable fromTable = FromTable.of(relationshipJoinTable.getTableName());
            SqlDelete sqlDelete = sqlStatementFactory.generateDeleteById(fromTable, idColumns);
            String sql = dbConfiguration.getSqlStatementGenerator().export(sqlDelete);
            dbConfiguration.getJdbcRunner().delete(sql, connectionHolder.getConnection(), idParameters);
        } else {
            ModelValueArray<AbstractAttribute> modelValueArray = sqlStatementFactory.expandJoinColumnAttributes(
                    entity.getId(), primaryKey,
                    relationshipJoinTable.getTargetJoinColumnMapping().getJoinColumnAttributes());
            List<AbstractAttribute> attributes = modelValueArray.getModels();

            List<String> idColumns = attributes.stream().map(p -> p.getColumnName()).collect(Collectors.toList());
            FromTable fromTable = FromTable.of(relationshipJoinTable.getTableName());
            SqlDelete sqlDelete = sqlStatementFactory.generateDeleteById(fromTable, idColumns);
            String sql = dbConfiguration.getSqlStatementGenerator().export(sqlDelete);
            dbConfiguration.getJdbcRunner().delete(sql, connectionHolder.getConnection(), idParameters);
        }
    }

}
