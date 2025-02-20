/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jpa.db;

import org.minijpa.jdbc.*;
import org.minijpa.jdbc.db.SqlSelectData;
import org.minijpa.jdbc.db.SqlSelectDataBuilder;
import org.minijpa.jdbc.mapper.AbstractDbTypeMapper;
import org.minijpa.jdbc.mapper.ObjectConverter;
import org.minijpa.jpa.AbstractQuery;
import org.minijpa.jpa.*;
import org.minijpa.jpa.criteria.*;
import org.minijpa.jpa.criteria.expression.*;
import org.minijpa.jpa.criteria.join.CollectionFetchJoinImpl;
import org.minijpa.jpa.criteria.join.CollectionJoinImpl;
import org.minijpa.jpa.criteria.join.FetchJoinType;
import org.minijpa.jpa.criteria.predicate.*;
import org.minijpa.jpa.model.*;
import org.minijpa.jpa.model.relationship.JoinColumnAttribute;
import org.minijpa.jpa.model.relationship.Relationship;
import org.minijpa.jpa.model.relationship.RelationshipJoinTable;
import org.minijpa.metadata.AliasGenerator;
import org.minijpa.sql.model.From;
import org.minijpa.sql.model.*;
import org.minijpa.sql.model.condition.*;
import org.minijpa.sql.model.join.FromJoin;
import org.minijpa.sql.model.join.FromJoinImpl;
import org.minijpa.sql.model.join.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

public class SqlStatementFactory extends JdbcSqlStatementFactory {

    private final Logger log = LoggerFactory.getLogger(SqlStatementFactory.class);
    protected CriteriaExpressionHelper criteriaExpressionHelper;

    public SqlStatementFactory() {
    }

    public void init() {
        this.criteriaExpressionHelper = new CriteriaExpressionHelper();
    }

    public SqlInsert generateInsert(
            MetaEntity entity,
            List<String> columns,
            boolean hasIdentityColumn,
            boolean identityColumnNull,
            MetaEntity metaEntity,
            AliasGenerator tableAliasGenerator) {
        List<Column> cs = columns.stream().map(Column::new).collect(Collectors.toList());

        return new SqlInsert(
                FromTable.of(entity.getTableName(), tableAliasGenerator.getDefault(entity.getTableName())),
                cs,
                hasIdentityColumn,
                identityColumnNull,
                metaEntity != null ? metaEntity.getId().getAttribute().getColumnName() : null);
    }

    private ForUpdate calcForUpdate(LockType lockType) {
        if (lockType == null) {
            return null;
        }

        if (lockType == LockType.PESSIMISTIC_WRITE) {
            return new ForUpdate();
        }

        return null;
    }

    public SqlSelectData generateSelectById(
            MetaEntity entity,
            LockType lockType,
            AliasGenerator tableAliasGenerator) throws Exception {
        List<FetchParameter> fetchParameters = MetaEntityHelper.convertAllAttributes(entity);
        FromTable fromTable = FromTable.of(entity.getTableName(),
                tableAliasGenerator.getDefault(entity.getTableName()));
        List<TableColumn> tableColumns = MetaEntityHelper.toValues(entity.getId().getAttributes(),
                fromTable);
        List<Condition> conditions = tableColumns.stream().map(t -> {
            return new BinaryCondition.Builder(ConditionType.EQUAL).withLeft(t)
                    .withRight(CriteriaUtils.QM).build();
        }).collect(Collectors.toList());

        Condition condition = Condition.toAnd(conditions);
        SqlSelectDataBuilder sqlSelectBuilder = new SqlSelectDataBuilder();
        sqlSelectBuilder.withFromTable(fromTable);
        if (lockType != null) {
            sqlSelectBuilder.withForUpdate(calcForUpdate(lockType));
        }

        sqlSelectBuilder.withValues(MetaEntityHelper.toValues(entity, fromTable))
                .withConditions(List.of(condition));
        sqlSelectBuilder.withFetchParameters(fetchParameters);
        return (SqlSelectData) sqlSelectBuilder.build();
    }

//    public SqlSelectData generateSelectVersion(
//            MetaEntity entity,
//            LockType lockType,
//            AliasGenerator tableAliasGenerator) throws Exception {
//        FetchParameter fetchParameter = MetaEntityHelper.toFetchParameter(
//                entity.getVersionMetaAttribute());
//
//        FromTable fromTable = FromTable.of(entity.getTableName(),
//                tableAliasGenerator.getDefault(entity.getTableName()));
//        List<TableColumn> tableColumns = MetaEntityHelper.toValues(entity.getId().getAttributes(),
//                fromTable);
//        List<Condition> conditions = tableColumns.stream().map(t -> {
//            return new BinaryCondition.Builder(ConditionType.EQUAL).withLeft(t)
//                    .withRight(CriteriaUtils.QM).build();
//        }).collect(Collectors.toList());
//
//        Condition condition = Condition.toAnd(conditions);
//        SqlSelectDataBuilder sqlSelectBuilder = new SqlSelectDataBuilder();
//        sqlSelectBuilder.withFromTable(fromTable);
//        if (lockType != null) {
//            sqlSelectBuilder.withForUpdate(calcForUpdate(lockType));
//        }
//
//        sqlSelectBuilder.withValues(
//                        List.of(MetaEntityHelper.toValue(entity.getVersionMetaAttribute(), fromTable)))
//                .withConditions(List.of(condition));
//        sqlSelectBuilder.withFetchParameters(List.of(fetchParameter));
//        return (SqlSelectData) sqlSelectBuilder.build();
//    }

    public SqlSelectData generateSelectByForeignKey(
            MetaEntity entity,
            List<String> columns,
            AliasGenerator tableAliasGenerator) {
        List<FetchParameter> fetchColumnNameValues = MetaEntityHelper.convertAllAttributes(entity);
        // LOG.info("generateSelectByForeignKey: fetchColumnNameValues=" +
        // fetchColumnNameValues);
        // LOG.info("generateSelectByForeignKey: parameters=" + parameters);
        FromTable fromTable = FromTable.of(entity.getTableName(),
                tableAliasGenerator.getDefault(entity.getTableName()));
        List<TableColumn> tableColumns = columns.stream()
                .map(c -> new TableColumn(fromTable, new Column(c))).collect(Collectors.toList());
        List<Condition> conditions = tableColumns.stream()
                .map(t -> new BinaryCondition.Builder(ConditionType.EQUAL).withLeft(t)
                        .withRight(CriteriaUtils.QM).build()).collect(Collectors.toList());

        Condition condition = Condition.toAnd(conditions);
        SqlSelectDataBuilder sqlSelectBuilder = new SqlSelectDataBuilder();
        sqlSelectBuilder.withFromTable(fromTable);
        sqlSelectBuilder.withValues(MetaEntityHelper.toValues(entity, fromTable))
                .withConditions(Arrays.asList(condition)).withResult(fromTable);
        sqlSelectBuilder.withFetchParameters(fetchColumnNameValues);
        return (SqlSelectData) sqlSelectBuilder.build();
    }

    public ModelValueArray<JoinColumnAttribute> expandJoinColumnAttributes(
            Pk owningId,
            Object joinTableForeignKey,
            List<JoinColumnAttribute> allJoinColumnAttributes)
            throws Exception {
        ModelValueArray<AbstractMetaAttribute> modelValueArray = new ModelValueArray<>();
        log.debug("expandJoinColumnAttributes: owningId={}", owningId);
        owningId.expand(joinTableForeignKey, modelValueArray);

        log.debug("expandJoinColumnAttributes: modelValueArray.size()={}", modelValueArray.size());
        allJoinColumnAttributes.forEach(
                a -> log.debug("expandJoinColumnAttributes: a.getForeignKeyAttribute()={}",
                        a.getForeignKeyAttribute()));
        ModelValueArray<JoinColumnAttribute> result = new ModelValueArray<>();
        for (int i = 0; i < modelValueArray.size(); ++i) {
            MetaAttribute attribute = (MetaAttribute) modelValueArray.getModel(i);
            log.debug("expandJoinColumnAttributes: attribute={}", attribute);
            Optional<JoinColumnAttribute> optional = allJoinColumnAttributes.stream()
                    .filter(j -> j.getForeignKeyAttribute() == attribute).findFirst();
            log.debug("expandJoinColumnAttributes: optional.isPresent()={}", optional.isPresent());
            result.add(optional.get(), modelValueArray.getValue(i));
        }

        return result;
    }

