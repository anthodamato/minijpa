package org.minijpa.jpa.db;

import org.minijpa.jdbc.*;
import org.minijpa.jdbc.db.SqlSelectData;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.model.AbstractAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.Pk;
import org.minijpa.jpa.model.RelationshipMetaAttribute;
import org.minijpa.jpa.model.relationship.JoinColumnAttribute;
import org.minijpa.jpa.model.relationship.Relationship;
import org.minijpa.jpa.model.relationship.RelationshipJoinTable;
import org.minijpa.metadata.AliasGenerator;
import org.minijpa.sql.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JdbcQueryRunner {

    private final Logger log = LoggerFactory.getLogger(JdbcQueryRunner.class);
    private final ConnectionHolder connectionHolder;
    private final DbConfiguration dbConfiguration;
    private final AliasGenerator aliasGenerator;
    private final JdbcRunner.JdbcValueBuilderById jdbcValueBuilderById = new JdbcRunner.JdbcValueBuilderById();
    private final JdbcFetchParameterRecordBuilder jdbcFetchParameterRecordBuilder = new JdbcFetchParameterRecordBuilder();

    public JdbcQueryRunner(
            ConnectionHolder connectionHolder,
            DbConfiguration dbConfiguration,
            AliasGenerator tableAliasGenerator) {
        super();
        this.connectionHolder = connectionHolder;
        this.dbConfiguration = dbConfiguration;
        this.aliasGenerator = tableAliasGenerator;
    }

    public Optional<ModelValueArray<FetchParameter>> findById(
            MetaEntity entity,
            Object primaryKey,
            LockType lockType)
            throws Exception {
        SqlSelectData sqlSelectData = dbConfiguration.getSqlStatementFactory()
                .generateSelectById(entity, lockType,
                        aliasGenerator);
        List<QueryParameter> parameters = entity.getId().queryParameters(primaryKey);

        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelectData);
        jdbcValueBuilderById.setFetchParameters(sqlSelectData.getFetchParameters());
        return dbConfiguration.getJdbcRunner()
                .findById(sql, connectionHolder.getConnection(), parameters,
                        jdbcValueBuilderById);
    }

