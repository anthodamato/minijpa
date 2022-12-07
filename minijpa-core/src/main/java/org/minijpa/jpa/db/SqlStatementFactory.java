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

import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.minijpa.jdbc.BasicFetchParameter;
import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.JdbcTypes;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.db.SqlSelectData;
import org.minijpa.jdbc.db.SqlSelectDataBuilder;
import org.minijpa.jdbc.mapper.AbstractDbTypeMapper;
import org.minijpa.jpa.DeleteQuery;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.MiniTypedQuery;
import org.minijpa.jpa.UpdateQuery;
import org.minijpa.jpa.criteria.AggregateFunctionExpression;
import org.minijpa.jpa.criteria.AttributePath;
import org.minijpa.jpa.criteria.BinaryExpression;
import org.minijpa.jpa.criteria.ExpressionOperator;
import org.minijpa.jpa.criteria.MiniCriteriaUpdate;
import org.minijpa.jpa.criteria.MiniRoot;
import org.minijpa.jpa.criteria.TypecastExpression;
import org.minijpa.jpa.criteria.UnaryExpression;
import org.minijpa.jpa.criteria.predicate.BetweenExpressionsPredicate;
import org.minijpa.jpa.criteria.predicate.BetweenValuesPredicate;
import org.minijpa.jpa.criteria.predicate.BinaryBooleanExprPredicate;
import org.minijpa.jpa.criteria.predicate.BooleanExprPredicate;
import org.minijpa.jpa.criteria.predicate.ComparisonPredicate;
import org.minijpa.jpa.criteria.predicate.ExprPredicate;
import org.minijpa.jpa.criteria.predicate.InPredicate;
import org.minijpa.jpa.criteria.predicate.LikePredicate;
import org.minijpa.jpa.criteria.predicate.MultiplePredicate;
import org.minijpa.jpa.criteria.predicate.PredicateType;
import org.minijpa.jpa.criteria.predicate.PredicateTypeInfo;
import org.minijpa.jpa.jpql.AggregateFunctionType;
import org.minijpa.jpa.jpql.FunctionUtils;
import org.minijpa.jpa.model.AbstractAttribute;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.Pk;
import org.minijpa.jpa.model.relationship.JoinColumnAttribute;
import org.minijpa.jpa.model.relationship.Relationship;
import org.minijpa.jpa.model.relationship.RelationshipJoinTable;
import org.minijpa.metadata.AliasGenerator;
import org.minijpa.sql.model.Column;
import org.minijpa.sql.model.ForUpdate;
import org.minijpa.sql.model.FromTable;
import org.minijpa.sql.model.FromTableImpl;
import org.minijpa.sql.model.OrderBy;
import org.minijpa.sql.model.OrderByType;
import org.minijpa.sql.model.SqlDelete;
import org.minijpa.sql.model.SqlInsert;
import org.minijpa.sql.model.SqlSelect;
import org.minijpa.sql.model.SqlUpdate;
import org.minijpa.sql.model.TableColumn;
import org.minijpa.sql.model.Value;
import org.minijpa.sql.model.condition.BetweenCondition;
import org.minijpa.sql.model.condition.BinaryCondition;
import org.minijpa.sql.model.condition.BinaryLogicConditionImpl;
import org.minijpa.sql.model.condition.Condition;
import org.minijpa.sql.model.condition.ConditionType;
import org.minijpa.sql.model.condition.EmptyCondition;
import org.minijpa.sql.model.condition.InCondition;
import org.minijpa.sql.model.condition.LikeCondition;
import org.minijpa.sql.model.condition.UnaryCondition;
import org.minijpa.sql.model.condition.UnaryLogicConditionImpl;
import org.minijpa.sql.model.expression.SqlBinaryExpression;
import org.minijpa.sql.model.expression.SqlBinaryExpressionBuilder;
import org.minijpa.sql.model.expression.SqlExpressionOperator;
import org.minijpa.sql.model.function.Count;
import org.minijpa.sql.model.function.Sqrt;
import org.minijpa.sql.model.join.FromJoin;
import org.minijpa.sql.model.join.FromJoinImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlStatementFactory extends JdbcSqlStatementFactory {

    public static final String QM = "?";

    private final Logger LOG = LoggerFactory.getLogger(SqlStatementFactory.class);

    public SqlStatementFactory() {
    }

    public SqlInsert generateInsert(MetaEntity entity, List<String> columns, boolean hasIdentityColumn,
            boolean identityColumnNull, Optional<MetaEntity> metaEntity, AliasGenerator tableAliasGenerator)
            throws Exception {
        List<Column> cs = columns.stream().map(c -> {
            return new Column(c);
        }).collect(Collectors.toList());

        return new SqlInsert(FromTable.of(entity.getTableName(), tableAliasGenerator.getDefault(entity.getTableName())),
                cs, hasIdentityColumn, identityColumnNull,
                metaEntity.isPresent() ? Optional.of(metaEntity.get().getId().getAttribute().getColumnName())
                        : Optional.empty());
    }

    private Optional<ForUpdate> calcForUpdate(LockType lockType) {
        if (lockType == null)
            return Optional.empty();

        if (lockType == LockType.PESSIMISTIC_WRITE)
            return Optional.of(new ForUpdate());

        return Optional.empty();
    }

    public SqlSelectData generateSelectById(MetaEntity entity, LockType lockType, AliasGenerator tableAliasGenerator)
            throws Exception {
        List<FetchParameter> fetchParameters = MetaEntityHelper.convertAllAttributes(entity);
        FromTable fromTable = FromTable.of(entity.getTableName(),
                tableAliasGenerator.getDefault(entity.getTableName()));
        List<TableColumn> tableColumns = MetaEntityHelper.toValues(entity.getId().getAttributes(), fromTable);
        List<Condition> conditions = tableColumns.stream().map(t -> {
            return new BinaryCondition.Builder(ConditionType.EQUAL).withLeft(t).withRight(QM).build();
        }).collect(Collectors.toList());

        Condition condition = Condition.toAnd(conditions);
        SqlSelectDataBuilder sqlSelectBuilder = new SqlSelectDataBuilder();
        sqlSelectBuilder.withFromTable(fromTable);
        if (lockType != null)
            sqlSelectBuilder.withForUpdate(calcForUpdate(lockType));

        sqlSelectBuilder.withValues(MetaEntityHelper.toValues(entity, fromTable)).withConditions(List.of(condition));
        sqlSelectBuilder.withFetchParameters(fetchParameters);
        return (SqlSelectData) sqlSelectBuilder.build();
    }

    public SqlSelectData generateSelectVersion(MetaEntity entity, LockType lockType, AliasGenerator tableAliasGenerator)
            throws Exception {
        FetchParameter fetchParameter = MetaEntityHelper.toFetchParameter(entity.getVersionAttribute().get());

        FromTable fromTable = FromTable.of(entity.getTableName(),
                tableAliasGenerator.getDefault(entity.getTableName()));
        List<TableColumn> tableColumns = MetaEntityHelper.toValues(entity.getId().getAttributes(), fromTable);
        List<Condition> conditions = tableColumns.stream().map(t -> {
            return new BinaryCondition.Builder(ConditionType.EQUAL).withLeft(t).withRight(QM).build();
        }).collect(Collectors.toList());

        Condition condition = Condition.toAnd(conditions);
        SqlSelectDataBuilder sqlSelectBuilder = new SqlSelectDataBuilder();
        sqlSelectBuilder.withFromTable(fromTable);
        if (lockType != null)
            sqlSelectBuilder.withForUpdate(calcForUpdate(lockType));

        sqlSelectBuilder.withValues(List.of(MetaEntityHelper.toValue(entity.getVersionAttribute().get(), fromTable)))
                .withConditions(List.of(condition));
        sqlSelectBuilder.withFetchParameters(List.of(fetchParameter));
        return (SqlSelectData) sqlSelectBuilder.build();
    }

    public SqlSelectData generateSelectByForeignKey(MetaEntity entity, MetaAttribute foreignKeyAttribute,
            List<String> columns, AliasGenerator tableAliasGenerator) throws Exception {
        List<FetchParameter> fetchColumnNameValues = MetaEntityHelper.convertAllAttributes(entity);
        // LOG.info("generateSelectByForeignKey: fetchColumnNameValues=" +
        // fetchColumnNameValues);
        // LOG.info("generateSelectByForeignKey: parameters=" + parameters);
        FromTable fromTable = FromTable.of(entity.getTableName(),
                tableAliasGenerator.getDefault(entity.getTableName()));
        List<TableColumn> tableColumns = columns.stream().map(c -> new TableColumn(fromTable, new Column(c)))
                .collect(Collectors.toList());
        List<Condition> conditions = tableColumns.stream().map(t -> {
            return new BinaryCondition.Builder(ConditionType.EQUAL).withLeft(t).withRight(QM).build();
        }).collect(Collectors.toList());

        Condition condition = Condition.toAnd(conditions);
        SqlSelectDataBuilder sqlSelectBuilder = new SqlSelectDataBuilder();
        sqlSelectBuilder.withFromTable(fromTable);
        sqlSelectBuilder.withValues(MetaEntityHelper.toValues(entity, fromTable))
                .withConditions(Arrays.asList(condition)).withResult(fromTable);
        sqlSelectBuilder.withFetchParameters(fetchColumnNameValues);
        return (SqlSelectData) sqlSelectBuilder.build();
    }

    public ModelValueArray<AbstractAttribute> expandJoinColumnAttributes(Pk owningId, Object joinTableForeignKey,
            List<JoinColumnAttribute> allJoinColumnAttributes) throws Exception {
        ModelValueArray<MetaAttribute> modelValueArray = new ModelValueArray<>();
        LOG.debug("expandJoinColumnAttributes: owningId={}", owningId);
        MetaEntityHelper.expand(owningId, joinTableForeignKey, modelValueArray);

        LOG.debug("expandJoinColumnAttributes: modelValueArray.size()={}", modelValueArray.size());
        allJoinColumnAttributes.forEach(a -> LOG.debug("expandJoinColumnAttributes: a.getForeignKeyAttribute()={}",
                a.getForeignKeyAttribute()));
        ModelValueArray<AbstractAttribute> result = new ModelValueArray<>();
        for (int i = 0; i < modelValueArray.size(); ++i) {
            MetaAttribute attribute = modelValueArray.getModel(i);
            LOG.debug("expandJoinColumnAttributes: attribute={}", attribute);
            Optional<JoinColumnAttribute> optional = allJoinColumnAttributes.stream()
                    .filter(j -> j.getForeignKeyAttribute() == attribute).findFirst();
            LOG.debug("expandJoinColumnAttributes: optional.isPresent()={}", optional.isPresent());
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
    public List<FromJoin> calculateJoins(MetaEntity entity, MetaAttribute metaAttribute,
            AliasGenerator aliasGenerator) {
        if (metaAttribute.getRelationship().getJoinTable() != null) {
            List<MetaAttribute> idSourceAttributes = entity.getId().getAttributes();
            List<Column> idSourceColumns = idSourceAttributes.stream().map(a -> {
                return new Column(a.getColumnName());
            }).collect(Collectors.toList());

            RelationshipJoinTable relationshipJoinTable = metaAttribute.getRelationship().getJoinTable();
            List<Column> idOwningColumns = relationshipJoinTable.getOwningJoinColumnMapping().getJoinColumnAttributes()
                    .stream().map(a -> {
                        return new Column(a.getColumnName());
                    }).collect(Collectors.toList());

            String tableAlias = aliasGenerator.getDefault(relationshipJoinTable.getTableName());
            FromTable joinTable = new FromTableImpl(relationshipJoinTable.getTableName(), tableAlias);
            tableAlias = aliasGenerator.getDefault(entity.getTableName());
            FromJoin fromJoin = new FromJoinImpl(joinTable, tableAlias, idSourceColumns, idOwningColumns);

            MetaEntity destEntity = relationshipJoinTable.getTargetEntity();
            List<MetaAttribute> idTargetAttributes = destEntity.getId().getAttributes();
            List<Column> idDestColumns = idTargetAttributes.stream().map(a -> {
                return new Column(a.getColumnName());
            }).collect(Collectors.toList());

            List<Column> idTargetColumns = relationshipJoinTable.getTargetJoinColumnMapping().getJoinColumnAttributes()
                    .stream().map(a -> {
                        return new Column(a.getColumnName());
                    }).collect(Collectors.toList());
            tableAlias = aliasGenerator.getDefault(destEntity.getTableName());
            FromTable joinTable2 = new FromTableImpl(destEntity.getTableName(), tableAlias);
            FromJoin fromJoin2 = new FromJoinImpl(joinTable2,
                    aliasGenerator.getDefault(relationshipJoinTable.getTableName()), idTargetColumns, idDestColumns);

            return Arrays.asList(fromJoin, fromJoin2);
        } else if (metaAttribute.getRelationship().getJoinColumnMapping().isPresent()) {
            // List<MetaAttribute> idSourceAttributes = entity.getId().getAttributes();
            List<JoinColumnAttribute> joinColumnAttributes = metaAttribute.getRelationship().getJoinColumnMapping()
                    .get().getJoinColumnAttributes();
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
            FromJoin fromJoin = new FromJoinImpl(joinTable, tableAlias, idSourceColumns, idDestColumns);

            return List.of(fromJoin);
        }

        return null;
    }

    public FromJoin calculateFromTableByJoinTable(MetaEntity entity, RelationshipJoinTable relationshipJoinTable,
            AliasGenerator tableAliasGenerator) {
        List<MetaAttribute> idAttributes = entity.getId().getAttributes();
        List<Column> idColumns = idAttributes.stream().map(a -> {
            return new Column(a.getColumnName());
        }).collect(Collectors.toList());

        List<Column> idTargetColumns = relationshipJoinTable.getTargetJoinColumnMapping().getJoinColumnAttributes()
                .stream().map(a -> {
                    return new Column(a.getColumnName());
                }).collect(Collectors.toList());

        FromTable joinTable = new FromTableImpl(relationshipJoinTable.getTableName(),
                tableAliasGenerator.getDefault(relationshipJoinTable.getTableName()));
        String tableAlias = tableAliasGenerator.getDefault(entity.getTableName());
        return new FromJoinImpl(joinTable, tableAlias, idColumns, idTargetColumns);
    }

    public Condition generateJoinCondition(Relationship relationship, MetaEntity owningEntity, MetaEntity targetEntity,
            AliasGenerator tableAliasGenerator) {
        if (relationship.getJoinTable() != null) {
            RelationshipJoinTable relationshipJoinTable = relationship.getJoinTable();
            Pk pk = relationshipJoinTable.getOwningAttribute();
            pk.getAttributes().forEach(a -> LOG
                    .debug("generateJoinCondition: relationshipJoinTable.getOwningAttribute()={}", a.getColumnName()));
            relationshipJoinTable.getTargetAttribute().getAttributes().forEach(a -> LOG
                    .debug("generateJoinCondition: relationshipJoinTable.getTargetAttribute()={}", a.getColumnName()));
            relationshipJoinTable.getOwningJoinColumnMapping().getJoinColumnAttributes()
                    .forEach(a -> LOG.debug("generateJoinCondition: getOwningJoinColumnMapping a.getColumnName()={}",
                            a.getColumnName()));
            relationshipJoinTable.getOwningJoinColumnMapping().getJoinColumnAttributes()
                    .forEach(a -> LOG.debug(
                            "generateJoinCondition: getOwningJoinColumnMapping a.getAttribute().getColumnName()={}",
                            a.getAttribute().getColumnName()));
            relationshipJoinTable.getTargetJoinColumnMapping().getJoinColumnAttributes()
                    .forEach(a -> LOG.debug("generateJoinCondition: getTargetJoinColumnMapping a.getColumnName()={}",
                            a.getColumnName()));
            relationshipJoinTable.getTargetJoinColumnMapping().getJoinColumnAttributes()
                    .forEach(a -> LOG.debug(
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
                MetaAttribute ak = owningEntity.getId().getAttributes().get(i);
                TableColumn owningEntityTableColumn = new TableColumn(fromTable, new Column(ak.getColumnName()));
                Condition condition = new BinaryCondition.Builder(ConditionType.EQUAL).withLeft(owningTableColumn)
                        .withRight(owningEntityTableColumn).build();
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
                MetaAttribute ak = targetEntity.getId().getAttributes().get(i);
                TableColumn targetEntityTableColumn = new TableColumn(targetTable, new Column(ak.getColumnName()));
                Condition condition = new BinaryCondition.Builder(ConditionType.EQUAL).withLeft(owningTableColumn)
                        .withRight(targetEntityTableColumn).build();
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
     * @throws Exception
     */
    public SqlSelectData generateSelectByJoinTable(MetaEntity entity, RelationshipJoinTable relationshipJoinTable,
            List<AbstractAttribute> attributes, AliasGenerator aliasGenerator) throws Exception {
        // select t1.id, t1.p1 from entity t1 inner join jointable j on t1.id=j.id1
        // where j.t2=fk
        FromTable joinTable = new FromTableImpl(relationshipJoinTable.getTableName(),
                aliasGenerator.getDefault(relationshipJoinTable.getTableName()));
        FromJoin fromJoin = calculateFromTableByJoinTable(entity, relationshipJoinTable, aliasGenerator);
        FromTable fromTable = FromTable.of(entity.getTableName(), aliasGenerator.getDefault(entity.getTableName()));
        // handles multiple column pk

        List<TableColumn> tableColumns = MetaEntityHelper.attributesToTableColumns(attributes, joinTable);
        List<Condition> conditions = tableColumns.stream().map(t -> {
            return new BinaryCondition.Builder(ConditionType.EQUAL).withLeft(t).withRight(QM).build();
        }).collect(Collectors.toList());

        Condition condition = Condition.toAnd(conditions);
        List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
        List<FetchParameter> fetchColumnNameValues = MetaEntityHelper.convertAttributes(expandedAttributes);
        SqlSelectDataBuilder sqlSelectBuilder = new SqlSelectDataBuilder();
        sqlSelectBuilder.withFromTable(fromTable);
        sqlSelectBuilder.withJoin(fromJoin).withValues(MetaEntityHelper.toValues(entity, fromTable))
                .withConditions(Arrays.asList(condition)).withResult(fromTable);
        sqlSelectBuilder.withFetchParameters(fetchColumnNameValues);
        return (SqlSelectData) sqlSelectBuilder.build();
    }

    public SqlSelectData generateSelectByJoinTableFromTarget(MetaEntity entity,
            RelationshipJoinTable relationshipJoinTable, List<AbstractAttribute> attributes,
            AliasGenerator tableAliasGenerator) throws Exception {
        // select t1.id, t1.p1 from entity t1 inner join jointable j on t1.id=j.id1
        // where j.t2=fk
        List<MetaAttribute> idAttributes = entity.getId().getAttributes();
        List<Column> idColumns = idAttributes.stream().map(a -> {
            return new Column(a.getColumnName());
        }).collect(Collectors.toList());

        List<Column> idTargetColumns = relationshipJoinTable.getOwningJoinColumnMapping().getJoinColumnAttributes()
                .stream().map(a -> {
                    return new Column(a.getColumnName());
                }).collect(Collectors.toList());

        FromTable joinTable = new FromTableImpl(relationshipJoinTable.getTableName(),
                tableAliasGenerator.getDefault(relationshipJoinTable.getTableName()));
        String tableAlias = tableAliasGenerator.getDefault(entity.getTableName());
        FromJoin fromJoin = new FromJoinImpl(joinTable, tableAlias, idColumns, idTargetColumns);
        FromTable fromTable = FromTable.of(entity.getTableName(),
                tableAliasGenerator.getDefault(entity.getTableName()));
        // handles multiple column pk

        List<TableColumn> tableColumns = MetaEntityHelper.attributesToTableColumns(attributes, joinTable);

        List<Condition> conditions = tableColumns.stream().map(t -> {
            return new BinaryCondition.Builder(ConditionType.EQUAL).withLeft(t).withRight(QM).build();
        }).collect(Collectors.toList());

        Condition condition = Condition.toAnd(conditions);
        List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
        List<FetchParameter> fetchColumnNameValues = MetaEntityHelper.convertAttributes(expandedAttributes);
        SqlSelectDataBuilder sqlSelectBuilder = new SqlSelectDataBuilder();
        sqlSelectBuilder.withFromTable(fromTable);
        sqlSelectBuilder.withJoin(fromJoin).withValues(MetaEntityHelper.toValues(entity, fromTable))
                .withConditions(Arrays.asList(condition)).withResult(fromTable).build();
        sqlSelectBuilder.withFetchParameters(fetchColumnNameValues);
        return (SqlSelectData) sqlSelectBuilder.build();
    }

    public List<QueryParameter> createRelationshipJoinTableParameters(RelationshipJoinTable relationshipJoinTable,
            Object owningInstance, Object targetInstance) throws Exception {
        List<QueryParameter> parameters = new ArrayList<>();
        Pk owningId = relationshipJoinTable.getOwningAttribute();
        relationshipJoinTable.getOwningJoinColumnMapping().getJoinColumnAttributes()
                .forEach(a -> LOG.debug("createRelationshipJoinTableParameters: a={}", a));
        parameters.addAll(MetaEntityHelper.createJoinColumnAVSToQP(
                relationshipJoinTable.getOwningJoinColumnMapping().getJoinColumnAttributes(), owningId,
                AttributeUtil.getIdValue(owningId, owningInstance)));

        Pk targetId = relationshipJoinTable.getTargetAttribute();
        parameters.addAll(MetaEntityHelper.createJoinColumnAVSToQP(
                relationshipJoinTable.getTargetJoinColumnMapping().getJoinColumnAttributes(), targetId,
                AttributeUtil.getIdValue(targetId, targetInstance)));
        return parameters;
    }

    public SqlInsert generateJoinTableInsert(RelationshipJoinTable relationshipJoinTable, List<String> columnNames)
            throws Exception {
        List<Column> columns = columnNames.stream().map(c -> new Column(c)).collect(Collectors.toList());
        return new SqlInsert(new FromTableImpl(relationshipJoinTable.getTableName()), columns, false, false,
                Optional.empty());
    }

    public SqlUpdate generateUpdate(MetaEntity entity, List<String> columns, List<String> idColumnNames,
            AliasGenerator tableAliasGenerator) throws Exception {
        FromTable fromTable = FromTable.of(entity.getTableName(),
                tableAliasGenerator.getDefault(entity.getTableName()));
        List<TableColumn> cs = columns.stream().map(c -> {
            return new TableColumn(fromTable, new Column(c));
        }).collect(Collectors.toList());

        Condition condition = createAttributeEqualCondition(fromTable, idColumnNames);
        return new SqlUpdate(fromTable, cs, Optional.of(condition));
    }

    public SqlDelete generateDeleteById(MetaEntity entity, List<String> idColumnNames,
            AliasGenerator tableAliasGenerator) throws Exception {
        FromTable fromTable = FromTable.of(entity.getTableName(),
                tableAliasGenerator.getDefault(entity.getTableName()));
        Condition condition = createAttributeEqualCondition(fromTable, idColumnNames);
        return new SqlDelete(fromTable, Optional.of(condition));
    }

    public SqlDelete generateDeleteById(FromTable fromTable, List<String> idColumnNames) throws Exception {
        Condition condition = createAttributeEqualCondition(fromTable, idColumnNames);
        return new SqlDelete(fromTable, Optional.of(condition));
    }

    private Condition createAttributeEqualCondition(FromTable fromTable, List<String> columns) throws Exception {
        if (columns.size() == 1)
            return new BinaryCondition.Builder(ConditionType.EQUAL)
                    .withLeft(new TableColumn(fromTable, new Column(columns.get(0)))).withRight(QM).build();

        List<Condition> conditions = new ArrayList<>();
        for (String columnName : columns) {
            BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                    .withLeft(new TableColumn(fromTable, new Column(columnName))).withRight(QM).build();
            conditions.add(binaryCondition);
        }

        return new BinaryLogicConditionImpl(ConditionType.AND, conditions);
    }

    private Optional<Value> createSelectionValue(FromTable fromTable, Selection<?> selection,
            AliasGenerator aliasGenerator) {
        if (selection == null)
            return Optional.empty();

        if (selection instanceof TypecastExpression)
            return createSelectionValue(fromTable, ((TypecastExpression) selection).getExpression(), aliasGenerator);

        LOG.debug("createSelectionValue: selection={}", selection);
        if (selection instanceof AttributePath<?>) {
            AttributePath<?> miniPath = (AttributePath<?>) selection;
            MetaAttribute metaAttribute = miniPath.getMetaAttribute();
            return Optional.of(new TableColumn(fromTable, new Column(metaAttribute.getColumnName())));
        } else if (selection instanceof AggregateFunctionExpression<?>) {
            AggregateFunctionExpression<?> aggregateFunctionExpression = (AggregateFunctionExpression<?>) selection;
            Expression<?> expr = aggregateFunctionExpression.getX();
            if (aggregateFunctionExpression
                    .getAggregateFunctionType() == org.minijpa.jpa.criteria.AggregateFunctionType.COUNT) {
                if (expr instanceof AttributePath<?>) {
                    AttributePath<?> miniPath = (AttributePath<?>) expr;
                    MetaAttribute metaAttribute = miniPath.getMetaAttribute();
                    return Optional.of(new Count(new TableColumn(fromTable, new Column(metaAttribute.getColumnName())),
                            aggregateFunctionExpression.isDistinct()));
                } else if (expr instanceof MiniRoot<?>) {
                    MiniRoot<?> miniRoot = (MiniRoot<?>) expr;
                    MetaEntity metaEntity = miniRoot.getMetaEntity();
                    List<MetaAttribute> idAttrs = metaEntity.getId().getAttributes();
                    return Optional
                            .of(new Count(
                                    new TableColumn(
                                            FromTable.of(metaEntity.getTableName(),
                                                    aliasGenerator.getDefault(metaEntity.getTableName())),
                                            new Column(idAttrs.get(0).getColumnName())),
                                    aggregateFunctionExpression.isDistinct()));
                }
            } else if (expr instanceof AttributePath<?>) {
                AttributePath<?> miniPath = (AttributePath<?>) expr;
                MetaAttribute metaAttribute = miniPath.getMetaAttribute();
                Value value = FunctionUtils.createAggregateFunction(
                        getAggregateFunction(aggregateFunctionExpression.getAggregateFunctionType()),
                        new TableColumn(fromTable, new Column(metaAttribute.getColumnName())), false);
                return Optional.of(value);
            }
        } else if (selection instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) selection;
            SqlBinaryExpressionBuilder builder = new SqlBinaryExpressionBuilder(
                    getSqlExpressionOperator(binaryExpression.getExpressionOperator()));
            if (binaryExpression.getX().isPresent()) {
                AttributePath<?> miniPath = (AttributePath<?>) binaryExpression.getX().get();
                MetaAttribute metaAttribute = miniPath.getMetaAttribute();
                builder.setLeftExpression(new TableColumn(
                        FromTable.of(miniPath.getMetaEntity().getTableName(),
                                aliasGenerator.getDefault(miniPath.getMetaEntity().getTableName())),
                        new Column(metaAttribute.getColumnName())));
            }

            if (binaryExpression.getxValue().isPresent())
                if (requireQM(binaryExpression.getxValue().get()))
                    builder.setLeftExpression(QM);
                else
                    builder.setLeftExpression(buildValue(binaryExpression.getxValue().get()));

            if (binaryExpression.getY().isPresent()) {
                AttributePath<?> miniPath = (AttributePath<?>) binaryExpression.getY().get();
                MetaAttribute metaAttribute = miniPath.getMetaAttribute();
                builder.setRightExpression(new TableColumn(
                        FromTable.of(miniPath.getMetaEntity().getTableName(),
                                aliasGenerator.getDefault(miniPath.getMetaEntity().getTableName())),
                        new Column(metaAttribute.getColumnName())));
            }

            if (binaryExpression.getyValue().isPresent())
                if (requireQM(binaryExpression.getyValue().get()))
                    builder.setRightExpression(QM);
                else
                    builder.setRightExpression(buildValue(binaryExpression.getyValue().get()));

            SqlBinaryExpression sqlBinaryExpression = builder.build();
            LOG.debug("createSelectionValue: sqlBinaryExpression={}", sqlBinaryExpression);
            return Optional.of((Value) sqlBinaryExpression);
        } else if (selection instanceof UnaryExpression) {
            UnaryExpression<?> unaryExpression = (UnaryExpression<?>) selection;
            if (unaryExpression.getExpressionOperator() == ExpressionOperator.SQRT) {
                Optional optional = createSelectionValue(fromTable, unaryExpression.getExpression(), aliasGenerator);
                return Optional.of(new Sqrt(optional.get()));
            }
        }

        return Optional.empty();
    }

    private List<Value> createSelectionValues(FromTable fromTable, Selection<?> selection,
            AliasGenerator tableAliasGenerator) {
        if (selection == null)
            return Collections.emptyList();

        List<Value> values = new ArrayList<>();
        if (selection.isCompoundSelection()) {
            List<Selection<?>> selections = selection.getCompoundSelectionItems();
            for (Selection<?> s : selections) {
                Optional<Value> optional = createSelectionValue(fromTable, s, tableAliasGenerator);
                if (optional.isPresent())
                    values.add(optional.get());
            }
        } else {
            Optional<Value> optional = createSelectionValue(fromTable, selection, tableAliasGenerator);
            if (optional.isPresent())
                values.add(optional.get());
        }

        return values;
    }

    private Optional<FetchParameter> createFetchParameter(Selection<?> selection, Class<?> resultClass) {
        if (selection == null)
            return Optional.empty();

        if (selection instanceof AttributePath<?>) {
            AttributePath<?> miniPath = (AttributePath<?>) selection;
            MetaAttribute metaAttribute = miniPath.getMetaAttribute();
            FetchParameter columnNameValue = AttributeFetchParameter.build(metaAttribute);
            return Optional.of(columnNameValue);
        } else if (selection instanceof AggregateFunctionExpression<?>) {
            AggregateFunctionExpression<?> aggregateFunctionExpression = (AggregateFunctionExpression<?>) selection;
            if (aggregateFunctionExpression
                    .getAggregateFunctionType() == org.minijpa.jpa.criteria.AggregateFunctionType.COUNT) {
                // AttributeMapper attributeMapper =
                // dbConfiguration.getDbTypeMapper().aggregateFunctionMapper(Count.class);
                FetchParameter cnv = new BasicFetchParameter("count", null, Optional.empty());
                return Optional.of(cnv);
            } else if (aggregateFunctionExpression
                    .getAggregateFunctionType() == org.minijpa.jpa.criteria.AggregateFunctionType.SUM) {
                // AttributeMapper attributeMapper =
                // dbConfiguration.getDbTypeMapper().aggregateFunctionMapper(Sum.class);
                FetchParameter cnv = new BasicFetchParameter("sum", null, Optional.empty());
                return Optional.of(cnv);
            } else if (aggregateFunctionExpression
                    .getAggregateFunctionType() == org.minijpa.jpa.criteria.AggregateFunctionType.AVG) {
                // AttributeMapper attributeMapper =
                // dbConfiguration.getDbTypeMapper().aggregateFunctionMapper(Avg.class);
                FetchParameter cnv = new BasicFetchParameter("avg", null, Optional.empty());
                return Optional.of(cnv);
            } else {
                Expression<?> expr = aggregateFunctionExpression.getX();
                if (expr instanceof AttributePath<?>) {
                    AttributePath<?> miniPath = (AttributePath<?>) expr;
                    MetaAttribute metaAttribute = miniPath.getMetaAttribute();
                    FetchParameter columnNameValue = AttributeFetchParameter.build(metaAttribute);
                    return Optional.of(columnNameValue);
                }
            }
        } else if (selection instanceof BinaryExpression) {
            Integer sqlType = resultClass != null ? JdbcTypes.sqlTypeFromClass(resultClass) : null;
            BinaryExpression binaryExpression = (BinaryExpression) selection;
            if (binaryExpression.getX().isPresent()) {
                if (checkDataType((AttributePath<?>) binaryExpression.getX().get(), sqlType))
                    return Optional.of(new BasicFetchParameter("result", sqlType, Optional.empty()));
            }

            if (binaryExpression.getY().isPresent()) {
                if (checkDataType((AttributePath<?>) binaryExpression.getY().get(), sqlType))
                    return Optional.of(new BasicFetchParameter("result", sqlType, Optional.empty()));
            }

            if (binaryExpression.getxValue().isPresent()) {
                if (sqlType != null && sqlType != Types.NULL
                        && binaryExpression.getxValue().get().getClass() == resultClass)
                    return Optional.of(new BasicFetchParameter("result", sqlType, Optional.empty()));
            }

            if (binaryExpression.getyValue().isPresent()) {
                if (sqlType != null && sqlType != Types.NULL
                        && binaryExpression.getyValue().get().getClass() == resultClass)
                    return Optional.of(new BasicFetchParameter("result", sqlType, Optional.empty()));
            }

            sqlType = calculateSqlType(binaryExpression);
            return Optional.of(new BasicFetchParameter("result", sqlType, Optional.empty()));
        } else if (selection instanceof UnaryExpression<?>) {
            UnaryExpression<?> unaryExpression = (UnaryExpression<?>) selection;
            if (unaryExpression.getExpressionOperator() == ExpressionOperator.SQRT) {
                return Optional.of(new BasicFetchParameter("result", Types.DOUBLE, Optional.empty()));
            }
        } else if (selection instanceof TypecastExpression<?>) {
            ExpressionOperator expressionOperator = ((TypecastExpression<?>) selection).getExpressionOperator();
            if (expressionOperator == ExpressionOperator.TO_BIGDECIMAL)
                return Optional.of(new BasicFetchParameter("result", Types.DECIMAL, Optional.empty()));
            else if (expressionOperator == ExpressionOperator.TO_BIGINTEGER)
                return Optional.of(new BasicFetchParameter("result", Types.BIGINT,
                        Optional.of(AbstractDbTypeMapper.numberToBigIntegerAttributeMapper)));
            else if (expressionOperator == ExpressionOperator.TO_DOUBLE)
                return Optional.of(new BasicFetchParameter("result", Types.DOUBLE, Optional.empty()));
            else if (expressionOperator == ExpressionOperator.TO_FLOAT)
                return Optional.of(new BasicFetchParameter("result", Types.FLOAT, Optional.empty()));
            else if (expressionOperator == ExpressionOperator.TO_INTEGER)
                return Optional.of(new BasicFetchParameter("result", Types.INTEGER, Optional.empty()));
            else if (expressionOperator == ExpressionOperator.TO_LONG)
                return Optional.of(new BasicFetchParameter("result", Types.BIGINT, Optional.empty()));
        }

        return Optional.empty();
    }

    private Integer calculateSqlType(BinaryExpression binaryExpression) {
        LOG.debug("calculateSqlType: binaryExpression.getX().isPresent()={}", binaryExpression.getX().isPresent());
        if (binaryExpression.getX().isPresent()) {
            LOG.debug("calculateSqlType: binaryExpression.getX().get()={}", binaryExpression.getX().get());
            return calculateSqlTypeFromExpression((Expression<?>) binaryExpression.getX().get(),
                    binaryExpression.getY(), binaryExpression.getyValue());
        }

        Integer sqlType1 = JdbcTypes.sqlTypeFromClass(binaryExpression.getxValue().get().getClass());
        LOG.debug("calculateSqlType: binaryExpression.getxValue().get().getClass()={}",
                binaryExpression.getxValue().get().getClass());
        if (binaryExpression.getY().isPresent()) {
            AttributePath<?> attributePath2 = (AttributePath<?>) binaryExpression.getY().get();
            MetaAttribute metaAttribute2 = attributePath2.getMetaAttribute();
            Integer sqlType2 = metaAttribute2.getSqlType();
            int compare = JdbcTypes.compareNumericTypes(sqlType1, sqlType2);
            LOG.debug("calculateSqlType: sqlType1={}, sqlType2={}, compare={}", sqlType1, sqlType2, compare);
            return compare < 0 ? sqlType2 : sqlType1;
        }

        Integer sqlType2 = JdbcTypes.sqlTypeFromClass(binaryExpression.getyValue().get().getClass());
        int compare = JdbcTypes.compareNumericTypes(sqlType1, sqlType2);
        LOG.debug("calculateSqlType: sqlType1={}, sqlType2={}, compare={}", sqlType1, sqlType2, compare);
        return compare < 0 ? sqlType2 : sqlType1;
    }

    private Integer calculateSqlTypeFromExpression(Expression<?> expression, Optional<Expression<?>> optionalExpression,
            Optional<Object> optionalValue) {
        AttributePath<?> attributePath1 = (AttributePath<?>) expression;
        LOG.debug("calculateSqlTypeFromExpression: attributePath1={}", attributePath1);
        MetaAttribute metaAttribute1 = attributePath1.getMetaAttribute();
        Integer sqlType1 = metaAttribute1.getSqlType();
        LOG.debug("calculateSqlTypeFromExpression: sqlType1={}", sqlType1);
        if (optionalExpression.isPresent()) {
            AttributePath<?> attributePath2 = (AttributePath<?>) optionalExpression.get();
            MetaAttribute metaAttribute2 = attributePath2.getMetaAttribute();
            Integer sqlType2 = metaAttribute2.getSqlType();
            int compare = JdbcTypes.compareNumericTypes(sqlType1, sqlType2);
            LOG.debug("calculateSqlTypeFromExpression: sqlType1={}, sqlType2={}, compare={}", sqlType1, sqlType2,
                    compare);
            return compare < 0 ? sqlType2 : sqlType1;
        }

        Integer sqlType2 = JdbcTypes.sqlTypeFromClass(optionalValue.get().getClass());
        int compare = JdbcTypes.compareNumericTypes(sqlType1, sqlType2);
        LOG.debug("calculateSqlTypeFromExpression: value={}", optionalValue.get());
        LOG.debug("calculateSqlTypeFromExpression: sqlType1={}, sqlType2={}, compare={}", sqlType1, sqlType2, compare);
        return compare < 0 ? sqlType2 : sqlType1;
    }

    private boolean checkDataType(AttributePath<?> attributePath, Integer sqlType) {
        MetaAttribute metaAttribute = attributePath.getMetaAttribute();
        LOG.debug("checkDataType: sqlType={}, metaAttribute.getSqlType()={}", sqlType, metaAttribute.getSqlType());
        if (sqlType != null && sqlType != Types.NULL && sqlType == metaAttribute.getSqlType())
            return true;

        return false;
    }

    private List<FetchParameter> createFetchParameters(Selection<?> selection, Class<?> resultClass) {
        if (selection == null)
            return Collections.emptyList();

        List<FetchParameter> values = new ArrayList<>();
        if (selection.isCompoundSelection()) {
            List<Selection<?>> selections = selection.getCompoundSelectionItems();
            for (Selection<?> s : selections) {
                Optional<FetchParameter> optional = createFetchParameter(s, resultClass);
                if (optional.isPresent())
                    values.add(optional.get());
            }
        } else {
            Optional<FetchParameter> optional = createFetchParameter(selection, resultClass);
            if (optional.isPresent())
                values.add(optional.get());
        }

        return values;
    }

    private boolean requireQM(Object value) {
        if (value instanceof LocalDate)
            return true;

        return false;
    }

    private QueryParameter createQueryParameterForParameterExpression(ParameterExpression<?> parameterExpression,
            MetaAttribute attribute, Query query) {
        Object value = null;
        if (parameterExpression.getName() != null)
            value = query.getParameterValue(parameterExpression.getName());
        else if (parameterExpression.getPosition() != null)
            value = query.getParameter(parameterExpression.getPosition());

        return new QueryParameter(attribute.getColumnName(), value, attribute.getType(), attribute.getSqlType(),
                attribute.getAttributeMapper());
    }

    private QueryParameter createQueryParameterForParameterExpression(ParameterExpression<?> parameterExpression,
            String columnName, Class<?> type, Integer sqlType, Query query) {
        Object value = null;
        if (parameterExpression.getName() != null)
            value = query.getParameterValue(parameterExpression.getName());
        else if (parameterExpression.getPosition() != null)
            value = query.getParameter(parameterExpression.getPosition());

        return new QueryParameter(columnName, value, type, sqlType, Optional.empty());
    }

    private String buildValue(Object value) {
        StringBuilder sb = new StringBuilder();
        if (value instanceof String) {
            sb.append("'");
            sb.append((String) value);
            sb.append("'");
        } else
            sb.append(value.toString());

        return sb.toString();
    }

    private TableColumn createTableColumnFromPath(AttributePath<?> miniPath, AliasGenerator tableAliasGenerator) {
        MetaAttribute attribute1 = miniPath.getMetaAttribute();
        return new TableColumn(
                FromTable.of(miniPath.getMetaEntity().getTableName(),
                        tableAliasGenerator.getDefault(miniPath.getMetaEntity().getTableName())),
                new Column(attribute1.getColumnName()));
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

        throw new IllegalArgumentException("Unknown condition type for predicate type: " + predicateType);
    }

    private AggregateFunctionType getAggregateFunction(
            org.minijpa.jpa.criteria.AggregateFunctionType aggregateFunctionType) {
        switch (aggregateFunctionType) {
        case AVG:
            return AggregateFunctionType.AVG;
        case MAX:
            return AggregateFunctionType.MAX;
        case MIN:
            return AggregateFunctionType.MIN;
        case COUNT:
            return AggregateFunctionType.COUNT;
        case SUM:
            return AggregateFunctionType.SUM;
        default:
            break;
        }

        throw new IllegalArgumentException(
                "Unknown aggregate function type for predicate type: " + aggregateFunctionType);
    }

    private SqlExpressionOperator getSqlExpressionOperator(ExpressionOperator expressionOperator) {
        switch (expressionOperator) {
        case DIFF:
            return SqlExpressionOperator.DIFF;
        case MINUS:
            return SqlExpressionOperator.MINUS;
        case PROD:
            return SqlExpressionOperator.PROD;
        case QUOT:
            return SqlExpressionOperator.QUOT;
        case SUM:
            return SqlExpressionOperator.SUM;
        default:
            break;
        }

        throw new IllegalArgumentException("Unknown  operator for expression type: " + expressionOperator);
    }

    private Optional<Condition> translateComparisonPredicate(ComparisonPredicate comparisonPredicate,
            List<QueryParameter> parameters, Query query, AliasGenerator tableAliasGenerator) {
        Expression<?> expression1 = comparisonPredicate.getX();
        Expression<?> expression2 = comparisonPredicate.getY();

        ConditionType conditionType = getOperator(comparisonPredicate.getPredicateType());
        BinaryCondition.Builder builder = new BinaryCondition.Builder(conditionType);
        if (expression1 instanceof AttributePath<?>) {
            AttributePath<?> miniPath = (AttributePath<?>) expression1;
            MetaAttribute attribute1 = miniPath.getMetaAttribute();
            TableColumn tableColumn1 = createTableColumnFromPath(miniPath, tableAliasGenerator);
            if (expression2 != null) {
                if (expression2 instanceof AttributePath<?>) {
                    miniPath = (AttributePath<?>) expression2;
                    TableColumn tableColumn2 = createTableColumnFromPath(miniPath, tableAliasGenerator);
                    builder.withLeft(tableColumn1).withRight(tableColumn2);
                } else if (expression2 instanceof ParameterExpression<?>) {
                    ParameterExpression<?> parameterExpression = (ParameterExpression<?>) expression2;
                    parameters.add(createQueryParameterForParameterExpression(parameterExpression, attribute1, query));
//                    addParameter(parameterExpression, attribute1, parameters, query);
                    builder.withLeft(tableColumn1).withRight(QM);
                }
            } else if (comparisonPredicate.getValue() != null)
                if (requireQM(comparisonPredicate.getValue())) {
                    QueryParameter queryParameter = new QueryParameter(attribute1.getColumnName(),
                            comparisonPredicate.getValue(), attribute1.getType(), attribute1.getSqlType(),
                            attribute1.getAttributeMapper());
                    parameters.add(queryParameter);
                    builder.withLeft(tableColumn1).withRight(QM);
                } else
                    builder.withLeft(tableColumn1).withRight(buildValue(comparisonPredicate.getValue()));

            return Optional.of(builder.build());
        }
        // else if (expression1 instanceof ParameterExpression<?>) {
        // ParameterExpression<?> parameterExpression = (ParameterExpression<?>)
        // expression1;
        //// MiniPath<?> miniPath = (MiniPath<?>) expression1;
        //// MetaAttribute attribute1 = miniPath.getMetaAttribute();
        // addParameter(parameterExpression, attribute1, parameters, query);
        // if (expression2 instanceof MiniPath<?>) {
        // MiniPath<?> miniPath = (MiniPath<?>) expression2;
        //// MetaAttribute attribute2 = miniPath.getMetaAttribute();
        //// FromTable fromTable2 = FromTable.of(miniPath.getMetaEntity());
        //// Column column1 = new Column(attribute2.getColumnName(),
        // fromTable2.getAlias().get());
        //// TableColumn tableColumn = new TableColumn(fromTable2, column1);
        // TableColumn tableColumn = createTableColumnFromPath(miniPath);
        // return Optional.of(new EqualExprColumnCondition(QM, tableColumn));
        // } else if (expression2 instanceof ParameterExpression<?>) {
        // parameterExpression = (ParameterExpression<?>) expression2;
        // miniPath = (MiniPath<?>) expression2;
        // MetaAttribute attribute2 = miniPath.getMetaAttribute();
        // addParameter(parameterExpression, attribute2, parameters, query);
        // return Optional.of(new EqualExprExprCondition(QM, QM));
        // }
        // }

        return Optional.empty();
    }

    private Optional<Condition> translateBetweenExpressionsPredicate(
            BetweenExpressionsPredicate betweenExpressionsPredicate, List<QueryParameter> parameters, Query query,
            AliasGenerator tableAliasGenerator) {
        Expression<?> expression1 = betweenExpressionsPredicate.getX();
        Expression<?> expression2 = betweenExpressionsPredicate.getY();
        AttributePath<?> miniPath = (AttributePath<?>) betweenExpressionsPredicate.getV();
        MetaAttribute attribute = miniPath.getMetaAttribute();

        BetweenCondition.Builder builder = new BetweenCondition.Builder(
                createTableColumnFromPath(miniPath, tableAliasGenerator));
        if (expression1 instanceof AttributePath<?>)
            builder.withLeftExpression(createTableColumnFromPath((AttributePath<?>) expression1, tableAliasGenerator));
        else if (expression1 instanceof ParameterExpression<?>) {
            ParameterExpression<?> parameterExpression = (ParameterExpression<?>) expression1;
//            addParameter(parameterExpression, attribute, parameters, query);
            parameters.add(createQueryParameterForParameterExpression(parameterExpression, attribute, query));
            builder.withLeftExpression(QM);
        }

        if (expression2 instanceof AttributePath<?>)
            builder.withRightExpression(createTableColumnFromPath((AttributePath<?>) expression2, tableAliasGenerator));
        else if (expression2 instanceof ParameterExpression<?>) {
            ParameterExpression<?> parameterExpression = (ParameterExpression<?>) expression2;
//            addParameter(parameterExpression, attribute, parameters, query);
            parameters.add(createQueryParameterForParameterExpression(parameterExpression, attribute, query));
            builder.withLeftExpression(QM);
        }

        return Optional.of(builder.build());
    }

    private Optional<Condition> translateBetweenValuesPredicate(BetweenValuesPredicate betweenValuesPredicate,
            List<QueryParameter> parameters, AliasGenerator tableAliasGenerator) {
        Object x = betweenValuesPredicate.getX();
        Object y = betweenValuesPredicate.getY();
        AttributePath<?> miniPath = (AttributePath<?>) betweenValuesPredicate.getV();
        MetaAttribute attribute = miniPath.getMetaAttribute();

        BetweenCondition.Builder builder = new BetweenCondition.Builder(
                createTableColumnFromPath(miniPath, tableAliasGenerator));
        if (requireQM(x)) {
            QueryParameter queryParameter = new QueryParameter(attribute.getColumnName(), x, attribute.getType(),
                    attribute.getSqlType(), attribute.getAttributeMapper());
            parameters.add(queryParameter);
            builder.withLeftExpression(QM);
        } else
            builder.withLeftExpression(buildValue(x));

        if (requireQM(y)) {
            QueryParameter queryParameter = new QueryParameter(attribute.getColumnName(), y, attribute.getType(),
                    attribute.getSqlType(), attribute.getAttributeMapper());
            parameters.add(queryParameter);
            builder.withRightExpression(QM);
        } else
            builder.withRightExpression(buildValue(y));

        return Optional.of(builder.build());
    }

    private Optional<Condition> translateBooleanExprPredicate(BooleanExprPredicate booleanExprPredicate,
            List<QueryParameter> parameters, Query query, AliasGenerator tableAliasGenerator) {
        Expression<Boolean> x = booleanExprPredicate.getX();

        if (x instanceof Predicate) {
            Optional<Condition> optional = createConditions((Predicate) x, parameters, query, tableAliasGenerator);
            if (optional.isPresent())
                return Optional.of(new UnaryLogicConditionImpl(getOperator(booleanExprPredicate.getPredicateType()),
                        optional.get()));
        }

        if (x instanceof AttributePath<?>) {
            AttributePath<?> miniPath = (AttributePath<?>) x;
            return Optional.of(new UnaryCondition(getOperator(booleanExprPredicate.getPredicateType()),
                    createTableColumnFromPath(miniPath, tableAliasGenerator)));
        }

        return Optional.empty();
    }

    private Optional<Condition> translateExprPredicate(ExprPredicate exprPredicate, List<QueryParameter> parameters,
            Query query, AliasGenerator aliasGenerator) {
        Expression<?> x = exprPredicate.getX();

        if (x instanceof AttributePath<?>) {
            AttributePath<?> miniPath = (AttributePath<?>) x;
            return Optional.of(new UnaryCondition(getOperator(exprPredicate.getPredicateType()),
                    createTableColumnFromPath(miniPath, aliasGenerator)));
        }

        return Optional.empty();
    }

    private Optional<Condition> translateMultiplePredicate(MultiplePredicate multiplePredicate,
            List<QueryParameter> parameters, Query query, AliasGenerator tableAliasGenerator) {
        Predicate[] predicates = multiplePredicate.getRestrictions();
        List<Condition> conditions = new ArrayList<>();
        LOG.info("translateMultiplePredicate: conditions.size()={}", conditions.size());
        for (Predicate p : predicates) {
            Optional<Condition> optional = createConditions(p, parameters, query, tableAliasGenerator);
            if (optional.isPresent())
                conditions.add(optional.get());
        }

        if (!conditions.isEmpty())
            return Optional.of(
                    new BinaryLogicConditionImpl(getOperator(multiplePredicate.getPredicateType()), conditions, true));

        return Optional.empty();
    }

    private Optional<Condition> translateBinaryBooleanExprPredicate(
            BinaryBooleanExprPredicate binaryBooleanExprPredicate, List<QueryParameter> parameters, Query query,
            AliasGenerator tableAliasGenerator) {
        Expression<Boolean> x = binaryBooleanExprPredicate.getX();
        Expression<Boolean> y = binaryBooleanExprPredicate.getY();

        LOG.debug("translateBinaryBooleanExprPredicate: x={}", x);
        LOG.debug("translateBinaryBooleanExprPredicate: y={}", y);
        List<Condition> conditions = new ArrayList<>();
        if (x instanceof Predicate) {
            Optional<Condition> optional = createConditions((Predicate) x, parameters, query, tableAliasGenerator);
            optional.ifPresent(conditions::add);
        }

        if (y instanceof Predicate) {
            Optional<Condition> optional = createConditions((Predicate) y, parameters, query, tableAliasGenerator);
            optional.ifPresent(conditions::add);
        }

        if (!conditions.isEmpty())
            return Optional.of(new BinaryLogicConditionImpl(getOperator(binaryBooleanExprPredicate.getPredicateType()),
                    conditions, true));

        return Optional.empty();
    }

    private Optional<Condition> translateInPredicate(InPredicate<?> inPredicate, List<QueryParameter> parameters,
            Query query, AliasGenerator tableAliasGenerator) {
        Expression<?> expression = inPredicate.getExpression();
        LOG.info("translateInPredicate: expression={}", expression);
        TableColumn tableColumn = null;
        if (expression instanceof AttributePath<?>) {
            AttributePath<?> miniPath = (AttributePath<?>) expression;
            MetaAttribute attribute = miniPath.getMetaAttribute();
            tableColumn = createTableColumnFromPath(miniPath, tableAliasGenerator);

            List<String> list = inPredicate.getValues().stream().map(v -> {
                return QM;
            }).collect(Collectors.toList());

            List<QueryParameter> queryParameters = inPredicate.getValues().stream().map(v -> {
                return new QueryParameter(attribute.getColumnName(), v, attribute.getType(), attribute.getSqlType(),
                        attribute.getAttributeMapper());
            }).collect(Collectors.toList());
            parameters.addAll(queryParameters);

            return Optional.of(new InCondition(tableColumn, list, inPredicate.isNot()));
        }

        return Optional.empty();
    }

    private Optional<Condition> translateLikePredicate(LikePredicate likePredicate, List<QueryParameter> parameters,
            Query query, AliasGenerator aliasGenerator) {
        Optional<Expression<String>> patternExpression = likePredicate.getPatternExpression();
        Optional<String> pattern = likePredicate.getPattern();
        AttributePath<?> miniPath = (AttributePath<?>) likePredicate.getX();
        Object right = null;
        if (pattern.isPresent()) {
            right = buildValue(pattern.get());
        } else if (patternExpression.isPresent()) {
            if (patternExpression.get() instanceof ParameterExpression<?>) {
                ParameterExpression<?> parameterExpression = (ParameterExpression<?>) patternExpression.get();
                MetaAttribute attribute = miniPath.getMetaAttribute();
                parameters.add(createQueryParameterForParameterExpression(parameterExpression, attribute, query));
                right = QM;
            } else if (patternExpression.get() instanceof AttributePath<?>) {
                AttributePath<?> attributePath = (AttributePath<?>) patternExpression.get();
                right = createTableColumnFromPath(attributePath, aliasGenerator);
            }
        }

        String escapeChar = null;
        Optional<Character> escapeCharacter = likePredicate.getEscapeChar();
        Optional<Expression<Character>> escapeExpression = likePredicate.getEscapeCharEx();
        if (escapeCharacter.isPresent()) {
            escapeChar = buildValue("" + likePredicate.getEscapeChar().get());
        } else if (escapeExpression.isPresent()) {
            if (escapeExpression.get() instanceof ParameterExpression<?>) {
                ParameterExpression<?> parameterExpression = (ParameterExpression<?>) escapeExpression.get();

                // TODO: escape char not working on Derby. Replaced with string value
//                parameters.add(createQueryParameterForParameterExpression(parameterExpression, "escape",
//                        Character.class, Types.CHAR, query));
//                escapeChar = QM;

                Object value = null;
                if (parameterExpression.getName() != null)
                    value = query.getParameterValue(parameterExpression.getName());
                else if (parameterExpression.getPosition() != null)
                    value = query.getParameter(parameterExpression.getPosition());

                escapeChar = buildValue("" + String.valueOf((char) value));
            }
        }

        LikeCondition likeCondition = new LikeCondition(createTableColumnFromPath(miniPath, aliasGenerator), right,
                escapeChar, likePredicate.isNot());

        return Optional.of(likeCondition);
    }

    private Optional<Condition> createConditions(Predicate predicate, List<QueryParameter> parameters, Query query,
            AliasGenerator tableAliasGenerator) {
        PredicateTypeInfo predicateTypeInfo = (PredicateTypeInfo) predicate;
        PredicateType predicateType = predicateTypeInfo.getPredicateType();
        if (predicateType == PredicateType.EQUAL || predicateType == PredicateType.NOT_EQUAL
                || predicateType == PredicateType.GREATER_THAN
                || predicateType == PredicateType.GREATER_THAN_OR_EQUAL_TO || predicateType == PredicateType.GT
                || predicateType == PredicateType.LESS_THAN || predicateType == PredicateType.LESS_THAN_OR_EQUAL_TO
                || predicateType == PredicateType.LT)
            return translateComparisonPredicate((ComparisonPredicate) predicate, parameters, query,
                    tableAliasGenerator);
        else if (predicateType == PredicateType.BETWEEN_EXPRESSIONS) {
            BetweenExpressionsPredicate betweenExpressionsPredicate = (BetweenExpressionsPredicate) predicate;
            return translateBetweenExpressionsPredicate(betweenExpressionsPredicate, parameters, query,
                    tableAliasGenerator);
        } else if (predicateType == PredicateType.BETWEEN_VALUES) {
            BetweenValuesPredicate betweenValuesPredicate = (BetweenValuesPredicate) predicate;
            return translateBetweenValuesPredicate(betweenValuesPredicate, parameters, tableAliasGenerator);
        } else if (predicateType == PredicateType.LIKE_PATTERN) {
            LikePredicate likePredicate = (LikePredicate) predicate;
            return translateLikePredicate(likePredicate, parameters, query, tableAliasGenerator);
//        } else if (predicateType == PredicateType.LIKE_PATTERN_EXPR) {
//            LikePredicate likePatternExprPredicate = (LikePredicate) predicate;
//            return translateLikePredicate(likePatternExprPredicate, parameters, query, tableAliasGenerator);
        } else if (predicateType == PredicateType.OR || predicateType == PredicateType.AND)
            if (predicate instanceof MultiplePredicate)
                return translateMultiplePredicate((MultiplePredicate) predicate, parameters, query,
                        tableAliasGenerator);
            else
                return translateBinaryBooleanExprPredicate((BinaryBooleanExprPredicate) predicate, parameters, query,
                        tableAliasGenerator);
        else if (predicateType == PredicateType.NOT)
            return translateBooleanExprPredicate((BooleanExprPredicate) predicate, parameters, query,
                    tableAliasGenerator);
        else if (predicateType == PredicateType.IS_NULL || predicateType == PredicateType.IS_NOT_NULL)
            return translateExprPredicate((ExprPredicate) predicate, parameters, query, tableAliasGenerator);
        else if (predicateType == PredicateType.EQUALS_TRUE || predicateType == PredicateType.EQUALS_FALSE)
            return translateBooleanExprPredicate((BooleanExprPredicate) predicate, parameters, query,
                    tableAliasGenerator);
        else if (predicateType == PredicateType.EMPTY_CONJUNCTION || predicateType == PredicateType.EMPTY_DISJUNCTION)
            return Optional.of(new EmptyCondition(getOperator(predicateType)));
        else if (predicateType == PredicateType.IN)
            return translateInPredicate((InPredicate<?>) predicate, parameters, query, tableAliasGenerator);

        return Optional.empty();
    }

    private Optional<List<OrderBy>> createOrderByList(CriteriaQuery<?> criteriaQuery,
            AliasGenerator tableAliasGenerator) {
        if (criteriaQuery.getOrderList() == null || criteriaQuery.getOrderList().isEmpty())
            return Optional.empty();

        List<OrderBy> result = new ArrayList<>();
        List<Order> orders = criteriaQuery.getOrderList();
        for (Order order : orders) {
            Expression<?> expression = order.getExpression();
            if (expression instanceof AttributePath<?>) {
                AttributePath<?> miniPath = (AttributePath<?>) expression;
                MetaAttribute metaAttribute = miniPath.getMetaAttribute();
                TableColumn tableColumn = new TableColumn(
                        FromTable.of(miniPath.getMetaEntity().getTableName(),
                                tableAliasGenerator.getDefault(miniPath.getMetaEntity().getTableName())),
                        new Column(metaAttribute.getColumnName()));
                OrderByType orderByType = order.isAscending() ? OrderByType.ASC : OrderByType.DESC;
                OrderBy orderBy = new OrderBy(tableColumn, orderByType);
                result.add(orderBy);
            }
        }

        if (result.isEmpty())
            return Optional.empty();

        return Optional.of(result);
    }

    public StatementParameters select(Query query, AliasGenerator aliasGenerator) {
        CriteriaQuery<?> criteriaQuery = null;
        if (query instanceof MiniTypedQuery<?>)
            criteriaQuery = ((MiniTypedQuery<?>) query).getCriteriaQuery();

        Set<Root<?>> roots = criteriaQuery.getRoots();
        Root<?> root = roots.iterator().next();
        MetaEntity entity = ((MiniRoot<?>) root).getMetaEntity();

        Predicate restriction = criteriaQuery.getRestriction();
        List<QueryParameter> parameters = new ArrayList<>();
        Optional<Condition> optionalCondition = Optional.empty();
        if (restriction != null)
            optionalCondition = createConditions(restriction, parameters, query, aliasGenerator);

        List<Condition> conditions = null;
        if (optionalCondition.isPresent())
            conditions = List.of(optionalCondition.get());

        SqlSelectDataBuilder builder = new SqlSelectDataBuilder();
        FromTable fromTable = FromTable.of(entity.getTableName(), aliasGenerator.getDefault(entity.getTableName()));
        builder.withFromTable(fromTable);

        LockType lockType = LockTypeUtils.toLockType(query.getLockMode());
        if (lockType != null)
            builder.withForUpdate(calcForUpdate(lockType));

        Optional<List<OrderBy>> optionalOrderBy = createOrderByList(criteriaQuery, aliasGenerator);
        optionalOrderBy.ifPresent(builder::withOrderBy);

        if (criteriaQuery.isDistinct())
            builder.distinct();

        Selection<?> selection = criteriaQuery.getSelection();
        if (selection instanceof MiniRoot<?>) {
            List<FetchParameter> fetchParameters = MetaEntityHelper.convertAllAttributes(entity);
            LOG.debug("select: entity={}", entity);
            entity.getBasicAttributes().forEach(b -> LOG.debug("select: b={}", b));
            fetchParameters.forEach(
                    f -> LOG.debug("select: f.getAttribute()={}", ((AttributeFetchParameter) f).getAttribute()));

            builder.withValues(MetaEntityHelper.toValues(entity, fromTable)).withConditions(conditions)
                    .withResult(fromTable);
            builder.withFetchParameters(fetchParameters);
            SqlSelectData sqlSelectData = (SqlSelectData) builder.build();
            return new StatementParameters(sqlSelectData, parameters);
        }

        List<Value> values = createSelectionValues(fromTable, selection, aliasGenerator);
        List<FetchParameter> fetchParameters = createFetchParameters(selection, criteriaQuery.getResultType());

        builder.withValues(values).withConditions(conditions);
        builder.withFetchParameters(fetchParameters);
        SqlSelect sqlSelect = builder.build();
        return new StatementParameters(sqlSelect, parameters);
    }

    private QueryParameter createQueryParameter(AttributePath<?> miniPath, Object value) {
        MetaAttribute a = miniPath.getMetaAttribute();
        return new QueryParameter(a.getColumnName(), value, a.getType(), a.getSqlType(), a.getAttributeMapper());
    }

    public SqlUpdate update(Query query, List<QueryParameter> parameters, AliasGenerator tableAliasGenerator) {
        CriteriaUpdate<?> criteriaUpdate = ((UpdateQuery) query).getCriteriaUpdate();
        MetaEntity entity = ((MiniRoot<?>) criteriaUpdate.getRoot()).getMetaEntity();

        FromTable fromTable = FromTable.of(entity.getTableName(),
                tableAliasGenerator.getDefault(entity.getTableName()));
        List<TableColumn> columns = new ArrayList<>();
        Map<Path<?>, Object> setValues = ((MiniCriteriaUpdate) criteriaUpdate).getSetValues();
        setValues.forEach((k, v) -> {
            AttributePath<?> miniPath = (AttributePath<?>) k;
            TableColumn tableColumn = new TableColumn(fromTable,
                    new Column(miniPath.getMetaAttribute().getColumnName()));
            columns.add(tableColumn);
        });

        Predicate restriction = criteriaUpdate.getRestriction();
        Optional<Condition> optionalCondition = Optional.empty();
        if (restriction != null)
            optionalCondition = createConditions(restriction, parameters, query, tableAliasGenerator);

        return new SqlUpdate(fromTable, columns, optionalCondition);
    }

    public List<QueryParameter> createUpdateParameters(Query query) {
        CriteriaUpdate<?> criteriaUpdate = ((UpdateQuery) query).getCriteriaUpdate();
        List<QueryParameter> parameters = new ArrayList<>();
        Map<Path<?>, Object> setValues = ((MiniCriteriaUpdate) criteriaUpdate).getSetValues();
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
        if (restriction != null)
            optionalCondition = createConditions(restriction, parameters, query, tableAliasGenerator);

        SqlDelete sqlDelete = new SqlDelete(fromTable, optionalCondition);
        return new StatementParameters(sqlDelete, parameters);
    }

}