    /**
     * Calculates the joins from <i>entity</i> to
     * <i>metaAttribute</i>.<i>metaAttribute</i> is a relationship attribute.
     *
     * @param entity
     * @param metaAttribute
     * @param aliasGenerator
     * @return
     */
    public List<FromJoin> calculateJoins(
            MetaEntity entity,
            RelationshipMetaAttribute metaAttribute,
            JoinType joinType,
            AliasGenerator aliasGenerator) {
        if (metaAttribute.getRelationship().getJoinTable() != null) {
            List<MetaAttribute> idSourceAttributes = entity.getId().getAttributes();
            List<Column> idSourceColumns = idSourceAttributes.stream().map(a -> {
                return new Column(a.getColumnName());
            }).collect(Collectors.toList());

            RelationshipJoinTable relationshipJoinTable = metaAttribute.getRelationship().getJoinTable();
            List<Column> idOwningColumns = relationshipJoinTable.getOwningJoinColumnMapping()
                    .getJoinColumnAttributes().stream().map(a -> {
                        return new Column(a.getColumnName());
                    }).collect(Collectors.toList());

            String tableAlias = aliasGenerator.getDefault(relationshipJoinTable.getTableName());
            FromTable joinTable = new FromTableImpl(relationshipJoinTable.getTableName(), tableAlias);
            tableAlias = aliasGenerator.getDefault(entity.getTableName());
            FromJoin fromJoin = new FromJoinImpl(joinTable, tableAlias, idSourceColumns, idOwningColumns,
                    joinType);

            MetaEntity destEntity = relationshipJoinTable.getTargetEntity();
            List<MetaAttribute> idTargetAttributes = destEntity.getId().getAttributes();
            List<Column> idDestColumns = idTargetAttributes.stream().map(a -> {
                return new Column(a.getColumnName());
            }).collect(Collectors.toList());

            List<Column> idTargetColumns = relationshipJoinTable.getTargetJoinColumnMapping()
                    .getJoinColumnAttributes().stream().map(a -> {
                        return new Column(a.getColumnName());
                    }).collect(Collectors.toList());
            tableAlias = aliasGenerator.getDefault(destEntity.getTableName());
            FromTable joinTable2 = new FromTableImpl(destEntity.getTableName(), tableAlias);
            FromJoin fromJoin2 = new FromJoinImpl(joinTable2,
                    aliasGenerator.getDefault(relationshipJoinTable.getTableName()), idTargetColumns,
                    idDestColumns, joinType);

            log.debug("calculateJoins: fromJoin={}, fromJoin2={}", fromJoin, fromJoin2);
            return Arrays.asList(fromJoin, fromJoin2);
        } else if (metaAttribute.getRelationship().getJoinColumnMapping() != null) {
            List<JoinColumnAttribute> joinColumnAttributes = metaAttribute.getRelationship()
                    .getJoinColumnMapping().getJoinColumnAttributes();
            List<Column> idSourceColumns = joinColumnAttributes.stream().map(a -> {
                return new Column(a.getColumnName());
            }).collect(Collectors.toList());

            MetaEntity destEntity = metaAttribute.getRelationship().getAttributeType();
            List<MetaAttribute> idTargetAttributes = destEntity.getId().getAttributes();
            List<Column> idDestColumns = idTargetAttributes.stream().map(a -> {
                return new Column(a.getColumnName());
            }).collect(Collectors.toList());

            String tableAlias = aliasGenerator.getDefault(destEntity.getTableName());
            FromTable joinTable = new FromTableImpl(destEntity.getTableName(), tableAlias);
            tableAlias = aliasGenerator.getDefault(entity.getTableName());
            FromJoin fromJoin = new FromJoinImpl(joinTable, tableAlias, idSourceColumns, idDestColumns,
                    joinType);

            return List.of(fromJoin);
        }

        return List.of();
    }

    public FromJoin calculateFromTableByJoinTable(
            MetaEntity entity,
            RelationshipJoinTable relationshipJoinTable,
            AliasGenerator tableAliasGenerator) {
        List<MetaAttribute> idAttributes = entity.getId().getAttributes();
        List<Column> idColumns = idAttributes.stream().map(a -> {
            return new Column(a.getColumnName());
        }).collect(Collectors.toList());

        List<Column> idTargetColumns = relationshipJoinTable.getTargetJoinColumnMapping()
                .getJoinColumnAttributes().stream().map(a -> {
                    return new Column(a.getColumnName());
                }).collect(Collectors.toList());

        FromTable joinTable = new FromTableImpl(relationshipJoinTable.getTableName(),
                tableAliasGenerator.getDefault(relationshipJoinTable.getTableName()));
        String tableAlias = tableAliasGenerator.getDefault(entity.getTableName());
        return new FromJoinImpl(joinTable, tableAlias, idColumns, idTargetColumns);
    }

    public Condition generateJoinCondition(
            Relationship relationship,
            MetaEntity owningEntity,
            MetaEntity targetEntity,
            AliasGenerator tableAliasGenerator) {
        if (relationship.getJoinTable() != null) {
            RelationshipJoinTable relationshipJoinTable = relationship.getJoinTable();
            Pk pk = relationshipJoinTable.getOwningAttribute();
            pk.getAttributes().forEach(
                    a -> log.debug("generateJoinCondition: relationshipJoinTable.getOwningAttribute()={}",
                            a.getColumnName()));
            relationshipJoinTable.getTargetAttribute().getAttributes().forEach(
                    a -> log.debug("generateJoinCondition: relationshipJoinTable.getTargetAttribute()={}",
                            a.getColumnName()));
            relationshipJoinTable.getOwningJoinColumnMapping().getJoinColumnAttributes().forEach(
                    a -> log.debug("generateJoinCondition: getOwningJoinColumnMapping a.getColumnName()={}",
                            a.getColumnName()));
            relationshipJoinTable.getOwningJoinColumnMapping().getJoinColumnAttributes().forEach(
                    a -> log.debug(
                            "generateJoinCondition: getOwningJoinColumnMapping a.getAttribute().getColumnName()={}",
                            a.getAttribute().getColumnName()));
            relationshipJoinTable.getTargetJoinColumnMapping().getJoinColumnAttributes().forEach(
                    a -> log.debug("generateJoinCondition: getTargetJoinColumnMapping a.getColumnName()={}",
                            a.getColumnName()));
            relationshipJoinTable.getTargetJoinColumnMapping().getJoinColumnAttributes().forEach(
                    a -> log.debug(
                            "generateJoinCondition: getTargetJoinColumnMapping a.getAttribute().getColumnName()={}",
                            a.getAttribute().getColumnName()));
            // op.orders_id = o.id and p.id = op.products_id
            List<JoinColumnAttribute> owningJoinColumnAttributes = relationshipJoinTable.getOwningJoinColumnMapping()
                    .getJoinColumnAttributes();
            List<Condition> conditions = new ArrayList<>();
            FromTable joinTable = new FromTableImpl(relationshipJoinTable.getTableName(),
                    tableAliasGenerator.getDefault(relationshipJoinTable.getTableName()));
            FromTable fromTable = FromTable.of(owningEntity.getTableName(),
                    tableAliasGenerator.getDefault(owningEntity.getTableName()));
            // op.orders_id = o.id
            // handle composite primary key
            for (int i = 0; i < owningJoinColumnAttributes.size(); ++i) {
                JoinColumnAttribute joinColumnAttribute = owningJoinColumnAttributes.get(i);
                TableColumn owningTableColumn = new TableColumn(joinTable,
                        new Column(joinColumnAttribute.getColumnName()));
                AbstractMetaAttribute ak = owningEntity.getId().getAttributes().get(i);
                TableColumn owningEntityTableColumn = new TableColumn(fromTable,
                        new Column(ak.getColumnName()));
                Condition condition = new BinaryCondition.Builder(ConditionType.EQUAL).withLeft(
                        owningTableColumn).withRight(owningEntityTableColumn).build();
                conditions.add(condition);
            }

            // p.id = op.products_id
            FromTable targetTable = FromTable.of(targetEntity.getTableName(),
                    tableAliasGenerator.getDefault(targetEntity.getTableName()));
            List<JoinColumnAttribute> targetJoinColumnAttributes = relationshipJoinTable.getTargetJoinColumnMapping()
                    .getJoinColumnAttributes();
            for (int i = 0; i < targetJoinColumnAttributes.size(); ++i) {
                JoinColumnAttribute joinColumnAttribute = targetJoinColumnAttributes.get(i);
                TableColumn owningTableColumn = new TableColumn(joinTable,
                        new Column(joinColumnAttribute.getColumnName()));
                AbstractMetaAttribute ak = targetEntity.getId().getAttributes().get(i);
                TableColumn targetEntityTableColumn = new TableColumn(targetTable,
                        new Column(ak.getColumnName()));
                Condition condition = new BinaryCondition.Builder(ConditionType.EQUAL).withLeft(
                        owningTableColumn).withRight(targetEntityTableColumn).build();
                conditions.add(condition);
            }

            return Condition.toAnd(conditions);
        }

        return null;
    }

    /**
     * @param entity
     * @param relationshipJoinTable
     * @param attributes
     * @param aliasGenerator
     * @return
     */
    public SqlSelectData generateSelectByJoinTable(
            MetaEntity entity,
            RelationshipJoinTable relationshipJoinTable,
            List<? extends AbstractAttribute> attributes,
            AliasGenerator aliasGenerator) {
        // select t1.id, t1.p1 from entity t1 inner join jointable j on t1.id=j.id1
        // where j.t2=fk
        FromTable joinTable = new FromTableImpl(relationshipJoinTable.getTableName(),
                aliasGenerator.getDefault(relationshipJoinTable.getTableName()));
        FromJoin fromJoin = calculateFromTableByJoinTable(entity, relationshipJoinTable,
                aliasGenerator);
        FromTable fromTable = FromTable.of(entity.getTableName(),
                aliasGenerator.getDefault(entity.getTableName()));
        // handles multiple column pk

        List<TableColumn> tableColumns = MetaEntityHelper.attributesToTableColumns(attributes,
                joinTable);
        List<Condition> conditions = tableColumns.stream()
                .map(t -> new BinaryCondition.Builder(ConditionType.EQUAL).withLeft(t)
                        .withRight(CriteriaUtils.QM).build())
                .collect(Collectors.toList());

        Condition condition = Condition.toAnd(conditions);
        List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
        List<FetchParameter> fetchColumnNameValues = expandedAttributes.stream()
                .map(a -> (FetchParameter) a).collect(Collectors.toList());
//        List<FetchParameter> fetchColumnNameValues = MetaEntityHelper.convertAttributes(
//                expandedAttributes);
        SqlSelectDataBuilder sqlSelectBuilder = new SqlSelectDataBuilder();
        sqlSelectBuilder.withFromTable(fromTable);
        sqlSelectBuilder.withFromTable(fromJoin)
                .withValues(MetaEntityHelper.toValues(entity, fromTable))
                .withConditions(Arrays.asList(condition)).withResult(fromTable);
        sqlSelectBuilder.withFetchParameters(fetchColumnNameValues);
        return (SqlSelectData) sqlSelectBuilder.build();
    }