//    public Optional<ModelValueArray<FetchParameter>> runVersionQuery(
//            MetaEntity entity,
//            Object primaryKey,
//            LockType lockType) throws Exception {
//        SqlSelectData sqlSelectData = dbConfiguration.getSqlStatementFactory()
//                .generateSelectVersion(entity, lockType,
//                        aliasGenerator);
//        List<QueryParameter> parameters = entity.getId().queryParameters(primaryKey);
//
//        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelectData);
//        jdbcValueBuilderById.setFetchParameters(sqlSelectData.getFetchParameters());
//        return dbConfiguration.getJdbcRunner()
//                .findById(sql, connectionHolder.getConnection(), parameters,
//                        jdbcValueBuilderById);
//    }


    /**
     * Executes a query like: 'select (Entity fields) from table where pk=foreignkey' <br> The
     * attribute 'foreignKeyAttribute' type can be one of 'java.util.Collection', 'java.util.List' or
     * 'java.util.Map', etc. <br> For example, in order to load the list of Employee for a given
     * Department (foreign key) we have to pass:
     * <p>
     * - the department instance, so we can get the foreign key - the Employee class
     *
     * @author adamato
     */
    public Object selectByForeignKey(
            MetaEntity entity,
            RelationshipMetaAttribute foreignKeyAttribute,
            Object foreignKey,
            Class<?> collectionClass,
            LockType lockType,
            EntityHandler entityLoader) throws Exception {
        List<QueryParameter> parameters = foreignKeyAttribute.queryParameters(foreignKey);
        List<String> columns = parameters.stream().map(p -> {
                    if (p.getColumn() instanceof String) return (String) p.getColumn();

                    return ((TableColumn) p.getColumn()).getColumn().getName();
                })
                .collect(Collectors.toList());
        SqlSelectData sqlSelectData = dbConfiguration.getSqlStatementFactory()
                .generateSelectByForeignKey(entity, columns, aliasGenerator);
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelectData);
        log.debug("Select By ForeignKey -> Foreign Key Attribute = {}", foreignKeyAttribute);
        Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(null,
                CollectionUtils.findCollectionImplementationClass(collectionClass));
        entityLoader.setLockType(lockType);

        jdbcFetchParameterRecordBuilder.setCollectionResult(collectionResult);
        jdbcFetchParameterRecordBuilder.setEntityLoader(entityLoader);
        jdbcFetchParameterRecordBuilder.setMetaEntity(entity);
        jdbcFetchParameterRecordBuilder.setFetchParameters(sqlSelectData.getFetchParameters());
        dbConfiguration.getJdbcRunner().runQuery(connectionHolder.getConnection(), sql, parameters,
                jdbcFetchParameterRecordBuilder);
        return collectionResult;
    }


    public Object selectByJoinTable(Object primaryKey, Pk id, Relationship relationship,
                                    RelationshipMetaAttribute metaAttribute,
                                    EntityHandler entityLoader) throws Exception {
        ModelValueArray<JoinColumnAttribute> modelValueArray = null;
        SqlSelectData sqlSelectData = null;
        if (relationship.isOwner()) {
            modelValueArray = dbConfiguration.getSqlStatementFactory()
                    .expandJoinColumnAttributes(id, primaryKey,
                            relationship.getJoinTable().getOwningJoinColumnMapping().getJoinColumnAttributes());
            List<JoinColumnAttribute> attributes = modelValueArray.getModels();
            sqlSelectData = dbConfiguration.getSqlStatementFactory().generateSelectByJoinTable(
                    relationship.getAttributeType(), relationship.getJoinTable(), attributes, aliasGenerator);
        } else {
            modelValueArray = dbConfiguration.getSqlStatementFactory()
                    .expandJoinColumnAttributes(id, primaryKey,
                            relationship.getJoinTable().getTargetJoinColumnMapping().getJoinColumnAttributes());
            List<JoinColumnAttribute> attributes = modelValueArray.getModels();
            sqlSelectData = dbConfiguration.getSqlStatementFactory().generateSelectByJoinTableFromTarget(
                    relationship.getAttributeType(), relationship.getJoinTable(), attributes, aliasGenerator);
        }

        List<QueryParameter> parameters = MetaEntityHelper.convertAbstractAVToQP(modelValueArray);
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelectData);
        Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(null,
                metaAttribute.getCollectionImplementationClass());
        entityLoader.setLockType(LockType.NONE);
        jdbcFetchParameterRecordBuilder.setCollectionResult(collectionResult);
        jdbcFetchParameterRecordBuilder.setEntityLoader(entityLoader);
        jdbcFetchParameterRecordBuilder.setMetaEntity(relationship.getAttributeType());
        jdbcFetchParameterRecordBuilder.setFetchParameters(sqlSelectData.getFetchParameters());
        dbConfiguration.getJdbcRunner().runQuery(connectionHolder.getConnection(), sql, parameters,
                jdbcFetchParameterRecordBuilder);
        return collectionResult;
    }

    public Object insertWithIdentityColumn(
            MetaEntity entity,
            Object entityInstance,
            List<QueryParameter> parameters,
            boolean isIdentityColumnNull) throws Exception {
        List<String> columns = parameters.stream().map(p -> {
                    if (p.getColumn() instanceof String) return (String) p.getColumn();

                    return ((TableColumn) p.getColumn()).getColumn().getName();
                })
                .collect(Collectors.toList());
        Pk pk = entity.getId();
        SqlInsert sqlInsert = dbConfiguration.getSqlStatementFactory()
                .generateInsert(entity, columns, true,
                        isIdentityColumnNull, entity, aliasGenerator);
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlInsert);
        return dbConfiguration.getJdbcRunner()
                .insertReturnGeneratedKeys(connectionHolder.getConnection(), sql,
                        parameters, pk.getAttribute().getColumnName());
    }

    public void insert(
            MetaEntity entity,
            Object entityInstance,
            List<QueryParameter> parameters)
            throws Exception {
        List<String> columns = parameters.stream().map(p -> {
                    if (p.getColumn() instanceof String)
                        return (String) p.getColumn();

                    return ((TableColumn) p.getColumn()).getColumn().getName();
                })
                .collect(Collectors.toList());
        SqlInsert sqlInsert = dbConfiguration.getSqlStatementFactory()
                .generateInsert(entity, columns, false, false,
                        null, aliasGenerator);
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlInsert);
        dbConfiguration.getJdbcRunner().insert(connectionHolder.getConnection(), sql, parameters);
    }

    public void deleteById(
            MetaEntity e,
            List<QueryParameter> idParameters) throws Exception {
        List<String> idColumns = idParameters.stream().map(p -> {
                    if (p.getColumn() instanceof String) return (String) p.getColumn();

                    return ((TableColumn) p.getColumn()).getColumn().getName();
                })
                .collect(Collectors.toList());

        SqlDelete sqlDelete = dbConfiguration.getSqlStatementFactory()
                .generateDeleteById(e, idColumns, aliasGenerator);
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlDelete);
        dbConfiguration.getJdbcRunner().delete(sql, connectionHolder.getConnection(), idParameters);
    }

    public int update(
            MetaEntity entity,
            List<QueryParameter> parameters,
            List<String> columns,
            List<String> idColumns)
            throws Exception {
        SqlUpdate sqlUpdate = dbConfiguration.getSqlStatementFactory()
                .generateUpdate(entity, columns, idColumns,
                        aliasGenerator);
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlUpdate);
        return dbConfiguration.getJdbcRunner()
                .update(connectionHolder.getConnection(), sql, parameters);
    }

    public void insertJoinTableAttribute(RelationshipJoinTable relationshipJoinTable,
                                         Object entityInstance,
                                         Object instance) throws Exception {
        List<QueryParameter> parameters = dbConfiguration.getSqlStatementFactory()
                .createRelationshipJoinTableParameters(relationshipJoinTable, entityInstance, instance);
        List<String> columnNames = parameters.stream().map(p -> {
                    if (p.getColumn() instanceof String) return (String) p.getColumn();

                    return ((TableColumn) p.getColumn()).getColumn().getName();
                })
                .collect(Collectors.toList());

        SqlInsert sqlInsert = dbConfiguration.getSqlStatementFactory()
                .generateJoinTableInsert(relationshipJoinTable,
                        columnNames);
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlInsert);
        dbConfiguration.getJdbcRunner().insert(connectionHolder.getConnection(), sql, parameters);
    }

    public void removeJoinTableRecords(MetaEntity entity, Object primaryKey,
                                       List<QueryParameter> idParameters,
                                       RelationshipJoinTable relationshipJoinTable) throws Exception {
        if (relationshipJoinTable.getOwningEntity() == entity) {
            ModelValueArray<JoinColumnAttribute> modelValueArray = dbConfiguration.getSqlStatementFactory()
                    .expandJoinColumnAttributes(entity.getId(), primaryKey,
                            relationshipJoinTable.getOwningJoinColumnMapping().getJoinColumnAttributes());
            List<JoinColumnAttribute> attributes = modelValueArray.getModels();

            List<String> idColumns = attributes.stream().map(AbstractAttribute::getColumnName)
                    .collect(Collectors.toList());
            FromTable fromTable = FromTable.of(relationshipJoinTable.getTableName());
            SqlDelete sqlDelete = dbConfiguration.getSqlStatementFactory()
                    .generateDeleteById(fromTable, idColumns);
            String sql = dbConfiguration.getSqlStatementGenerator().export(sqlDelete);
            dbConfiguration.getJdbcRunner().delete(sql, connectionHolder.getConnection(), idParameters);
        }
//    else {
//      ModelValueArray<AbstractAttribute> modelValueArray = dbConfiguration.getSqlStatementFactory()
//          .expandJoinColumnAttributes(entity.getId(), primaryKey,
//              relationshipJoinTable.getTargetJoinColumnMapping().getJoinColumnAttributes());
//      List<AbstractAttribute> attributes = modelValueArray.getModels();
//
//      List<String> idColumns = attributes.stream().map(p -> p.getColumnName())
//          .collect(Collectors.toList());
//      FromTable fromTable = FromTable.of(relationshipJoinTable.getTableName());
//      SqlDelete sqlDelete = dbConfiguration.getSqlStatementFactory()
//          .generateDeleteById(fromTable, idColumns);
//      String sql = dbConfiguration.getSqlStatementGenerator().export(sqlDelete);
//            dbConfiguration.getJdbcRunner().delete(sql, connectionHolder.getConnection(), idParameters);
//    }
    }

}
