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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Query;
import javax.persistence.criteria.CollectionJoin;
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
import javax.persistence.metamodel.Attribute;
import org.minijpa.jdbc.BasicFetchParameter;
import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.JdbcTypes;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.db.SqlSelectData;
import org.minijpa.jdbc.db.SqlSelectDataBuilder;
import org.minijpa.jdbc.mapper.AbstractDbTypeMapper;
import org.minijpa.jdbc.mapper.AttributeMapper;
import org.minijpa.jpa.DeleteQuery;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.MiniTypedQuery;
import org.minijpa.jpa.UpdateQuery;
import org.minijpa.jpa.criteria.AttributePath;
import org.minijpa.jpa.criteria.CriteriaUtils;
import org.minijpa.jpa.criteria.MiniCriteriaUpdate;
import org.minijpa.jpa.criteria.MiniRoot;
import org.minijpa.jpa.criteria.expression.AggregateFunctionExpression;
import org.minijpa.jpa.criteria.expression.BinaryExpression;
import org.minijpa.jpa.criteria.expression.CoalesceExpression;
import org.minijpa.jpa.criteria.expression.ConcatExpression;
import org.minijpa.jpa.criteria.expression.CriteriaExpressionHelper;
import org.minijpa.jpa.criteria.expression.CurrentDateExpression;
import org.minijpa.jpa.criteria.expression.CurrentTimeExpression;
import org.minijpa.jpa.criteria.expression.CurrentTimestampExpression;
import org.minijpa.jpa.criteria.expression.ExpressionOperator;
import org.minijpa.jpa.criteria.expression.LocateExpression;
import org.minijpa.jpa.criteria.expression.NegationExpression;
import org.minijpa.jpa.criteria.expression.NullifExpression;
import org.minijpa.jpa.criteria.expression.SubstringExpression;
import org.minijpa.jpa.criteria.expression.TrimExpression;
import org.minijpa.jpa.criteria.expression.TypecastExpression;
import org.minijpa.jpa.criteria.expression.UnaryExpression;
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
import org.minijpa.sql.model.From;
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
import org.minijpa.sql.model.join.FromJoin;
import org.minijpa.sql.model.join.FromJoinImpl;
import org.minijpa.sql.model.join.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlStatementFactory extends JdbcSqlStatementFactory {

  private final Logger LOG = LoggerFactory.getLogger(SqlStatementFactory.class);
  protected CriteriaExpressionHelper criteriaExpressionHelper;

  public SqlStatementFactory() {
  }

  public void init() {
    this.criteriaExpressionHelper = new CriteriaExpressionHelper();
  }

  public SqlInsert generateInsert(MetaEntity entity, List<String> columns,
      boolean hasIdentityColumn, boolean identityColumnNull, Optional<MetaEntity> metaEntity,
      AliasGenerator tableAliasGenerator) throws Exception {
    List<Column> cs = columns.stream().map(c -> {
      return new Column(c);
    }).collect(Collectors.toList());

    return new SqlInsert(
        FromTable.of(entity.getTableName(), tableAliasGenerator.getDefault(entity.getTableName())),
        cs, hasIdentityColumn, identityColumnNull, metaEntity.isPresent() ? Optional.of(
        metaEntity.get().getId().getAttribute().getColumnName()) : Optional.empty());
  }

  private Optional<ForUpdate> calcForUpdate(LockType lockType) {
    if (lockType == null) {
      return Optional.empty();
    }

    if (lockType == LockType.PESSIMISTIC_WRITE) {
      return Optional.of(new ForUpdate());
    }

    return Optional.empty();
  }

  public SqlSelectData generateSelectById(MetaEntity entity, LockType lockType,
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

  public SqlSelectData generateSelectVersion(MetaEntity entity, LockType lockType,
      AliasGenerator tableAliasGenerator) throws Exception {
    FetchParameter fetchParameter = MetaEntityHelper.toFetchParameter(
        entity.getVersionAttribute().get());

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

    sqlSelectBuilder.withValues(
            List.of(MetaEntityHelper.toValue(entity.getVersionAttribute().get(), fromTable)))
        .withConditions(List.of(condition));
    sqlSelectBuilder.withFetchParameters(List.of(fetchParameter));
    return (SqlSelectData) sqlSelectBuilder.build();
  }

  public SqlSelectData generateSelectByForeignKey(MetaEntity entity,
      MetaAttribute foreignKeyAttribute, List<String> columns, AliasGenerator tableAliasGenerator)
      throws Exception {
    List<FetchParameter> fetchColumnNameValues = MetaEntityHelper.convertAllAttributes(entity);
    // LOG.info("generateSelectByForeignKey: fetchColumnNameValues=" +
    // fetchColumnNameValues);
    // LOG.info("generateSelectByForeignKey: parameters=" + parameters);
    FromTable fromTable = FromTable.of(entity.getTableName(),
        tableAliasGenerator.getDefault(entity.getTableName()));
    List<TableColumn> tableColumns = columns.stream()
        .map(c -> new TableColumn(fromTable, new Column(c))).collect(Collectors.toList());
    List<Condition> conditions = tableColumns.stream().map(t -> {
      return new BinaryCondition.Builder(ConditionType.EQUAL).withLeft(t)
          .withRight(CriteriaUtils.QM).build();
    }).collect(Collectors.toList());

    Condition condition = Condition.toAnd(conditions);
    SqlSelectDataBuilder sqlSelectBuilder = new SqlSelectDataBuilder();
    sqlSelectBuilder.withFromTable(fromTable);
    sqlSelectBuilder.withValues(MetaEntityHelper.toValues(entity, fromTable))
        .withConditions(Arrays.asList(condition)).withResult(fromTable);
    sqlSelectBuilder.withFetchParameters(fetchColumnNameValues);
    return (SqlSelectData) sqlSelectBuilder.build();
  }

  public ModelValueArray<AbstractAttribute> expandJoinColumnAttributes(Pk owningId,
      Object joinTableForeignKey, List<JoinColumnAttribute> allJoinColumnAttributes)
      throws Exception {
    ModelValueArray<MetaAttribute> modelValueArray = new ModelValueArray<>();
    LOG.debug("expandJoinColumnAttributes: owningId={}", owningId);
    MetaEntityHelper.expand(owningId, joinTableForeignKey, modelValueArray);

    LOG.debug("expandJoinColumnAttributes: modelValueArray.size()={}", modelValueArray.size());
    allJoinColumnAttributes.forEach(
        a -> LOG.debug("expandJoinColumnAttributes: a.getForeignKeyAttribute()={}",
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

      return Arrays.asList(fromJoin, fromJoin2);
    } else if (metaAttribute.getRelationship().getJoinColumnMapping().isPresent()) {
      List<JoinColumnAttribute> joinColumnAttributes = metaAttribute.getRelationship()
          .getJoinColumnMapping().get().getJoinColumnAttributes();
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

  public FromJoin calculateFromTableByJoinTable(MetaEntity entity,
      RelationshipJoinTable relationshipJoinTable, AliasGenerator tableAliasGenerator) {
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

  public Condition generateJoinCondition(Relationship relationship, MetaEntity owningEntity,
      MetaEntity targetEntity, AliasGenerator tableAliasGenerator) {
    if (relationship.getJoinTable() != null) {
      RelationshipJoinTable relationshipJoinTable = relationship.getJoinTable();
      Pk pk = relationshipJoinTable.getOwningAttribute();
      pk.getAttributes().forEach(
          a -> LOG.debug("generateJoinCondition: relationshipJoinTable.getOwningAttribute()={}",
              a.getColumnName()));
      relationshipJoinTable.getTargetAttribute().getAttributes().forEach(
          a -> LOG.debug("generateJoinCondition: relationshipJoinTable.getTargetAttribute()={}",
              a.getColumnName()));
      relationshipJoinTable.getOwningJoinColumnMapping().getJoinColumnAttributes().forEach(
          a -> LOG.debug("generateJoinCondition: getOwningJoinColumnMapping a.getColumnName()={}",
              a.getColumnName()));
      relationshipJoinTable.getOwningJoinColumnMapping().getJoinColumnAttributes().forEach(
          a -> LOG.debug(
              "generateJoinCondition: getOwningJoinColumnMapping a.getAttribute().getColumnName()={}",
              a.getAttribute().getColumnName()));
      relationshipJoinTable.getTargetJoinColumnMapping().getJoinColumnAttributes().forEach(
          a -> LOG.debug("generateJoinCondition: getTargetJoinColumnMapping a.getColumnName()={}",
              a.getColumnName()));
      relationshipJoinTable.getTargetJoinColumnMapping().getJoinColumnAttributes().forEach(
          a -> LOG.debug(
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
        MetaAttribute ak = targetEntity.getId().getAttributes().get(i);
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
   * @throws Exception
   */
  public SqlSelectData generateSelectByJoinTable(MetaEntity entity,
      RelationshipJoinTable relationshipJoinTable, List<AbstractAttribute> attributes,
      AliasGenerator aliasGenerator) throws Exception {
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
    List<Condition> conditions = tableColumns.stream().map(t -> {
      return new BinaryCondition.Builder(ConditionType.EQUAL).withLeft(t)
          .withRight(CriteriaUtils.QM).build();
    }).collect(Collectors.toList());

    Condition condition = Condition.toAnd(conditions);
    List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
    List<FetchParameter> fetchColumnNameValues = MetaEntityHelper.convertAttributes(
        expandedAttributes);
    SqlSelectDataBuilder sqlSelectBuilder = new SqlSelectDataBuilder();
    sqlSelectBuilder.withFromTable(fromTable);
    sqlSelectBuilder.withFromTable(fromJoin)
        .withValues(MetaEntityHelper.toValues(entity, fromTable))
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
    List<FetchParameter> fetchColumnNameValues = MetaEntityHelper.convertAttributes(
        expandedAttributes);
    SqlSelectDataBuilder sqlSelectBuilder = new SqlSelectDataBuilder();
    sqlSelectBuilder.withFromTable(fromTable);
    sqlSelectBuilder.withFromTable(fromJoin)
        .withValues(MetaEntityHelper.toValues(entity, fromTable))
        .withConditions(Arrays.asList(condition)).withResult(fromTable).build();
    sqlSelectBuilder.withFetchParameters(fetchColumnNameValues);
    return (SqlSelectData) sqlSelectBuilder.build();
  }

  public List<QueryParameter> createRelationshipJoinTableParameters(
      RelationshipJoinTable relationshipJoinTable, Object owningInstance, Object targetInstance)
      throws Exception {
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

  public SqlInsert generateJoinTableInsert(RelationshipJoinTable relationshipJoinTable,
      List<String> columnNames) throws Exception {
    List<Column> columns = columnNames.stream().map(c -> new Column(c))
        .collect(Collectors.toList());
    return new SqlInsert(new FromTableImpl(relationshipJoinTable.getTableName()), columns, false,
        false, Optional.empty());
  }

  public SqlUpdate generateUpdate(MetaEntity entity, List<String> columns,
      List<String> idColumnNames, AliasGenerator tableAliasGenerator) throws Exception {
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

  public SqlDelete generateDeleteById(FromTable fromTable, List<String> idColumnNames)
      throws Exception {
    Condition condition = createAttributeEqualCondition(fromTable, idColumnNames);
    return new SqlDelete(fromTable, Optional.of(condition));
  }

  private Condition createAttributeEqualCondition(FromTable fromTable, List<String> columns)
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

  private Optional<FetchParameter> createFetchParameter(Selection<?> selection,
      Class<?> resultClass) {
    if (selection == null) {
      return Optional.empty();
    }

    LOG.debug("createFetchParameter: selection={}", selection);
    if (selection instanceof AttributePath<?>) {
      AttributePath<?> miniPath = (AttributePath<?>) selection;
      MetaAttribute metaAttribute = miniPath.getMetaAttribute();
      FetchParameter columnNameValue = AttributeFetchParameter.build(metaAttribute);
      return Optional.of(columnNameValue);
    } else if (selection instanceof AggregateFunctionExpression<?>) {
      AggregateFunctionExpression<?> aggregateFunctionExpression = (AggregateFunctionExpression<?>) selection;
      if (aggregateFunctionExpression.getAggregateFunctionType()
          == org.minijpa.jpa.criteria.expression.AggregateFunctionType.COUNT) {
        FetchParameter cnv = new BasicFetchParameter("count", null, Optional.empty());
        return Optional.of(cnv);
      } else if (aggregateFunctionExpression.getAggregateFunctionType()
          == org.minijpa.jpa.criteria.expression.AggregateFunctionType.SUM) {
        FetchParameter cnv = new BasicFetchParameter("sum", null, Optional.empty());
        return Optional.of(cnv);
      } else if (aggregateFunctionExpression.getAggregateFunctionType()
          == org.minijpa.jpa.criteria.expression.AggregateFunctionType.AVG) {
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
        if (checkDataType((AttributePath<?>) binaryExpression.getX().get(), sqlType)) {
          return Optional.of(new BasicFetchParameter("result", sqlType, Optional.empty()));
        }
      }

      if (binaryExpression.getY().isPresent()) {
        if (checkDataType((AttributePath<?>) binaryExpression.getY().get(), sqlType)) {
          return Optional.of(new BasicFetchParameter("result", sqlType, Optional.empty()));
        }
      }

      if (binaryExpression.getxValue().isPresent()) {
        if (sqlType != null && sqlType != Types.NULL
            && binaryExpression.getxValue().get().getClass() == resultClass) {
          return Optional.of(new BasicFetchParameter("result", sqlType, Optional.empty()));
        }
      }

      if (binaryExpression.getyValue().isPresent()) {
        if (sqlType != null && sqlType != Types.NULL
            && binaryExpression.getyValue().get().getClass() == resultClass) {
          return Optional.of(new BasicFetchParameter("result", sqlType, Optional.empty()));
        }
      }

      sqlType = calculateSqlType(binaryExpression);
      Optional<AttributeMapper> attributeMapper = Optional.empty();
      if (sqlType == Types.FLOAT) {
        // Postgres throws an exception: Persistence conversion to class java.lang.Float
        // from numeric not supported
        attributeMapper = Optional.of(AbstractDbTypeMapper.numberToFloatAttributeMapper);
        sqlType = Types.OTHER;
      }

      LOG.debug("createFetchParameter: sqlType={}", sqlType);
      return Optional.of(new BasicFetchParameter("result", sqlType, attributeMapper));
    } else if (selection instanceof UnaryExpression<?>) {
      UnaryExpression<?> unaryExpression = (UnaryExpression<?>) selection;
      if (unaryExpression.getExpressionOperator() == ExpressionOperator.SQRT) {
        return Optional.of(new BasicFetchParameter("result", Types.DOUBLE, Optional.empty()));
      } else if (unaryExpression.getExpressionOperator() == ExpressionOperator.LOWER) {
        return Optional.of(new BasicFetchParameter("result", Types.VARCHAR, Optional.empty()));
      } else if (unaryExpression.getExpressionOperator() == ExpressionOperator.UPPER) {
        return Optional.of(new BasicFetchParameter("result", Types.VARCHAR, Optional.empty()));
      } else if (unaryExpression.getExpressionOperator() == ExpressionOperator.ABS) {
        return Optional.of(new BasicFetchParameter("result", null, Optional.empty()));
      } else if (unaryExpression.getExpressionOperator() == ExpressionOperator.MOD) {
        return Optional.of(new BasicFetchParameter("result", Types.INTEGER, Optional.empty()));
      }
    } else if (selection instanceof TypecastExpression<?>) {
      ExpressionOperator expressionOperator = ((TypecastExpression<?>) selection).getExpressionOperator();
      if (expressionOperator == ExpressionOperator.TO_BIGDECIMAL) {
        return Optional.of(new BasicFetchParameter("result", Types.DECIMAL, Optional.empty()));
      } else if (expressionOperator == ExpressionOperator.TO_BIGINTEGER) {
        return Optional.of(new BasicFetchParameter("result", Types.OTHER,
            Optional.of(AbstractDbTypeMapper.numberToBigIntegerAttributeMapper)));
      } else if (expressionOperator == ExpressionOperator.TO_DOUBLE) {
        return Optional.of(new BasicFetchParameter("result", Types.OTHER,
            Optional.of(AbstractDbTypeMapper.numberToDoubleAttributeMapper)));
      } else if (expressionOperator == ExpressionOperator.TO_FLOAT) {
        return Optional.of(new BasicFetchParameter("result", Types.OTHER,
            Optional.of(AbstractDbTypeMapper.numberToFloatAttributeMapper)));
      } else if (expressionOperator == ExpressionOperator.TO_INTEGER) {
        return Optional.of(new BasicFetchParameter("result", Types.INTEGER, Optional.empty()));
      } else if (expressionOperator == ExpressionOperator.TO_LONG) {
        return Optional.of(new BasicFetchParameter("result", Types.OTHER,
            Optional.of(AbstractDbTypeMapper.numberToLongAttributeMapper)));
      }
    } else if (selection instanceof ConcatExpression) {
      return Optional.of(new BasicFetchParameter("concat", Types.VARCHAR, Optional.empty()));
    } else if (selection instanceof TrimExpression) {
      return Optional.of(new BasicFetchParameter("trim", Types.VARCHAR, Optional.empty()));
    } else if (selection instanceof LocateExpression) {
      return Optional.of(new BasicFetchParameter("locate", Types.INTEGER, Optional.empty()));
    } else if (selection instanceof SubstringExpression) {
      return Optional.of(new BasicFetchParameter("substring", Types.VARCHAR, Optional.empty()));
    } else if (selection instanceof NullifExpression) {
      return Optional.of(new BasicFetchParameter("nullif", Types.OTHER, Optional.empty()));
    } else if (selection instanceof CoalesceExpression) {
      return Optional.of(new BasicFetchParameter("coalesce", Types.OTHER, Optional.empty()));
    } else if (selection instanceof NegationExpression) {
      return Optional.of(new BasicFetchParameter("negation", Types.OTHER, Optional.empty()));
    } else if (selection instanceof CurrentDateExpression) {
      return Optional.of(new BasicFetchParameter("date", Types.DATE, Optional.empty()));
    } else if (selection instanceof CurrentTimeExpression) {
      return Optional.of(new BasicFetchParameter("time", Types.TIME, Optional.empty()));
    } else if (selection instanceof CurrentTimestampExpression) {
      return Optional.of(new BasicFetchParameter("timestamp", Types.TIMESTAMP, Optional.empty()));
    }

    return Optional.empty();
  }

  private Integer calculateSqlType(BinaryExpression binaryExpression) {
    LOG.debug("calculateSqlType: binaryExpression.getX().isPresent()={}",
        binaryExpression.getX().isPresent());
    if (binaryExpression.getX().isPresent()) {
      LOG.debug("calculateSqlType: binaryExpression.getX().get()={}",
          binaryExpression.getX().get());
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
      LOG.debug("calculateSqlType: sqlType1={}, sqlType2={}, compare={}", sqlType1, sqlType2,
          compare);
      return compare < 0 ? sqlType2 : sqlType1;
    }

    Integer sqlType2 = JdbcTypes.sqlTypeFromClass(binaryExpression.getyValue().get().getClass());
    int compare = JdbcTypes.compareNumericTypes(sqlType1, sqlType2);
    LOG.debug("calculateSqlType: sqlType1={}, sqlType2={}, compare={}", sqlType1, sqlType2,
        compare);
    return compare < 0 ? sqlType2 : sqlType1;
  }

  private Integer calculateSqlTypeFromExpression(Expression<?> expression,
      Optional<Expression<?>> optionalExpression, Optional<Object> optionalValue) {
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
      LOG.debug("calculateSqlTypeFromExpression: sqlType1={}, sqlType2={}, compare={}", sqlType1,
          sqlType2, compare);
      return compare < 0 ? sqlType2 : sqlType1;
    }

    Integer sqlType2 = JdbcTypes.sqlTypeFromClass(optionalValue.get().getClass());
    int compare = JdbcTypes.compareNumericTypes(sqlType1, sqlType2);
    LOG.debug("calculateSqlTypeFromExpression: value={}", optionalValue.get());
    LOG.debug("calculateSqlTypeFromExpression: sqlType1={}, sqlType2={}, compare={}", sqlType1,
        sqlType2, compare);
    return compare < 0 ? sqlType2 : sqlType1;
  }

  private boolean checkDataType(AttributePath<?> attributePath, Integer sqlType) {
    MetaAttribute metaAttribute = attributePath.getMetaAttribute();
    LOG.debug("checkDataType: sqlType={}, metaAttribute.getSqlType()={}", sqlType,
        metaAttribute.getSqlType());
    if (sqlType != null && sqlType != Types.NULL && sqlType == metaAttribute.getSqlType()) {
      return true;
    }

    return false;
  }


  private TableColumn createTableColumnFromPath(AttributePath<?> attributePath,
      AliasGenerator tableAliasGenerator) {
    MetaAttribute attribute = attributePath.getMetaAttribute();
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

  private Optional<Condition> translateComparisonPredicate(ComparisonPredicate comparisonPredicate,
      List<QueryParameter> parameters, Query query, AliasGenerator tableAliasGenerator) {
    Expression<?> expression1 = comparisonPredicate.getX();
    Expression<?> expression2 = comparisonPredicate.getY();

    ConditionType conditionType = getOperator(comparisonPredicate.getPredicateType());
    BinaryCondition.Builder builder = new BinaryCondition.Builder(conditionType);
    if (comparisonPredicate.isNot()) {
      builder.not();
    }

    if (expression1 instanceof AttributePath<?>) {
      AttributePath<?> attributePath = (AttributePath<?>) expression1;
      MetaAttribute attribute1 = attributePath.getMetaAttribute();
      TableColumn tableColumn1 = createTableColumnFromPath(attributePath, tableAliasGenerator);
      if (expression2 != null) {
        Object p = criteriaExpressionHelper.createParameterFromExpression(query, expression2,
            tableAliasGenerator, parameters, attribute1.getColumnName(), attribute1.getSqlType(),
            attribute1.getAttributeMapper());
        builder.withLeft(tableColumn1).withRight(p);
      } else if (comparisonPredicate.getValue() != null) {
        if (CriteriaUtils.requireQM(comparisonPredicate.getValue())) {
          QueryParameter queryParameter = new QueryParameter(attribute1.getColumnName(),
              comparisonPredicate.getValue(), attribute1.getSqlType(),
              attribute1.getAttributeMapper());
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
      BetweenExpressionsPredicate betweenExpressionsPredicate, List<QueryParameter> parameters,
      Query query, AliasGenerator tableAliasGenerator) {
    Expression<?> expression1 = betweenExpressionsPredicate.getX();
    Expression<?> expression2 = betweenExpressionsPredicate.getY();
    AttributePath<?> miniPath = (AttributePath<?>) betweenExpressionsPredicate.getV();
    MetaAttribute attribute = miniPath.getMetaAttribute();

    BetweenCondition.Builder builder = new BetweenCondition.Builder(
        createTableColumnFromPath(miniPath, tableAliasGenerator));
    builder.withLeftExpression(
        criteriaExpressionHelper.createParameterFromExpression(query, expression1,
            tableAliasGenerator, parameters, attribute.getColumnName(), attribute.getSqlType(),
            attribute.getAttributeMapper()));

    builder.withRightExpression(
        criteriaExpressionHelper.createParameterFromExpression(query, expression2,
            tableAliasGenerator, parameters, attribute.getColumnName(), attribute.getSqlType(),
            attribute.getAttributeMapper()));

    return Optional.of(builder.build());
  }

  private Optional<Condition> translateBetweenValuesPredicate(
      BetweenValuesPredicate betweenValuesPredicate, List<QueryParameter> parameters,
      AliasGenerator tableAliasGenerator) {
    Object x = betweenValuesPredicate.getX();
    Object y = betweenValuesPredicate.getY();
    AttributePath<?> miniPath = (AttributePath<?>) betweenValuesPredicate.getV();
    MetaAttribute attribute = miniPath.getMetaAttribute();

    BetweenCondition.Builder builder = new BetweenCondition.Builder(
        createTableColumnFromPath(miniPath, tableAliasGenerator));
    if (CriteriaUtils.requireQM(x)) {
      QueryParameter queryParameter = new QueryParameter(attribute.getColumnName(), x,
          attribute.getSqlType(), attribute.getAttributeMapper());
      parameters.add(queryParameter);
      builder.withLeftExpression(CriteriaUtils.QM);
    } else {
      builder.withLeftExpression(CriteriaUtils.buildValue(x));
    }

    if (CriteriaUtils.requireQM(y)) {
      QueryParameter queryParameter = new QueryParameter(attribute.getColumnName(), y,
          attribute.getSqlType(), attribute.getAttributeMapper());
      parameters.add(queryParameter);
      builder.withRightExpression(CriteriaUtils.QM);
    } else {
      builder.withRightExpression(CriteriaUtils.buildValue(y));
    }

    return Optional.of(builder.build());
  }

  private Optional<Condition> translateBooleanExprPredicate(
      BooleanExprPredicate booleanExprPredicate, List<QueryParameter> parameters, Query query,
      AliasGenerator tableAliasGenerator) {
    Expression<Boolean> x = booleanExprPredicate.getX();

    if (x instanceof Predicate) {
      Optional<Condition> optional = createConditions((Predicate) x, parameters, query,
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

  private Optional<Condition> translateExprPredicate(ExprPredicate exprPredicate,
      List<QueryParameter> parameters, Query query, AliasGenerator aliasGenerator) {
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
      if (optional.isPresent()) {
        conditions.add(optional.get());
      }
    }

    if (!conditions.isEmpty()) {
      return Optional.of(
          new BinaryLogicConditionImpl(getOperator(multiplePredicate.getPredicateType()),
              conditions, true));
    }

    return Optional.empty();
  }

  private Optional<Condition> translateBinaryBooleanExprPredicate(
      BinaryBooleanExprPredicate binaryBooleanExprPredicate, List<QueryParameter> parameters,
      Query query, AliasGenerator tableAliasGenerator) {
    Expression<Boolean> x = binaryBooleanExprPredicate.getX();
    Expression<Boolean> y = binaryBooleanExprPredicate.getY();

    LOG.debug("translateBinaryBooleanExprPredicate: x={}", x);
    LOG.debug("translateBinaryBooleanExprPredicate: y={}", y);
    List<Condition> conditions = new ArrayList<>();
    if (x instanceof Predicate) {
      Optional<Condition> optional = createConditions((Predicate) x, parameters, query,
          tableAliasGenerator);
      optional.ifPresent(conditions::add);
    }

    if (y instanceof Predicate) {
      Optional<Condition> optional = createConditions((Predicate) y, parameters, query,
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

  private Optional<Condition> translateInPredicate(InPredicate<?> inPredicate,
      List<QueryParameter> parameters, Query query, AliasGenerator tableAliasGenerator) {
    Expression<?> expression = inPredicate.getExpression();
    LOG.info("translateInPredicate: expression={}", expression);
    TableColumn tableColumn = null;
    if (expression instanceof AttributePath<?>) {
      AttributePath<?> miniPath = (AttributePath<?>) expression;
      MetaAttribute attribute = miniPath.getMetaAttribute();
      tableColumn = createTableColumnFromPath(miniPath, tableAliasGenerator);

      List<String> list = inPredicate.getValues().stream().map(v -> {
        return CriteriaUtils.QM;
      }).collect(Collectors.toList());

      List<QueryParameter> queryParameters = inPredicate.getValues().stream().map(v -> {
        return new QueryParameter(attribute.getColumnName(), v, attribute.getSqlType(),
            attribute.getAttributeMapper());
      }).collect(Collectors.toList());
      parameters.addAll(queryParameters);

      return Optional.of(new InCondition(tableColumn, list, inPredicate.isNot()));
    }

    return Optional.empty();
  }

  private Optional<Condition> translateLikePredicate(LikePredicate likePredicate,
      List<QueryParameter> parameters, Query query, AliasGenerator aliasGenerator) {
    Optional<Expression<String>> patternExpression = likePredicate.getPatternExpression();
    Optional<String> pattern = likePredicate.getPattern();
    AttributePath<?> miniPath = (AttributePath<?>) likePredicate.getX();
    MetaAttribute attribute = miniPath.getMetaAttribute();
    Object right = null;
    if (pattern.isPresent()) {
      right = CriteriaUtils.buildValue(pattern.get());
    } else if (patternExpression.isPresent()) {
      right = criteriaExpressionHelper.createParameterFromExpression(query, patternExpression.get(),
          aliasGenerator, parameters, attribute.getColumnName(), attribute.getSqlType(),
          attribute.getAttributeMapper());
    }

    String escapeChar = null;
    Optional<Character> escapeCharacter = likePredicate.getEscapeChar();
    Optional<Expression<Character>> escapeExpression = likePredicate.getEscapeCharEx();
    if (escapeCharacter.isPresent()) {
      escapeChar = CriteriaUtils.buildValue("" + likePredicate.getEscapeChar().get());
    } else if (escapeExpression.isPresent()) {
      if (escapeExpression.get() instanceof ParameterExpression<?>) {
        ParameterExpression<?> parameterExpression = (ParameterExpression<?>) escapeExpression.get();

        // TODO: escape char not working on Derby (OneToManyUniTest.testLike5). Replaced
        // with string value
//                parameters.add(createQueryParameterForParameterExpression(parameterExpression, "escape",
//                        Character.class, Types.CHAR, query));
//                escapeChar = QM;

        Object value = null;
        if (parameterExpression.getName() != null) {
          value = query.getParameterValue(parameterExpression.getName());
        } else if (parameterExpression.getPosition() != null) {
          value = query.getParameter(parameterExpression.getPosition());
        }

        escapeChar = CriteriaUtils.buildValue("" + String.valueOf((char) value));
      }
    }

    LikeCondition likeCondition = new LikeCondition(
        createTableColumnFromPath(miniPath, aliasGenerator), right, escapeChar,
        likePredicate.isNot());

    return Optional.of(likeCondition);
  }

  private Optional<Condition> createConditions(Predicate predicate, List<QueryParameter> parameters,
      Query query, AliasGenerator tableAliasGenerator) {
    PredicateTypeInfo predicateTypeInfo = (PredicateTypeInfo) predicate;
    PredicateType predicateType = predicateTypeInfo.getPredicateType();
    if (predicateType == PredicateType.EQUAL || predicateType == PredicateType.NOT_EQUAL
        || predicateType == PredicateType.GREATER_THAN
        || predicateType == PredicateType.GREATER_THAN_OR_EQUAL_TO
        || predicateType == PredicateType.GT || predicateType == PredicateType.LESS_THAN
        || predicateType == PredicateType.LESS_THAN_OR_EQUAL_TO
        || predicateType == PredicateType.LT) {
      return translateComparisonPredicate((ComparisonPredicate) predicate, parameters, query,
          tableAliasGenerator);
    } else if (predicateType == PredicateType.BETWEEN_EXPRESSIONS) {
      BetweenExpressionsPredicate betweenExpressionsPredicate = (BetweenExpressionsPredicate) predicate;
      return translateBetweenExpressionsPredicate(betweenExpressionsPredicate, parameters, query,
          tableAliasGenerator);
    } else if (predicateType == PredicateType.BETWEEN_VALUES) {
      BetweenValuesPredicate betweenValuesPredicate = (BetweenValuesPredicate) predicate;
      return translateBetweenValuesPredicate(betweenValuesPredicate, parameters,
          tableAliasGenerator);
    } else if (predicateType == PredicateType.LIKE_PATTERN) {
      LikePredicate likePredicate = (LikePredicate) predicate;
      return translateLikePredicate(likePredicate, parameters, query, tableAliasGenerator);
    } else if (predicateType == PredicateType.OR || predicateType == PredicateType.AND) {
      if (predicate instanceof MultiplePredicate) {
        return translateMultiplePredicate((MultiplePredicate) predicate, parameters, query,
            tableAliasGenerator);
      } else {
        return translateBinaryBooleanExprPredicate((BinaryBooleanExprPredicate) predicate,
            parameters, query, tableAliasGenerator);
      }
    } else if (predicateType == PredicateType.NOT) {
      return translateBooleanExprPredicate((BooleanExprPredicate) predicate, parameters, query,
          tableAliasGenerator);
    } else if (predicateType == PredicateType.IS_NULL
        || predicateType == PredicateType.IS_NOT_NULL) {
      return translateExprPredicate((ExprPredicate) predicate, parameters, query,
          tableAliasGenerator);
    } else if (predicateType == PredicateType.EQUALS_TRUE
        || predicateType == PredicateType.EQUALS_FALSE) {
      return translateBooleanExprPredicate((BooleanExprPredicate) predicate, parameters, query,
          tableAliasGenerator);
    } else if (predicateType == PredicateType.EMPTY_CONJUNCTION
        || predicateType == PredicateType.EMPTY_DISJUNCTION) {
      return Optional.of(new EmptyCondition(getOperator(predicateType)));
    } else if (predicateType == PredicateType.IN) {
      return translateInPredicate((InPredicate<?>) predicate, parameters, query,
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

  private List<From> buildFromClauseFromJoins(Set<Root<?>> roots,
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
          MetaAttribute metaAttribute = entity.getAttribute(attributeName);
          List<FromJoin> fromJoins = calculateJoins(entity, metaAttribute,
              decodeJoinType(collectionJoin.getJoinType()), aliasGenerator);
          froms.addAll(fromJoins);
        }
      });
    });

    return froms;
  }

  private List<Value> buildValues(MiniRoot<?> miniRoot, List<FromTable> fromTables) {
    MetaEntity entity = miniRoot.getMetaEntity();
    Optional<FromTable> optional = fromTables.stream()
        .filter(ft -> ft.getName().equals(entity.getTableName())).findFirst();
    return optional.map(fromTable -> MetaEntityHelper.toValues(entity, fromTable))
        .orElseGet(List::of);
  }

  private List<Value> buildSelectionValues(CriteriaQuery<?> criteriaQuery,
      List<FromTable> fromTables, Query query, AliasGenerator aliasGenerator,
      List<QueryParameter> parameters) {
    Selection<?> selection = criteriaQuery.getSelection();
    if (selection == null) {
      List<Value> values = new ArrayList<>();
      criteriaQuery.getRoots()
          .forEach(r -> values.addAll(buildValues((MiniRoot<?>) r, fromTables)));
      return values;
    }

    if (selection instanceof MiniRoot<?>) {
      return buildValues((MiniRoot<?>) selection, fromTables);
    }

    List<Value> values = new ArrayList<>();
    if (selection.isCompoundSelection()) {
      List<Selection<?>> selections = selection.getCompoundSelectionItems();
      for (Selection<?> s : selections) {
        FromTable fromTable = null; // get FromTable from selection
        Optional<Value> optional = criteriaExpressionHelper.createSelectionValue(fromTable,
            aliasGenerator, s, query, parameters);
        optional.ifPresent(values::add);
      }

      return values;
    }

    FromTable fromTable = null; // get FromTable from selection
    Optional<Value> optional = criteriaExpressionHelper.createSelectionValue(fromTable,
        aliasGenerator, selection, query, parameters);
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
      });
      return fetchParameters;
    }

    if (selection instanceof MiniRoot<?>) {
      MetaEntity entity = ((MiniRoot<?>) selection).getMetaEntity();
      return MetaEntityHelper.convertAllAttributes(entity);
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

  public StatementParameters select(Query query, AliasGenerator aliasGenerator) {
    CriteriaQuery<?> criteriaQuery = ((MiniTypedQuery<?>) query).getCriteriaQuery();

    Predicate restriction = criteriaQuery.getRestriction();
    List<QueryParameter> parameters = new ArrayList<>();
    Optional<Condition> optionalCondition = Optional.empty();
    if (restriction != null) {
      optionalCondition = createConditions(restriction, parameters, query, aliasGenerator);
    }

    List<Condition> conditions = null;
    if (optionalCondition.isPresent()) {
      conditions = List.of(optionalCondition.get());
    }

    SqlSelectDataBuilder builder = new SqlSelectDataBuilder();
    List<From> froms = buildFromClauseFromJoins(criteriaQuery.getRoots(), aliasGenerator);
    froms.forEach(builder::withFromTable);
    LOG.debug("select: froms={}", froms);

    LockType lockType = LockTypeUtils.toLockType(query.getLockMode());
    if (lockType != null) {
      builder.withForUpdate(calcForUpdate(lockType));
    }

    Optional<List<OrderBy>> optionalOrderBy = createOrderByList(criteriaQuery, aliasGenerator);
    optionalOrderBy.ifPresent(builder::withOrderBy);

    if (criteriaQuery.isDistinct()) {
      builder.distinct();
    }

    Selection<?> selection = criteriaQuery.getSelection();
    LOG.debug("select: selection={}", selection);
    List<FromTable> fromTables = froms.stream().filter(from -> from instanceof FromTable)
        .map(from -> (FromTable) from)
        .collect(Collectors.toList());
    List<Value> values = buildSelectionValues(criteriaQuery, fromTables, query, aliasGenerator,
        parameters);
    builder.withValues(values);
    builder.withConditions(conditions);
    List<FetchParameter> fetchParameters = createFetchParameters(criteriaQuery,
        criteriaQuery.getResultType());
    builder.withFetchParameters(fetchParameters);

    // TODO to be fixed with more roots case
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
    return new StatementParameters(sqlSelect, parameters);
  }

  private QueryParameter createQueryParameter(AttributePath<?> miniPath, Object value) {
    MetaAttribute a = miniPath.getMetaAttribute();
    return new QueryParameter(a.getColumnName(), value, a.getSqlType(), a.getAttributeMapper());
  }

  public SqlUpdate update(Query query, List<QueryParameter> parameters,
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
      optionalCondition = createConditions(restriction, parameters, query, tableAliasGenerator);
    }

    return new SqlUpdate(fromTable, columns, optionalCondition);
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
      optionalCondition = createConditions(restriction, parameters, query, tableAliasGenerator);
    }

    SqlDelete sqlDelete = new SqlDelete(fromTable, optionalCondition);
    return new StatementParameters(sqlDelete, parameters);
  }

}