    public SqlSelectData generateSelectByJoinTableFromTarget(
            MetaEntity entity,
            RelationshipJoinTable relationshipJoinTable,
            List<? extends AbstractAttribute> attributes,
            AliasGenerator tableAliasGenerator) throws Exception {
        // select t1.id, t1.p1 from entity t1 inner join jointable j on t1.id=j.id1
        // where j.t2=fk
        List<MetaAttribute> idAttributes = entity.getId().getAttributes();
        List<Column> idColumns = idAttributes.stream().map(a -> {
            return new Column(a.getColumnName());
        }).collect(Collectors.toList());

        List<Column> idTargetColumns = relationshipJoinTable.getOwningJoinColumnMapping()
                .getJoinColumnAttributes().stream().map(a -> {
                    return new Column(a.getColumnName());
                }).collect(Collectors.toList());

        FromTable joinTable = new FromTableImpl(relationshipJoinTable.getTableName(),
                tableAliasGenerator.getDefault(relationshipJoinTable.getTableName()));
        String tableAlias = tableAliasGenerator.getDefault(entity.getTableName());
        FromJoin fromJoin = new FromJoinImpl(joinTable, tableAlias, idColumns, idTargetColumns);
        FromTable fromTable = FromTable.of(entity.getTableName(),
                tableAliasGenerator.getDefault(entity.getTableName()));
        // handles multiple column pk

        List<TableColumn> tableColumns = MetaEntityHelper.attributesToTableColumns(attributes,
                joinTable);

        List<Condition> conditions = tableColumns.stream().map(t -> {
            return new BinaryCondition.Builder(ConditionType.EQUAL).withLeft(t)
                    .withRight(CriteriaUtils.QM).build();
        }).collect(Collectors.toList());

        Condition condition = Condition.toAnd(conditions);
        List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
        List<FetchParameter> fetchColumnNameValues = expandedAttributes.stream().map(a -> (FetchParameter) a).collect(Collectors.toList());
//        List<FetchParameter> fetchColumnNameValues = MetaEntityHelper.convertAttributes(
//                expandedAttributes);
        SqlSelectDataBuilder sqlSelectBuilder = new SqlSelectDataBuilder();
        sqlSelectBuilder.withFromTable(fromTable);
        sqlSelectBuilder.withFromTable(fromJoin)
                .withValues(MetaEntityHelper.toValues(entity, fromTable))
                .withConditions(Arrays.asList(condition)).withResult(fromTable).build();
        sqlSelectBuilder.withFetchParameters(fetchColumnNameValues);
        return (SqlSelectData) sqlSelectBuilder.build();
    }

    public List<QueryParameter> createRelationshipJoinTableParameters(
            RelationshipJoinTable relationshipJoinTable,
            Object owningInstance,
            Object targetInstance)
            throws Exception {
        List<QueryParameter> parameters = new ArrayList<>();
        Pk owningId = relationshipJoinTable.getOwningAttribute();
        relationshipJoinTable.getOwningJoinColumnMapping().getJoinColumnAttributes()
                .forEach(a -> log.debug("createRelationshipJoinTableParameters: a={}", a));
        parameters.addAll(MetaEntityHelper.createJoinColumnAVSToQP(
                relationshipJoinTable.getOwningJoinColumnMapping().getJoinColumnAttributes(), owningId,
                owningId.readValue(owningInstance)));

        Pk targetId = relationshipJoinTable.getTargetAttribute();
        parameters.addAll(MetaEntityHelper.createJoinColumnAVSToQP(
                relationshipJoinTable.getTargetJoinColumnMapping().getJoinColumnAttributes(), targetId,
                targetId.readValue(targetInstance)));
        return parameters;
    }

    public SqlInsert generateJoinTableInsert(
            RelationshipJoinTable relationshipJoinTable,
            List<String> columnNames) throws Exception {
        List<Column> columns = columnNames.stream().map(Column::new)
                .collect(Collectors.toList());
        return new SqlInsert(new FromTableImpl(relationshipJoinTable.getTableName()), columns, false,
                false, null);
    }

    public SqlUpdate generateUpdate(
            MetaEntity entity,
            List<String> columns,
            List<String> idColumnNames,
            AliasGenerator tableAliasGenerator) throws Exception {
        FromTable fromTable = FromTable.of(entity.getTableName(),
                tableAliasGenerator.getDefault(entity.getTableName()));
        List<TableColumn> cs = columns.stream().map(c -> new TableColumn(fromTable, new Column(c))).collect(Collectors.toList());

        Condition condition = createAttributeEqualCondition(fromTable, idColumnNames);
        return new SqlUpdate(fromTable, cs, condition);
    }

    public SqlDelete generateDeleteById(
            MetaEntity entity,
            List<String> idColumnNames,
            AliasGenerator tableAliasGenerator) throws Exception {
        FromTable fromTable = FromTable.of(entity.getTableName(),
                tableAliasGenerator.getDefault(entity.getTableName()));
        Condition condition = createAttributeEqualCondition(fromTable, idColumnNames);
        return new SqlDelete(fromTable, condition);
    }

    public SqlDelete generateDeleteById(
            FromTable fromTable, List<String> idColumnNames)
            throws Exception {
        Condition condition = createAttributeEqualCondition(fromTable, idColumnNames);
        return new SqlDelete(fromTable, condition);
    }

    private Condition createAttributeEqualCondition(
            FromTable fromTable, List<String> columns)
            throws Exception {
        if (columns.size() == 1) {
            return new BinaryCondition.Builder(ConditionType.EQUAL).withLeft(
                            new TableColumn(fromTable, new Column(columns.get(0)))).withRight(CriteriaUtils.QM)
                    .build();
        }

        List<Condition> conditions = new ArrayList<>();
        for (String columnName : columns) {
            BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL).withLeft(
                    new TableColumn(fromTable, new Column(columnName))).withRight(CriteriaUtils.QM).build();
            conditions.add(binaryCondition);
        }

        return new BinaryLogicConditionImpl(ConditionType.AND, conditions);
    }

    private Optional<FetchParameter> createFetchParameter(
            Selection<?> selection,
            Class<?> resultClass) {
        if (selection == null) {
            return Optional.empty();
        }

        log.debug("createFetchParameter: selection={}", selection);
        log.debug("createFetchParameter: resultClass={}", resultClass);
        if (selection instanceof AttributePath<?>) {
            AttributePath<?> miniPath = (AttributePath<?>) selection;
            AbstractMetaAttribute metaAttribute = miniPath.getMetaAttribute();
            FetchParameter columnNameValue = AttributeFetchParameter.build(metaAttribute);
            return Optional.of(columnNameValue);
        } else if (selection instanceof AggregateFunctionExpression<?>) {
            AggregateFunctionExpression<?> aggregateFunctionExpression = (AggregateFunctionExpression<?>) selection;
            Integer sqlType = resultClass != null ? JdbcTypes.sqlTypeFromClass(resultClass) : null;
            if (aggregateFunctionExpression.getAggregateFunctionType()
                    == org.minijpa.jpa.criteria.expression.AggregateFunctionType.COUNT) {
                FetchParameter cnv = new BasicFetchParameter("count", sqlType);
                return Optional.of(cnv);
            } else if (aggregateFunctionExpression.getAggregateFunctionType()
                    == org.minijpa.jpa.criteria.expression.AggregateFunctionType.SUM) {
                FetchParameter cnv = new BasicFetchParameter("sum", sqlType);
                return Optional.of(cnv);
            } else if (aggregateFunctionExpression.getAggregateFunctionType()
                    == org.minijpa.jpa.criteria.expression.AggregateFunctionType.AVG) {
                FetchParameter cnv = new BasicFetchParameter("avg", sqlType);
                return Optional.of(cnv);
            } else {
                Expression<?> expr = aggregateFunctionExpression.getX();
                if (expr instanceof AttributePath<?>) {
                    AttributePath<?> miniPath = (AttributePath<?>) expr;
                    AbstractMetaAttribute metaAttribute = miniPath.getMetaAttribute();
                    FetchParameter columnNameValue = AttributeFetchParameter.build(metaAttribute);
                    return Optional.of(columnNameValue);
                }
            }
        } else if (selection instanceof BinaryExpression) {
            Integer sqlType = resultClass != null ? JdbcTypes.sqlTypeFromClass(resultClass) : null;
            BinaryExpression binaryExpression = (BinaryExpression) selection;
            if (binaryExpression.getX() != null) {
                if (checkDataType((AttributePath<?>) binaryExpression.getX(), sqlType)) {
                    return Optional.of(new BasicFetchParameter("result", sqlType));
                }
            }

            if (binaryExpression.getY() != null) {
                if (checkDataType((AttributePath<?>) binaryExpression.getY(), sqlType)) {
                    return Optional.of(new BasicFetchParameter("result", sqlType));
                }
            }

            if (binaryExpression.getxValue() != null) {
                if (sqlType != null && sqlType != Types.OTHER
                        && binaryExpression.getxValue().getClass() == resultClass) {
                    return Optional.of(new BasicFetchParameter("result", sqlType));
                }
            }

            if (binaryExpression.getyValue() != null) {
                if (sqlType != null && sqlType != Types.NULL
                        && binaryExpression.getyValue().getClass() == resultClass) {
                    return Optional.of(new BasicFetchParameter("result", sqlType));
                }
            }

            sqlType = calculateSqlType(binaryExpression);
            ObjectConverter objectConverter = null;
            if (sqlType == Types.FLOAT) {
                // Postgres throws an exception: Persistence conversion to class java.lang.Float
                // from numeric not supported
                objectConverter = AbstractDbTypeMapper.NUMBER_TO_FLOAT_OBJECT_CONVERTER;
                sqlType = Types.OTHER;
            }

            log.debug("createFetchParameter: sqlType={}", sqlType);
            return Optional.of(new BasicFetchParameter("result", sqlType, objectConverter));
        } else if (selection instanceof UnaryExpression<?>) {
            UnaryExpression<?> unaryExpression = (UnaryExpression<?>) selection;
            if (unaryExpression.getExpressionOperator() == ExpressionOperator.SQRT) {
                return Optional.of(new BasicFetchParameter("result", Types.DOUBLE));
            } else if (unaryExpression.getExpressionOperator() == ExpressionOperator.LOWER) {
                return Optional.of(new BasicFetchParameter("result", Types.VARCHAR));
            } else if (unaryExpression.getExpressionOperator() == ExpressionOperator.UPPER) {
                return Optional.of(new BasicFetchParameter("result", Types.VARCHAR));
            } else if (unaryExpression.getExpressionOperator() == ExpressionOperator.ABS) {
                return Optional.of(new BasicFetchParameter("result", null));
            } else if (unaryExpression.getExpressionOperator() == ExpressionOperator.MOD) {
                return Optional.of(new BasicFetchParameter("result", Types.INTEGER));
            }
        } else if (selection instanceof TypecastExpression<?>) {
            ExpressionOperator expressionOperator = ((TypecastExpression<?>) selection).getExpressionOperator();
            if (expressionOperator == ExpressionOperator.TO_BIGDECIMAL) {
                return Optional.of(new BasicFetchParameter("result", Types.DECIMAL));
            } else if (expressionOperator == ExpressionOperator.TO_BIGINTEGER) {
                return Optional.of(new BasicFetchParameter("result", Types.OTHER,
                        AbstractDbTypeMapper.NUMBER_TO_BIG_INTEGER_OBJECT_CONVERTER));
            } else if (expressionOperator == ExpressionOperator.TO_DOUBLE) {
                return Optional.of(new BasicFetchParameter("result", Types.OTHER,
                        AbstractDbTypeMapper.NUMBER_TO_DOUBLE_OBJECT_CONVERTER));
            } else if (expressionOperator == ExpressionOperator.TO_FLOAT) {
                return Optional.of(new BasicFetchParameter("result", Types.OTHER,
                        AbstractDbTypeMapper.NUMBER_TO_FLOAT_OBJECT_CONVERTER));
            } else if (expressionOperator == ExpressionOperator.TO_INTEGER) {
                return Optional.of(new BasicFetchParameter("result", Types.INTEGER));
            } else if (expressionOperator == ExpressionOperator.TO_LONG) {
                return Optional.of(new BasicFetchParameter("result", Types.OTHER,
                        AbstractDbTypeMapper.NUMBER_TO_LONG_OBJECT_CONVERTER));
            }
        } else if (selection instanceof ConcatExpression) {
            return Optional.of(new BasicFetchParameter("concat", Types.VARCHAR));
        } else if (selection instanceof TrimExpression) {
            return Optional.of(new BasicFetchParameter("trim", Types.VARCHAR));
        } else if (selection instanceof LocateExpression) {
            return Optional.of(new BasicFetchParameter("locate", Types.INTEGER));
        } else if (selection instanceof SubstringExpression) {
            return Optional.of(new BasicFetchParameter("substring", Types.VARCHAR));
        } else if (selection instanceof NullifExpression) {
            return Optional.of(new BasicFetchParameter("nullif", Types.OTHER));
        } else if (selection instanceof CoalesceExpression) {
            return Optional.of(new BasicFetchParameter("coalesce", Types.OTHER));
        } else if (selection instanceof NegationExpression) {
            return Optional.of(new BasicFetchParameter("negation", Types.OTHER));
        } else if (selection instanceof CurrentDateExpression) {
            return Optional.of(new BasicFetchParameter("date", Types.DATE));
        } else if (selection instanceof CurrentTimeExpression) {
            return Optional.of(new BasicFetchParameter("time", Types.TIME));
        } else if (selection instanceof CurrentTimestampExpression) {
            return Optional.of(new BasicFetchParameter("timestamp", Types.TIMESTAMP));
        }

        return Optional.empty();
    }

    private Integer calculateSqlType(BinaryExpression binaryExpression) {
        log.debug("calculateSqlType: binaryExpression.getX()={}",
                binaryExpression.getX());
        if (binaryExpression.getX() != null) {
            log.debug("calculateSqlType: binaryExpression.getX()={}",
                    binaryExpression.getX());
            return calculateSqlTypeFromExpression(
                    (Expression<?>) binaryExpression.getX(),
                    (Expression<?>) binaryExpression.getY(),
                    binaryExpression.getyValue());
        }

        Integer sqlType1 = JdbcTypes.sqlTypeFromClass(binaryExpression.getxValue().getClass());
        log.debug("calculateSqlType: binaryExpression.getxValue().getClass()={}",
                binaryExpression.getxValue().getClass());
        if (binaryExpression.getY() != null) {
            AttributePath<?> attributePath2 = (AttributePath<?>) binaryExpression.getY();
            AbstractMetaAttribute metaAttribute2 = attributePath2.getMetaAttribute();
            Integer sqlType2 = metaAttribute2.getSqlType();
            int compare = JdbcTypes.compareNumericTypes(sqlType1, sqlType2);
            log.debug("calculateSqlType: sqlType1={}, sqlType2={}, compare={}", sqlType1, sqlType2,
                    compare);
            return compare < 0 ? sqlType2 : sqlType1;
        }

        Integer sqlType2 = JdbcTypes.sqlTypeFromClass(binaryExpression.getyValue().getClass());
        int compare = JdbcTypes.compareNumericTypes(sqlType1, sqlType2);
        log.debug("calculateSqlType: sqlType1={}, sqlType2={}, compare={}", sqlType1, sqlType2,
                compare);
        return compare < 0 ? sqlType2 : sqlType1;
    }


    private Integer calculateSqlTypeFromExpression(
            Expression<?> expression,
            Expression<?> expression2,
            Object value) {
        AttributePath<?> attributePath1 = (AttributePath<?>) expression;
        log.debug("calculateSqlTypeFromExpression: attributePath1={}", attributePath1);
        AbstractMetaAttribute metaAttribute1 = attributePath1.getMetaAttribute();
        Integer sqlType1 = metaAttribute1.getSqlType();
        log.debug("calculateSqlTypeFromExpression: sqlType1={}", sqlType1);
        if (expression2 != null) {
            AttributePath<?> attributePath2 = (AttributePath<?>) expression2;
            AbstractMetaAttribute metaAttribute2 = attributePath2.getMetaAttribute();
            Integer sqlType2 = metaAttribute2.getSqlType();
            int compare = JdbcTypes.compareNumericTypes(sqlType1, sqlType2);
            log.debug("calculateSqlTypeFromExpression: sqlType1={}, sqlType2={}, compare={}", sqlType1,
                    sqlType2, compare);
            return compare < 0 ? sqlType2 : sqlType1;
        }

        Integer sqlType2 = JdbcTypes.sqlTypeFromClass(value.getClass());
        int compare = JdbcTypes.compareNumericTypes(sqlType1, sqlType2);
        log.debug("calculateSqlTypeFromExpression: value={}", value);
        log.debug("calculateSqlTypeFromExpression: sqlType1={}, sqlType2={}, compare={}", sqlType1,
                sqlType2, compare);
        return compare < 0 ? sqlType2 : sqlType1;
    }


    private boolean checkDataType(AttributePath<?> attributePath, Integer sqlType) {
        AbstractMetaAttribute metaAttribute = attributePath.getMetaAttribute();
        log.debug("checkDataType: sqlType={}, metaAttribute.getSqlType()={}", sqlType,
                metaAttribute.getSqlType());
        if (sqlType != null && sqlType.equals(metaAttribute.getSqlType())) {
            return true;
        }

        return false;
    }


    private TableColumn createTableColumnFromPath(
            AttributePath<?> attributePath,
            AliasGenerator tableAliasGenerator) {
        AbstractMetaAttribute attribute = attributePath.getMetaAttribute();
        return new TableColumn(FromTable.of(attributePath.getMetaEntity().getTableName(),
                tableAliasGenerator.getDefault(attributePath.getMetaEntity().getTableName())),
                new Column(attribute.getColumnName()));
    }

    private ConditionType getOperator(PredicateType predicateType) {
        switch (predicateType) {
            case EQUAL:
                return ConditionType.EQUAL;
            case NOT_EQUAL:
                return ConditionType.NOT_EQUAL;
            case AND:
                return ConditionType.AND;
            case IS_NOT_NULL:
                return ConditionType.IS_NOT_NULL;
            case IS_NULL:
                return ConditionType.IS_NULL;
            case EQUALS_TRUE:
                return ConditionType.EQUALS_TRUE;
            case EQUALS_FALSE:
                return ConditionType.EQUALS_FALSE;
            case NOT:
                return ConditionType.NOT;
            case OR:
                return ConditionType.OR;
            case EMPTY_CONJUNCTION:
                return ConditionType.AND;
            case EMPTY_DISJUNCTION:
                return ConditionType.OR;
            case GREATER_THAN:
            case GT:
                return ConditionType.GREATER_THAN;
            case GREATER_THAN_OR_EQUAL_TO:
                return ConditionType.GREATER_THAN_OR_EQUAL_TO;
            case LESS_THAN:
            case LT:
                return ConditionType.LESS_THAN;
            case LESS_THAN_OR_EQUAL_TO:
                return ConditionType.LESS_THAN_OR_EQUAL_TO;
            case BETWEEN_EXPRESSIONS:
            case BETWEEN_VALUES:
                return ConditionType.BETWEEN;
            case LIKE_PATTERN:
            case LIKE_PATTERN_EXPR:
                return ConditionType.LIKE;
            case IN:
                return ConditionType.IN;
            default:
                break;
        }

        throw new IllegalArgumentException(
                "Unknown condition type for predicate type: " + predicateType);
    }

    private Optional<Condition> translateComparisonPredicate(
            ComparisonPredicate comparisonPredicate,
            List<QueryParameter> parameters,
            Map<Parameter<?>, Object> parameterMap,
            AliasGenerator tableAliasGenerator) {
        Expression<?> expression1 = comparisonPredicate.getX();
        Expression<?> expression2 = comparisonPredicate.getY();

        ConditionType conditionType = getOperator(comparisonPredicate.getPredicateType());
        BinaryCondition.Builder builder = new BinaryCondition.Builder(conditionType);
        if (comparisonPredicate.isNot()) {
            builder.not();
        }

        if (expression1 instanceof AttributePath<?>) {
            AttributePath<?> attributePath = (AttributePath<?>) expression1;
            AbstractMetaAttribute attribute1 = attributePath.getMetaAttribute();
            TableColumn tableColumn1 = createTableColumnFromPath(attributePath, tableAliasGenerator);
            if (expression2 != null) {
                Object p = criteriaExpressionHelper.createParameterFromExpression(
                        parameterMap,
                        expression2,
                        tableAliasGenerator, parameters, attribute1.getColumnName(), attribute1.getSqlType(),
                        attribute1.getObjectConverter());
                builder.withLeft(tableColumn1).withRight(p);
            } else if (comparisonPredicate.getValue() != null) {
                if (CriteriaUtils.requireQM(comparisonPredicate.getValue())) {
                    QueryParameter queryParameter = new QueryParameter(attribute1.getColumnName(),
                            comparisonPredicate.getValue(), attribute1.getSqlType(),
                            attribute1.getObjectConverter());
                    parameters.add(queryParameter);
                    builder.withLeft(tableColumn1).withRight(CriteriaUtils.QM);
                } else {
                    builder.withLeft(tableColumn1)
                            .withRight(CriteriaUtils.buildValue(comparisonPredicate.getValue()));
                }
            }

            return Optional.of(builder.build());
        }

        return Optional.empty();
    }

    private Optional<Condition> translateBetweenExpressionsPredicate(
            BetweenExpressionsPredicate betweenExpressionsPredicate,
            List<QueryParameter> parameters,
            Map<Parameter<?>, Object> parameterMap,
            AliasGenerator tableAliasGenerator) {
        Expression<?> expression1 = betweenExpressionsPredicate.getX();
        Expression<?> expression2 = betweenExpressionsPredicate.getY();
        AttributePath<?> miniPath = (AttributePath<?>) betweenExpressionsPredicate.getV();
        AbstractMetaAttribute attribute = miniPath.getMetaAttribute();

        BetweenCondition.Builder builder = new BetweenCondition.Builder(
                createTableColumnFromPath(miniPath, tableAliasGenerator));
        builder.withLeftExpression(
                criteriaExpressionHelper.createParameterFromExpression(
                        parameterMap,
                        expression1,
                        tableAliasGenerator, parameters, attribute.getColumnName(), attribute.getSqlType(),
                        attribute.getObjectConverter()));

        builder.withRightExpression(
                criteriaExpressionHelper.createParameterFromExpression(
                        parameterMap, expression2,
                        tableAliasGenerator, parameters, attribute.getColumnName(), attribute.getSqlType(),
                        attribute.getObjectConverter()));

        return Optional.of(builder.build());
    }

    private Optional<Condition> translateBetweenValuesPredicate(
            BetweenValuesPredicate betweenValuesPredicate,
            List<QueryParameter> parameters,
            AliasGenerator tableAliasGenerator) {
        Object x = betweenValuesPredicate.getX();
        Object y = betweenValuesPredicate.getY();
        AttributePath<?> miniPath = (AttributePath<?>) betweenValuesPredicate.getV();
        AbstractMetaAttribute attribute = miniPath.getMetaAttribute();

        BetweenCondition.Builder builder = new BetweenCondition.Builder(
                createTableColumnFromPath(miniPath, tableAliasGenerator));
        if (CriteriaUtils.requireQM(x)) {
            QueryParameter queryParameter = new QueryParameter(attribute.getColumnName(), x,
                    attribute.getSqlType(), attribute.getObjectConverter());
            parameters.add(queryParameter);
            builder.withLeftExpression(CriteriaUtils.QM);
        } else {
            builder.withLeftExpression(CriteriaUtils.buildValue(x));
        }

        if (CriteriaUtils.requireQM(y)) {
            QueryParameter queryParameter = new QueryParameter(attribute.getColumnName(), y,
                    attribute.getSqlType(), attribute.getObjectConverter());
            parameters.add(queryParameter);
            builder.withRightExpression(CriteriaUtils.QM);
        } else {
            builder.withRightExpression(CriteriaUtils.buildValue(y));
        }

        return Optional.of(builder.build());
    }

    private Optional<Condition> translateBooleanExprPredicate(
            BooleanExprPredicate booleanExprPredicate,
            List<QueryParameter> parameters,
            Map<Parameter<?>, Object> parameterMap,
            AliasGenerator tableAliasGenerator) {
        Expression<Boolean> x = booleanExprPredicate.getX();

        if (x instanceof Predicate) {
            Optional<Condition> optional = createConditions((Predicate) x, parameters, parameterMap,
                    tableAliasGenerator);
            if (optional.isPresent()) {
                return Optional.of(
                        new UnaryLogicConditionImpl(getOperator(booleanExprPredicate.getPredicateType()),
                                optional.get()));
            }
        }

        if (x instanceof AttributePath<?>) {
            AttributePath<?> miniPath = (AttributePath<?>) x;
            return Optional.of(new UnaryCondition(getOperator(booleanExprPredicate.getPredicateType()),
                    createTableColumnFromPath(miniPath, tableAliasGenerator)));
        }

        return Optional.empty();
    }

    private Optional<Condition> translateExprPredicate(
            ExprPredicate exprPredicate,
            List<QueryParameter> parameters,
            Map<Parameter<?>, Object> parameterMap,
            AliasGenerator aliasGenerator) {
        Expression<?> x = exprPredicate.getX();
        if (x instanceof AttributePath<?>) {
            AttributePath<?> miniPath = (AttributePath<?>) x;
            return Optional.of(new UnaryCondition(getOperator(exprPredicate.getPredicateType()),
                    createTableColumnFromPath(miniPath, aliasGenerator)));
        }

        return Optional.empty();
    }

    private Optional<Condition> translateMultiplePredicate(
            MultiplePredicate multiplePredicate,
            List<QueryParameter> parameters,
            Map<Parameter<?>, Object> parameterMap,
            AliasGenerator tableAliasGenerator) {
        Predicate[] predicates = multiplePredicate.getRestrictions();
        List<Condition> conditions = new ArrayList<>();
        for (Predicate p : predicates) {
            Optional<Condition> optional = createConditions(p, parameters, parameterMap, tableAliasGenerator);
            optional.ifPresent(conditions::add);
        }

        if (!conditions.isEmpty()) {
            return Optional.of(
                    new BinaryLogicConditionImpl(getOperator(multiplePredicate.getPredicateType()),
                            conditions, true));
        }

        return Optional.empty();
    }

    private Optional<Condition> translateBinaryBooleanExprPredicate(
            BinaryBooleanExprPredicate binaryBooleanExprPredicate,
            List<QueryParameter> parameters,
            Map<Parameter<?>, Object> parameterMap,
            AliasGenerator tableAliasGenerator) {
        Expression<Boolean> x = binaryBooleanExprPredicate.getX();
        Expression<Boolean> y = binaryBooleanExprPredicate.getY();

        log.debug("translateBinaryBooleanExprPredicate: x={}", x);
        log.debug("translateBinaryBooleanExprPredicate: y={}", y);
        List<Condition> conditions = new ArrayList<>();
        if (x instanceof Predicate) {
            Optional<Condition> optional = createConditions((Predicate) x, parameters, parameterMap,
                    tableAliasGenerator);
            optional.ifPresent(conditions::add);
        }

        if (y instanceof Predicate) {
            Optional<Condition> optional = createConditions((Predicate) y, parameters, parameterMap,
                    tableAliasGenerator);
            optional.ifPresent(conditions::add);
        }

        if (!conditions.isEmpty()) {
            return Optional.of(
                    new BinaryLogicConditionImpl(getOperator(binaryBooleanExprPredicate.getPredicateType()),
                            conditions, true));
        }

        return Optional.empty();
    }

    private Optional<Condition> translateInPredicate(
            InPredicate<?> inPredicate,
            List<QueryParameter> parameters,
            Map<Parameter<?>, Object> parameterMap,
            AliasGenerator tableAliasGenerator) {
        Expression<?> expression = inPredicate.getExpression();
        log.info("translateInPredicate: expression={}", expression);
        if (expression instanceof AttributePath<?>) {
            AttributePath<?> miniPath = (AttributePath<?>) expression;
            AbstractMetaAttribute attribute = miniPath.getMetaAttribute();
            TableColumn tableColumn = createTableColumnFromPath(miniPath, tableAliasGenerator);

            List<String> list = inPredicate.getValues().stream().map(v -> {
                return CriteriaUtils.QM;
            }).collect(Collectors.toList());

            List<QueryParameter> queryParameters = inPredicate.getValues().stream().map(v -> {
                return new QueryParameter(attribute.getColumnName(), v, attribute.getSqlType(),
                        attribute.getObjectConverter());
            }).collect(Collectors.toList());
            parameters.addAll(queryParameters);

            return Optional.of(new InCondition(tableColumn, list, inPredicate.isNot()));
        }

        return Optional.empty();
    }

    private Optional<Condition> translateLikePredicate(
            LikePredicate likePredicate,
            List<QueryParameter> parameters,
            Map<Parameter<?>, Object> parameterMap,
            AliasGenerator aliasGenerator) {
        Expression<String> patternExpression = likePredicate.getPatternExpression();
        String pattern = likePredicate.getPattern();
        AttributePath<?> miniPath = (AttributePath<?>) likePredicate.getX();
        AbstractMetaAttribute attribute = miniPath.getMetaAttribute();
        Object right = null;
        if (pattern != null) {
            right = CriteriaUtils.buildValue(pattern);
        } else if (patternExpression != null) {
            right = criteriaExpressionHelper.createParameterFromExpression(
                    parameterMap,
                    patternExpression,
                    aliasGenerator, parameters, attribute.getColumnName(), attribute.getSqlType(),
                    attribute.getObjectConverter());
        }

        String escapeChar = null;
        Character escapeCharacter = likePredicate.getEscapeChar();
        Expression<Character> escapeExpression = likePredicate.getEscapeCharEx();
        if (escapeCharacter != null) {
            escapeChar = CriteriaUtils.buildValue(String.valueOf(likePredicate.getEscapeChar()));
        } else if (escapeExpression != null) {
            if (escapeExpression instanceof ParameterExpression<?>) {
                ParameterExpression<?> parameterExpression = (ParameterExpression<?>) escapeExpression;

                // TODO: escape char not working on Derby (OneToManyUniTest.testLike5). Replaced
                // with string value
//                parameters.add(createQueryParameterForParameterExpression(parameterExpression, "escape",
//                        Character.class, Types.CHAR, query));
//                escapeChar = QM;

                Object value = null;
                if (parameterExpression.getName() != null) {
                    value = ParameterUtils.findParameterValueByName(parameterExpression.getName(), parameterMap);
                } else if (parameterExpression.getPosition() != null) {
                    value = ParameterUtils.findParameterByPosition(parameterExpression.getPosition(), parameterMap);
                }

                escapeChar = CriteriaUtils.buildValue(String.valueOf((char) value));
            }
        }

        LikeCondition likeCondition = new LikeCondition(
                createTableColumnFromPath(miniPath, aliasGenerator), right, escapeChar,
                likePredicate.isNot());

        return Optional.of(likeCondition);
    }

    private Optional<Condition> createConditions(
            Predicate predicate,
            List<QueryParameter> parameters,
            Map<Parameter<?>, Object> parameterMap,
            AliasGenerator tableAliasGenerator) {
        PredicateTypeInfo predicateTypeInfo = (PredicateTypeInfo) predicate;
        PredicateType predicateType = predicateTypeInfo.getPredicateType();
        if (predicateType == PredicateType.EQUAL || predicateType == PredicateType.NOT_EQUAL
                || predicateType == PredicateType.GREATER_THAN
                || predicateType == PredicateType.GREATER_THAN_OR_EQUAL_TO
                || predicateType == PredicateType.GT || predicateType == PredicateType.LESS_THAN
                || predicateType == PredicateType.LESS_THAN_OR_EQUAL_TO
                || predicateType == PredicateType.LT) {
            return translateComparisonPredicate((ComparisonPredicate) predicate, parameters, parameterMap,
                    tableAliasGenerator);
        } else if (predicateType == PredicateType.BETWEEN_EXPRESSIONS) {
            BetweenExpressionsPredicate betweenExpressionsPredicate = (BetweenExpressionsPredicate) predicate;
            return translateBetweenExpressionsPredicate(betweenExpressionsPredicate, parameters, parameterMap,
                    tableAliasGenerator);
        } else if (predicateType == PredicateType.BETWEEN_VALUES) {
            BetweenValuesPredicate betweenValuesPredicate = (BetweenValuesPredicate) predicate;
            return translateBetweenValuesPredicate(betweenValuesPredicate, parameters,
                    tableAliasGenerator);
        } else if (predicateType == PredicateType.LIKE_PATTERN) {
            LikePredicate likePredicate = (LikePredicate) predicate;
            return translateLikePredicate(likePredicate, parameters, parameterMap, tableAliasGenerator);
        } else if (predicateType == PredicateType.OR || predicateType == PredicateType.AND) {
            if (predicate instanceof MultiplePredicate) {
                return translateMultiplePredicate((MultiplePredicate) predicate, parameters, parameterMap,
                        tableAliasGenerator);
            } else {
                return translateBinaryBooleanExprPredicate((BinaryBooleanExprPredicate) predicate,
                        parameters, parameterMap, tableAliasGenerator);
            }
        } else if (predicateType == PredicateType.NOT) {
            return translateBooleanExprPredicate((BooleanExprPredicate) predicate, parameters, parameterMap,
                    tableAliasGenerator);
        } else if (predicateType == PredicateType.IS_NULL
                || predicateType == PredicateType.IS_NOT_NULL) {
            return translateExprPredicate((ExprPredicate) predicate, parameters, parameterMap,
                    tableAliasGenerator);
        } else if (predicateType == PredicateType.EQUALS_TRUE
                || predicateType == PredicateType.EQUALS_FALSE) {
            return translateBooleanExprPredicate((BooleanExprPredicate) predicate, parameters, parameterMap,
                    tableAliasGenerator);
        } else if (predicateType == PredicateType.EMPTY_CONJUNCTION
                || predicateType == PredicateType.EMPTY_DISJUNCTION) {
            return Optional.of(new EmptyCondition(getOperator(predicateType)));
        } else if (predicateType == PredicateType.IN) {
            return translateInPredicate((InPredicate<?>) predicate, parameters, parameterMap,
                    tableAliasGenerator);
        }

        return Optional.empty();
    }

    private Optional<List<OrderBy>> createOrderByList(CriteriaQuery<?> criteriaQuery,
                                                      AliasGenerator tableAliasGenerator) {
        if (criteriaQuery.getOrderList() == null || criteriaQuery.getOrderList().isEmpty()) {
            return Optional.empty();
        }

        List<OrderBy> result = new ArrayList<>();
        List<Order> orders = criteriaQuery.getOrderList();
        for (Order order : orders) {
            Expression<?> expression = order.getExpression();
            if (expression instanceof AttributePath<?>) {
                AttributePath<?> miniPath = (AttributePath<?>) expression;
                AbstractMetaAttribute metaAttribute = miniPath.getMetaAttribute();
                TableColumn tableColumn = new TableColumn(
                        FromTable.of(miniPath.getMetaEntity().getTableName(),
                                tableAliasGenerator.getDefault(miniPath.getMetaEntity().getTableName())),
                        new Column(metaAttribute.getColumnName()));
                OrderByType orderByType = order.isAscending() ? OrderByType.ASC : OrderByType.DESC;
                OrderBy orderBy = new OrderBy(tableColumn, orderByType);
                result.add(orderBy);
            }
        }

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(result);
    }

    private JoinType decodeJoinType(javax.persistence.criteria.JoinType jt) {
        switch (jt) {
            case INNER:
                return JoinType.Inner;
            case LEFT:
                return JoinType.Left;
            case RIGHT:
                return JoinType.Right;
        }

        throw new IllegalArgumentException("Join Type not supported: " + jt);
    }

    private StatementType getStatementType(Set<Root<?>> roots) {
        for (Root<?> root : roots) {
            for (Join join : root.getJoins()) {
                if (join instanceof CollectionJoinImpl) {
                    if (((CollectionJoinImpl) join).getFetchJoinType() == FetchJoinType.FETCH_JOIN) {
                        return StatementType.FETCH_JOIN;
                    }
                }
            }
        }

        return StatementType.PLAIN;
    }

    public List<Join> getJoins(Set<Root<?>> roots) {
        List<Join> joins = new ArrayList<>();
        roots.forEach(root -> {
            joins.addAll(root.getJoins());
        });

        return joins;
    }

    private List<MetaEntity> getFetchJoinEntities(Set<Root<?>> roots) {
        List<MetaEntity> metaEntities = new ArrayList<>();
        roots.forEach(root -> {
            root.getJoins().forEach(join -> {
                if (join instanceof CollectionJoinImpl) {
                    if (((CollectionJoinImpl) join).getFetchJoinType() == FetchJoinType.FETCH_JOIN) {
                        metaEntities.add(((CollectionJoinImpl) join).getMetaEntity());
                    }
                }
            });
        });

        return metaEntities;
    }

    private List<RelationshipMetaAttribute> getFetchJoinMetaAttributes(Set<Root<?>> roots) {
        List<RelationshipMetaAttribute> metaAttributes = new ArrayList<>();
        roots.forEach(root -> {
            root.getJoins().forEach(join -> {
                if (join instanceof CollectionJoinImpl) {
                    if (((CollectionJoinImpl) join).getFetchJoinType() == FetchJoinType.FETCH_JOIN) {
                        metaAttributes.add(((CollectionJoinImpl) join).getMetaAttribute());
                    }
                }
            });
        });

        return metaAttributes;
    }

    private List<From> buildFromClauseFromJoins(
            Set<Root<?>> roots,
            AliasGenerator aliasGenerator) {
        List<From> froms = new ArrayList<>();
        roots.forEach(root -> {
            MetaEntity entity = ((MiniRoot<?>) root).getMetaEntity();
            froms.add(
                    FromTable.of(entity.getTableName(), aliasGenerator.getDefault(entity.getTableName())));
            root.getJoins().forEach(join -> {
                if (join instanceof CollectionJoin) {
                    CollectionJoin collectionJoin = (CollectionJoin) join;
                    Attribute attribute = collectionJoin.getAttribute();
                    String attributeName = attribute.getName();
                    RelationshipMetaAttribute metaAttribute = entity.getRelationshipAttribute(attributeName);
                    List<FromJoin> fromJoins = calculateJoins(entity, metaAttribute,
                            decodeJoinType(collectionJoin.getJoinType()), aliasGenerator);
                    froms.addAll(fromJoins);
                }
            });
        });

        return froms;
    }

    private List<Value> buildValues(
            MiniRoot<?> miniRoot,
            List<FromTable> fromTables,
            AliasGenerator aliasGenerator) {
        MetaEntity entity = miniRoot.getMetaEntity();
        Optional<FromTable> optional = fromTables.stream()
                .filter(ft -> ft.getName().equals(entity.getTableName())).findFirst();

        List<Value> values = MetaEntityHelper.toValues(entity, optional.get());
        log.debug("buildValues: values={}", values);
        miniRoot.getJoins().forEach(j -> {
            if (j instanceof Fetch) {
                AbstractFrom from = (AbstractFrom) j;
                MetaEntity joinMetaEntity = from.getMetaEntity();
                FromTable fetchFromTable = FromTable.of(joinMetaEntity.getTableName(),
                        aliasGenerator.getDefault(joinMetaEntity.getTableName()));
                values.addAll(MetaEntityHelper.toValues(joinMetaEntity, fetchFromTable));
            }
        });

        return values;
    }

    private List<Value> buildSelectionValues(
            CriteriaQuery<?> criteriaQuery,
            List<FromTable> fromTables,
            Map<Parameter<?>, Object> parameterValues,
            AliasGenerator aliasGenerator,
            List<QueryParameter> parameters) {
        Selection<?> selection = criteriaQuery.getSelection();
        if (selection == null) {
            List<Value> values = new ArrayList<>();
            criteriaQuery.getRoots()
                    .forEach(r -> values.addAll(buildValues((MiniRoot<?>) r, fromTables, aliasGenerator)));
            return values;
        }

        if (selection instanceof MiniRoot<?>) {
            return buildValues((MiniRoot<?>) selection, fromTables, aliasGenerator);
        }

        List<Value> values = new ArrayList<>();
        if (selection.isCompoundSelection()) {
            List<Selection<?>> selections = selection.getCompoundSelectionItems();
            for (Selection<?> s : selections) {
                FromTable fromTable = null; // get FromTable from selection
                Optional<Value> optional = criteriaExpressionHelper.createSelectionValue(fromTable,
                        aliasGenerator, s, parameterValues, parameters);
                optional.ifPresent(values::add);
            }

            return values;
        }

        FromTable fromTable = null; // get FromTable from selection
        log.debug("buildSelectionValues: selection={}", selection);
        Optional<Value> optional = criteriaExpressionHelper.createSelectionValue(fromTable,
                aliasGenerator, selection, parameterValues, parameters);
        optional.ifPresent(values::add);
        return values;
    }

    private List<FetchParameter> createFetchParameters(CriteriaQuery<?> criteriaQuery,
                                                       Class<?> resultClass) {
        Selection<?> selection = criteriaQuery.getSelection();
        if (selection == null) {
            List<FetchParameter> fetchParameters = new ArrayList<>();
            criteriaQuery.getRoots().forEach(r -> {
                MetaEntity entity = ((MiniRoot<?>) r).getMetaEntity();
                fetchParameters.addAll(MetaEntityHelper.convertAllAttributes(entity));

                // fetch join cases
                r.getJoins().forEach(join -> {
                    log.debug("createFetchParameters: join instanceof Fetch={}", (join instanceof Fetch));
                    if (join instanceof Fetch) {
                        AbstractFrom from = (AbstractFrom) join;
                        MetaEntity fetchMetaEntity = from.getMetaEntity();
                        log.debug("createFetchParameters: fetchMetaEntity={}", fetchMetaEntity);
                        fetchParameters.addAll(MetaEntityHelper.convertAllAttributes(fetchMetaEntity));
                    }
                });
            });

            return fetchParameters;
        }

        if (selection instanceof MiniRoot<?>) {
            MetaEntity entity = ((MiniRoot<?>) selection).getMetaEntity();

            List<FetchParameter> fetchParameters = new ArrayList<>();
            fetchParameters.addAll(MetaEntityHelper.convertAllAttributes(entity));

            ((MiniRoot) selection).getJoins().forEach(j -> {
                if (j instanceof Fetch) {
                    AbstractFrom from = (AbstractFrom) j;
                    MetaEntity joinMetaEntity = from.getMetaEntity();
                    fetchParameters.addAll(MetaEntityHelper.convertAllAttributes(joinMetaEntity));
                }
            });

            return fetchParameters;
        }

        List<FetchParameter> values = new ArrayList<>();
        if (selection.isCompoundSelection()) {
            List<Selection<?>> selections = selection.getCompoundSelectionItems();
            for (Selection<?> s : selections) {
                Optional<FetchParameter> optional = createFetchParameter(s, resultClass);
                optional.ifPresent(values::add);
            }
        } else {
            Optional<FetchParameter> optional = createFetchParameter(selection, resultClass);
            optional.ifPresent(values::add);
        }

        return values;
    }

    private Optional<Predicate> filter(
            ComparisonPredicate comparisonPredicate,
            MetaEntity metaEntity) {
        Expression<?> expression1 = comparisonPredicate.getX();
        Expression<?> expression2 = comparisonPredicate.getY();

        if (expression1 instanceof AttributePath<?>) {
            AttributePath<?> attributePath = (AttributePath<?>) expression1;
            if (attributePath.getMetaEntity() == metaEntity)
                return Optional.empty();
        }

        if (expression2 instanceof AttributePath<?>) {
            AttributePath<?> attributePath = (AttributePath<?>) expression2;
            if (attributePath.getMetaEntity() == metaEntity)
                return Optional.empty();
        }

        return Optional.of(comparisonPredicate);
    }

    private Optional<Predicate> filter(
            BetweenExpressionsPredicate betweenExpressionsPredicate,
            MetaEntity metaEntity) {
        Expression<?> expression1 = betweenExpressionsPredicate.getX();
        Expression<?> expression2 = betweenExpressionsPredicate.getY();
        AttributePath<?> miniPath = (AttributePath<?>) betweenExpressionsPredicate.getV();

        if (miniPath.getMetaEntity() == metaEntity)
            return Optional.empty();

        if (expression1 instanceof AttributePath<?>) {
            AttributePath<?> attributePath = (AttributePath<?>) expression1;
            if (attributePath.getMetaEntity() == metaEntity)
                return Optional.empty();
        }

        if (expression2 instanceof AttributePath<?>) {
            AttributePath<?> attributePath = (AttributePath<?>) expression2;
            if (attributePath.getMetaEntity() == metaEntity)
                return Optional.empty();
        }

        return Optional.of(betweenExpressionsPredicate);
    }

    private Optional<Predicate> filter(
            LikePredicate likePredicate,
            MetaEntity metaEntity) {
        Expression<String> patternExpression = likePredicate.getPatternExpression();
        AttributePath<?> miniPath = (AttributePath<?>) likePredicate.getX();
        if (miniPath.getMetaEntity() == metaEntity)
            return Optional.empty();

        if (patternExpression != null) {
            if (patternExpression instanceof AttributePath<?>) {
                AttributePath<?> attributePath = (AttributePath<?>) patternExpression;
                if (attributePath.getMetaEntity() == metaEntity)
                    return Optional.empty();
            }
        }

        Expression<Character> escapeExpression = likePredicate.getEscapeCharEx();
        if (escapeExpression != null) {
            if (escapeExpression instanceof AttributePath<?>) {
                AttributePath<?> attributePath = (AttributePath<?>) escapeExpression;
                if (attributePath.getMetaEntity() == metaEntity)
                    return Optional.empty();
            }
        }

        return Optional.of(likePredicate);
    }

    private Optional<Predicate> filter(
            MultiplePredicate multiplePredicate,
            MetaEntity metaEntity) {
        Predicate[] predicates = multiplePredicate.getRestrictions();
        List<Predicate> conditions = new ArrayList<>();
        for (Predicate p : predicates) {
            Optional<Predicate> optional = filter(p, metaEntity);
            optional.ifPresent(conditions::add);
        }

        if (conditions.isEmpty())
            return Optional.empty();

        return Optional.of(multiplePredicate);
    }

    private Optional<Predicate> filter(
            BinaryBooleanExprPredicate binaryBooleanExprPredicate,
            MetaEntity metaEntity) {
        Expression<Boolean> x = binaryBooleanExprPredicate.getX();
        Expression<Boolean> y = binaryBooleanExprPredicate.getY();

        List<Predicate> conditions = new ArrayList<>();
        if (x instanceof Predicate) {
            Optional<Predicate> optional = filter((Predicate) x, metaEntity);
            optional.ifPresent(conditions::add);
        }

        if (y instanceof Predicate) {
            Optional<Predicate> optional = filter((Predicate) y, metaEntity);
            optional.ifPresent(conditions::add);
        }

        if (conditions.isEmpty())
            return Optional.empty();

        return Optional.of(binaryBooleanExprPredicate);
    }

    private Optional<Predicate> filter(
            BooleanExprPredicate booleanExprPredicate,
            MetaEntity metaEntity) {
        Expression<Boolean> x = booleanExprPredicate.getX();

        if (x instanceof Predicate) {
            Optional<Predicate> optional = filter((Predicate) x, metaEntity);
            if (optional.isEmpty())
                return Optional.empty();
        }

        if (x instanceof AttributePath<?>) {
            AttributePath<?> miniPath = (AttributePath<?>) x;
            if (miniPath.getMetaEntity() == metaEntity)
                return Optional.empty();
        }

        return Optional.of(booleanExprPredicate);
    }

    private Optional<Predicate> filter(
            ExprPredicate exprPredicate,
            MetaEntity metaEntity) {
        Expression<?> x = exprPredicate.getX();
        if (x instanceof AttributePath<?>) {
            AttributePath<?> miniPath = (AttributePath<?>) x;
            if (miniPath.getMetaEntity() == metaEntity)
                return Optional.empty();
        }

        return Optional.of(exprPredicate);
    }

    private Optional<Predicate> filter(
            InPredicate<?> inPredicate,
            MetaEntity metaEntity) {
        Expression<?> expression = inPredicate.getExpression();
        log.info("translateInPredicate: expression={}", expression);
        if (expression instanceof AttributePath<?>) {
            AttributePath<?> miniPath = (AttributePath<?>) expression;
            if (miniPath.getMetaEntity() == metaEntity)
                return Optional.empty();
        }

        return Optional.of(inPredicate);
    }

    private Optional<Predicate> filter(Predicate predicate, MetaEntity metaEntity) {
        PredicateTypeInfo predicateTypeInfo = (PredicateTypeInfo) predicate;
        PredicateType predicateType = predicateTypeInfo.getPredicateType();
        if (predicateType == PredicateType.EQUAL || predicateType == PredicateType.NOT_EQUAL
                || predicateType == PredicateType.GREATER_THAN
                || predicateType == PredicateType.GREATER_THAN_OR_EQUAL_TO
                || predicateType == PredicateType.GT || predicateType == PredicateType.LESS_THAN
                || predicateType == PredicateType.LESS_THAN_OR_EQUAL_TO
                || predicateType == PredicateType.LT) {
            return filter((ComparisonPredicate) predicate, metaEntity);
        }

        if (predicateType == PredicateType.BETWEEN_EXPRESSIONS) {
            BetweenExpressionsPredicate betweenExpressionsPredicate = (BetweenExpressionsPredicate) predicate;
            return filter(betweenExpressionsPredicate, metaEntity);
        }

        if (predicateType == PredicateType.BETWEEN_VALUES) {
            BetweenValuesPredicate betweenValuesPredicate = (BetweenValuesPredicate) predicate;
            AttributePath<?> miniPath = (AttributePath<?>) betweenValuesPredicate.getV();

            if (miniPath.getMetaEntity() == metaEntity)
                return Optional.empty();

            return Optional.of(betweenValuesPredicate);
        }

        if (predicateType == PredicateType.LIKE_PATTERN) {
            LikePredicate likePredicate = (LikePredicate) predicate;
            return filter(likePredicate, metaEntity);
        }

        if (predicateType == PredicateType.OR || predicateType == PredicateType.AND) {
            if (predicate instanceof MultiplePredicate) {
                return filter((MultiplePredicate) predicate, metaEntity);
            } else {
                return filter((BinaryBooleanExprPredicate) predicate, metaEntity);
            }
        } else if (predicateType == PredicateType.NOT) {
            return filter((BooleanExprPredicate) predicate, metaEntity);
        } else if (predicateType == PredicateType.IS_NULL
                || predicateType == PredicateType.IS_NOT_NULL) {
            return filter((ExprPredicate) predicate, metaEntity);
        } else if (predicateType == PredicateType.EQUALS_TRUE
                || predicateType == PredicateType.EQUALS_FALSE) {
            return filter((BooleanExprPredicate) predicate, metaEntity);
        } else if (predicateType == PredicateType.EMPTY_CONJUNCTION
                || predicateType == PredicateType.EMPTY_DISJUNCTION) {
            return Optional.of(predicate);
        } else if (predicateType == PredicateType.IN) {
            return filter((InPredicate<?>) predicate, metaEntity);
        }

        return Optional.empty();
    }

    private Optional<List<Order>> filterOrderByList(
            List<Order> orderList,
            MetaEntity metaEntity) {
        if (orderList == null || orderList.isEmpty()) {
            return Optional.empty();
        }

        List<Order> result = new ArrayList<>();
        for (Order order : orderList) {
            Expression<?> expression = order.getExpression();
            if (expression instanceof AttributePath<?>) {
                AttributePath<?> miniPath = (AttributePath<?>) expression;
                if (miniPath.getMetaEntity() != metaEntity)
                    result.add(order);
            }
        }

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(result);
    }

    public StatementParameters select(
            CriteriaQuery<?> criteriaQuery,
            LockModeType lockModeType,
            Map<Parameter<?>, Object> parameterMap,
            AliasGenerator aliasGenerator) {
        Predicate restriction = criteriaQuery.getRestriction();
        List<QueryParameter> parameters = new ArrayList<>();
        Optional<Condition> optionalCondition = Optional.empty();
        if (restriction != null) {
            optionalCondition = createConditions(restriction, parameters, parameterMap, aliasGenerator);
        }

        List<Condition> conditions = null;
        if (optionalCondition.isPresent()) {
            conditions = List.of(optionalCondition.get());
        }

        SqlSelectDataBuilder builder = new SqlSelectDataBuilder();
        List<From> froms = buildFromClauseFromJoins(criteriaQuery.getRoots(), aliasGenerator);
        froms.forEach(builder::withFromTable);
        log.debug("select: froms={}", froms);

        LockType lockType = LockTypeUtils.toLockType(lockModeType);
        if (lockType != null) {
            builder.withForUpdate(calcForUpdate(lockType));
        }

        Optional<List<OrderBy>> optionalOrderBy = createOrderByList(criteriaQuery, aliasGenerator);
        optionalOrderBy.ifPresent(builder::withOrderBy);

        if (criteriaQuery.isDistinct()) {
            builder.distinct();
        }

        Selection<?> selection = criteriaQuery.getSelection();
        log.debug("select: selection={}", selection);
        List<FromTable> fromTables = froms.stream().filter(from -> from instanceof FromTable)
                .map(from -> (FromTable) from)
                .collect(Collectors.toList());
        List<Value> values = buildSelectionValues(criteriaQuery, fromTables,
                parameterMap, aliasGenerator,
                parameters);
        builder.withValues(values);
        builder.withConditions(conditions);
        List<FetchParameter> fetchParameters = createFetchParameters(criteriaQuery,
                criteriaQuery.getResultType());
        builder.withFetchParameters(fetchParameters);

        // TODO to be fixed with more root case
        if (selection != null) {
            if (selection instanceof MiniRoot<?>) {
                MetaEntity entity = ((MiniRoot<?>) selection).getMetaEntity();
                Optional<FromTable> optional = fromTables.stream()
                        .filter(ft -> ft.getName().equals(entity.getTableName())).findFirst();
                optional.ifPresent(builder::withResult);
            }
        } else {
            fromTables.forEach(builder::withResult);
        }

        SqlSelect sqlSelect = builder.build();
        List<MetaEntity> metaEntities = getFetchJoinEntities(criteriaQuery.getRoots());
        if (!metaEntities.isEmpty()) {
            List<RelationshipMetaAttribute> metaAttributes = getFetchJoinMetaAttributes(
                    criteriaQuery.getRoots());
            return new StatementParameters(sqlSelect, parameters,
                    StatementType.FETCH_JOIN, metaEntities, metaAttributes);
        }

        return new StatementParameters(sqlSelect, parameters);
    }

    public CriteriaQuery filterCriteriaQuery(
            CriteriaQuery<?> criteriaQuery,
            MetaEntity metaEntity) {
        MiniCriteriaQuery miniCriteriaQuery = new MiniCriteriaQuery<>(
                criteriaQuery.getResultType(),
                ((MiniCriteriaQuery) criteriaQuery).getMetamodel(),
                ((MiniCriteriaQuery) criteriaQuery).getPersistenceUnitContext());

        Predicate predicate = criteriaQuery.getRestriction();
        Optional<Predicate> optionalPredicate = filter(predicate, metaEntity);
        if (optionalPredicate.isPresent())
            miniCriteriaQuery.where(optionalPredicate.get());

        Optional<List<Order>> optionalOrders = filterOrderByList(criteriaQuery.getOrderList(), metaEntity);
        if (optionalOrders.isPresent())
            miniCriteriaQuery.orderBy(optionalOrders.get());

        miniCriteriaQuery.distinct(criteriaQuery.isDistinct());
        miniCriteriaQuery.select(criteriaQuery.getSelection());

        criteriaQuery.getRoots().forEach(root -> {
            List<CollectionJoinImpl> collectionJoins = new ArrayList<>();
            root.getJoins().forEach(join -> {
                if (join instanceof CollectionJoinImpl) {
                    CollectionJoinImpl collectionJoin = (CollectionJoinImpl) join;
                    if (collectionJoin.getMetaEntity() != metaEntity) {
                        collectionJoins.add(collectionJoin);
                    }
                }
            });

            MiniRoot miniRoot = (MiniRoot) root;
            Root r = miniCriteriaQuery.from(miniRoot.getMetaEntity().getEntityClass());
            collectionJoins.forEach(join -> {
                if (join instanceof CollectionFetchJoinImpl) {
                    RelationshipMetaAttribute metaAttribute = ((CollectionJoinImpl) join).getMetaAttribute();
                    r.fetch(metaAttribute.getName());
                } else if (join instanceof CollectionJoinImpl) {
                    RelationshipMetaAttribute metaAttribute = ((CollectionJoinImpl) join).getMetaAttribute();
                    r.join(metaAttribute.getName());
                }
            });
        });

        return miniCriteriaQuery;
    }

    private QueryParameter createQueryParameter(AttributePath<?> miniPath, Object value) {
        AbstractMetaAttribute a = miniPath.getMetaAttribute();
        return new QueryParameter(a.getColumnName(), value, a.getSqlType(), a.getObjectConverter());
    }

    public SqlUpdate update(Query query,
                            List<QueryParameter> parameters,
                            AliasGenerator tableAliasGenerator) {
        CriteriaUpdate<?> criteriaUpdate = ((UpdateQuery) query).getCriteriaUpdate();
        MetaEntity entity = ((MiniRoot<?>) criteriaUpdate.getRoot()).getMetaEntity();

        FromTable fromTable = FromTable.of(entity.getTableName(),
                tableAliasGenerator.getDefault(entity.getTableName()));
        List<TableColumn> columns = new ArrayList<>();
        Map<Path<?>, Object> setValues = ((MiniCriteriaUpdate<?>) criteriaUpdate).getSetValues();
        setValues.forEach((k, v) -> {
            AttributePath<?> miniPath = (AttributePath<?>) k;
            TableColumn tableColumn = new TableColumn(fromTable,
                    new Column(miniPath.getMetaAttribute().getColumnName()));
            columns.add(tableColumn);
        });

        Predicate restriction = criteriaUpdate.getRestriction();
        Optional<Condition> optionalCondition = Optional.empty();
        if (restriction != null) {
            optionalCondition = createConditions(restriction, parameters, ((AbstractQuery) query).getParameterMap(), tableAliasGenerator);
        }

        return new SqlUpdate(fromTable, columns, optionalCondition.orElse(null));
    }

    public List<QueryParameter> createUpdateParameters(Query query) {
        CriteriaUpdate<?> criteriaUpdate = ((UpdateQuery) query).getCriteriaUpdate();
        List<QueryParameter> parameters = new ArrayList<>();
        Map<Path<?>, Object> setValues = ((MiniCriteriaUpdate<?>) criteriaUpdate).getSetValues();
        setValues.forEach((k, v) -> {
            AttributePath<?> miniPath = (AttributePath<?>) k;
            QueryParameter qp = createQueryParameter(miniPath, v);
            parameters.add(qp);
        });

        return parameters;
    }

    public StatementParameters delete(Query query, AliasGenerator tableAliasGenerator) {
        CriteriaDelete<?> criteriaDelete = ((DeleteQuery) query).getCriteriaDelete();
        MetaEntity entity = ((MiniRoot<?>) criteriaDelete.getRoot()).getMetaEntity();

        FromTable fromTable = FromTable.of(entity.getTableName(),
                tableAliasGenerator.getDefault(entity.getTableName()));
        List<QueryParameter> parameters = new ArrayList<>();

        Predicate restriction = criteriaDelete.getRestriction();
        Optional<Condition> optionalCondition = Optional.empty();
        if (restriction != null) {
            optionalCondition = createConditions(restriction, parameters, ((AbstractQuery) query).getParameterMap(), tableAliasGenerator);
        }

        SqlDelete sqlDelete = new SqlDelete(fromTable, optionalCondition.orElse(null));
        return new StatementParameters(sqlDelete, parameters);
    }

}
