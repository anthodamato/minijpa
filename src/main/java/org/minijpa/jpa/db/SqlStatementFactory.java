package org.minijpa.jpa.db;

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

import org.minijpa.jdbc.AbstractAttributeValue;
import org.minijpa.jdbc.AttributeUtil;
import org.minijpa.jdbc.AttributeValue;
import org.minijpa.jdbc.AttributeValueConverter;
import org.minijpa.jdbc.ColumnNameValue;
import org.minijpa.jdbc.EmbeddedIdAttributeValueConverter;
import org.minijpa.jdbc.JdbcTypes;
import org.minijpa.jdbc.JoinColumnAttribute;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.MetaEntityHelper;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.model.Column;
import org.minijpa.jdbc.model.FromTable;
import org.minijpa.jdbc.model.FromTableImpl;
import org.minijpa.jdbc.model.OrderBy;
import org.minijpa.jdbc.model.SqlDelete;
import org.minijpa.jdbc.model.SqlInsert;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.model.SqlUpdate;
import org.minijpa.jdbc.model.TableColumn;
import org.minijpa.jdbc.model.Value;
import org.minijpa.jdbc.model.aggregate.AggregateFunctionBasicType;
import org.minijpa.jdbc.model.aggregate.BasicAggregateFunction;
import org.minijpa.jdbc.model.aggregate.Count;
import org.minijpa.jdbc.model.condition.BetweenCondition;
import org.minijpa.jdbc.model.condition.BinaryCondition;
import org.minijpa.jdbc.model.condition.BinaryLogicConditionImpl;
import org.minijpa.jdbc.model.condition.Condition;
import org.minijpa.jdbc.model.condition.ConditionType;
import org.minijpa.jdbc.model.condition.EmptyCondition;
import org.minijpa.jdbc.model.condition.InCondition;
import org.minijpa.jdbc.model.condition.UnaryCondition;
import org.minijpa.jdbc.model.condition.UnaryLogicConditionImpl;
import org.minijpa.jdbc.model.expression.SqlBinaryExpression;
import org.minijpa.jdbc.model.expression.SqlBinaryExpressionBuilder;
import org.minijpa.jdbc.model.expression.SqlExpressionOperator;
import org.minijpa.jdbc.model.join.FromJoin;
import org.minijpa.jdbc.model.join.FromJoinImpl;
import org.minijpa.jdbc.relationship.RelationshipJoinTable;
import org.minijpa.jpa.DeleteQuery;
import org.minijpa.jpa.MiniTypedQuery;
import org.minijpa.jpa.UpdateQuery;
import org.minijpa.jpa.criteria.AggregateFunctionExpression;
import org.minijpa.jpa.criteria.AggregateFunctionType;
import org.minijpa.jpa.criteria.BinaryExpression;
import org.minijpa.jpa.criteria.ExpressionOperator;
import org.minijpa.jpa.criteria.predicate.BetweenExpressionsPredicate;
import org.minijpa.jpa.criteria.predicate.BetweenValuesPredicate;
import org.minijpa.jpa.criteria.predicate.BinaryBooleanExprPredicate;
import org.minijpa.jpa.criteria.predicate.BooleanExprPredicate;
import org.minijpa.jpa.criteria.predicate.ComparisonPredicate;
import org.minijpa.jpa.criteria.predicate.ExprPredicate;
import org.minijpa.jpa.criteria.predicate.InPredicate;
import org.minijpa.jpa.criteria.predicate.LikePatternExprPredicate;
import org.minijpa.jpa.criteria.predicate.LikePatternPredicate;
import org.minijpa.jpa.criteria.MiniCriteriaUpdate;
import org.minijpa.jpa.criteria.MiniPath;
import org.minijpa.jpa.criteria.MiniRoot;
import org.minijpa.jpa.criteria.predicate.MultiplePredicate;
import org.minijpa.jpa.criteria.predicate.PredicateType;
import org.minijpa.jpa.criteria.predicate.PredicateTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlStatementFactory {

    public static final String QM = "?";

    private final Logger LOG = LoggerFactory.getLogger(SqlStatementFactory.class);
    private final AttributeValueConverter attributeValueConverter = new EmbeddedIdAttributeValueConverter();
    private final MetaEntityHelper metaEntityHelper = new MetaEntityHelper();

    public SqlInsert generatePlainInsert(Object entityInstance, MetaEntity entity, List<AttributeValue> attrValues)
	    throws Exception {
	MetaAttribute id = entity.getId();
	Object idValue = id.getReadMethod().invoke(entityInstance);
	List<AttributeValue> attrValuesWithId = new ArrayList<>();
	AttributeValue attrValueId = new AttributeValue(id, idValue);
	attrValuesWithId.add(attrValueId);
	attrValuesWithId.addAll(attrValues);

	List<QueryParameter> parameters = metaEntityHelper.convertAVToQP(attrValuesWithId);
	List<Column> columns = parameters.stream().map(p -> {
	    return new Column(p.getColumnName());
	}).collect(Collectors.toList());

	return new SqlInsert(FromTable.of(entity), columns, parameters, idValue);
    }

    public SqlInsert generateInsertIdentityStrategy(MetaEntity entity, List<AttributeValue> attrValues)
	    throws Exception {
	List<QueryParameter> parameters = metaEntityHelper.convertAVToQP(attrValues);
	List<Column> columns = parameters.stream().map(p -> {
	    return new Column(p.getColumnName());
	}).collect(Collectors.toList());

	return new SqlInsert(FromTable.of(entity), columns, parameters);
    }

    public SqlSelect generateSelectById(MetaEntity entity, Object idValue) throws Exception {
	AttributeValue attrValueId = new AttributeValue(entity.getId(), idValue);
	List<ColumnNameValue> fetchColumnNameValues = metaEntityHelper.convertAllAttributes(entity);

	FromTable fromTable = FromTable.of(entity);
	List<TableColumn> tableColumns = metaEntityHelper.toTableColumns(entity.getId().expand(), fromTable);
	List<Condition> conditions = tableColumns.stream().map(t -> {
	    return new BinaryCondition.Builder(ConditionType.EQUAL).withLeftColumn(t).withRightExpression(QM).build();
	}).collect(Collectors.toList());

	Condition condition = Condition.toAnd(conditions);
	return new SqlSelect.SqlSelectBuilder(fromTable).withValues(metaEntityHelper.toValues(entity, fromTable))
		.withFetchParameters(fetchColumnNameValues).withConditions(Arrays.asList(condition))
		.withParameters(metaEntityHelper.convertAVToQP(attrValueId)).build();
    }

    public SqlSelect generateSelectByForeignKey(MetaEntity entity, MetaAttribute foreignKeyAttribute,
	    Object foreignKeyInstance) throws Exception {
	AttributeValue attrValue = new AttributeValue(foreignKeyAttribute, foreignKeyInstance);
	LOG.info("generateSelectByForeignKey: foreignKeyAttribute=" + foreignKeyAttribute);
	LOG.info("generateSelectByForeignKey: foreignKeyInstance=" + foreignKeyInstance);
	List<ColumnNameValue> fetchColumnNameValues = metaEntityHelper.convertAllAttributes(entity);

	LOG.info("generateSelectByForeignKey: fetchColumnNameValues=" + fetchColumnNameValues);
	List<QueryParameter> parameters = metaEntityHelper.convertAVToQP(attrValue);
	LOG.info("generateSelectByForeignKey: parameters=" + parameters);
	FromTable fromTable = FromTable.of(entity);
	List<TableColumn> tableColumns = metaEntityHelper.queryParametersToTableColumns(parameters, fromTable);
	List<Condition> conditions = tableColumns.stream().map(t -> {
	    return new BinaryCondition.Builder(ConditionType.EQUAL).withLeftColumn(t).withRightExpression(QM).build();
	}).collect(Collectors.toList());

	Condition condition = Condition.toAnd(conditions);
	return new SqlSelect.SqlSelectBuilder(fromTable).withValues(metaEntityHelper.toValues(entity, fromTable))
		.withFetchParameters(fetchColumnNameValues).withConditions(Arrays.asList(condition))
		.withParameters(parameters).withResult(entity).build();
    }

    public SqlSelect generateSelectByJoinTable(MetaEntity entity, MetaAttribute owningId, Object joinTableForeignKey,
	    RelationshipJoinTable relationshipJoinTable) throws Exception {
	// select t1.id, t1.p1 from entity t1 inner join jointable j on t1.id=j.id1
	// where j.t2=fk
	List<MetaAttribute> idAttributes = entity.getId().expand();
	List<Column> idColumns = idAttributes.stream().map(a -> {
	    return new Column(a.getColumnName());
	}).collect(Collectors.toList());

	List<Column> idTargetColumns = relationshipJoinTable.getJoinColumnTargetAttributes().stream().map(a -> {
	    return new Column(a.getColumnName());
	}).collect(Collectors.toList());

	FromTable joinTable = new FromTableImpl(relationshipJoinTable.getTableName(), relationshipJoinTable.getAlias());
	FromJoin fromJoin = new FromJoinImpl(joinTable, idColumns, idTargetColumns);
	FromTable fromTable = FromTable.of(entity, fromJoin);
	// handles multiple column pk
	List<JoinColumnAttribute> joinColumnOwningAttributes = relationshipJoinTable.getJoinColumnOwningAttributes();
	List<AttributeValue> owningIdAttributeValues = attributeValueConverter
		.convert(new AttributeValue(owningId, joinTableForeignKey));

	List<AbstractAttributeValue> attributeValues = new ArrayList<>();
	for (AttributeValue av : owningIdAttributeValues) {
	    int index = AttributeUtil.indexOfJoinColumnAttribute(joinColumnOwningAttributes, av.getAttribute());
	    attributeValues.add(new AbstractAttributeValue(joinColumnOwningAttributes.get(index), av.getValue()));
	}

	List<QueryParameter> parameters = metaEntityHelper.convertAbstractAVToQP(attributeValues);
	List<TableColumn> tableColumns = metaEntityHelper.queryParametersToTableColumns(parameters, joinTable);
	List<Condition> conditions = tableColumns.stream().map(t -> {
	    return new BinaryCondition.Builder(ConditionType.EQUAL).withLeftColumn(t).withRightExpression(QM).build();
	}).collect(Collectors.toList());

	Condition condition = Condition.toAnd(conditions);
	List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
	List<ColumnNameValue> fetchColumnNameValues = metaEntityHelper.convertAttributes(expandedAttributes);
	return new SqlSelect.SqlSelectBuilder(fromTable).withValues(metaEntityHelper.toValues(entity, fromTable))
		.withFetchParameters(fetchColumnNameValues).withConditions(Arrays.asList(condition))
		.withParameters(parameters).withResult(entity).build();
    }

    public SqlSelect generateSelectByJoinTableFromTarget(MetaEntity entity, MetaAttribute owningId,
	    Object joinTableForeignKey, RelationshipJoinTable relationshipJoinTable) throws Exception {
	// select t1.id, t1.p1 from entity t1 inner join jointable j on t1.id=j.id1
	// where j.t2=fk
	List<MetaAttribute> idAttributes = entity.getId().expand();
	List<Column> idColumns = idAttributes.stream().map(a -> {
	    return new Column(a.getColumnName());
	}).collect(Collectors.toList());

	List<Column> idTargetColumns = relationshipJoinTable.getJoinColumnOwningAttributes().stream().map(a -> {
	    return new Column(a.getColumnName());
	}).collect(Collectors.toList());

	idTargetColumns.stream().forEach(c -> LOG.info("generateSelectByJoinTableFromTarget: c.getName()=" + c.getName()));
	LOG.info("generateSelectByJoinTableFromTarget: owningId=" + owningId);
	LOG.info("generateSelectByJoinTableFromTarget: joinTableForeignKey=" + joinTableForeignKey);
	LOG.info("generateSelectByJoinTableFromTarget: relationshipJoinTable.getTableName()=" + relationshipJoinTable.getTableName());
	FromTable joinTable = new FromTableImpl(relationshipJoinTable.getTableName(), relationshipJoinTable.getAlias());
	FromJoin fromJoin = new FromJoinImpl(joinTable, idColumns, idTargetColumns);
	FromTable fromTable = FromTable.of(entity, fromJoin);
	// handles multiple column pk
	List<JoinColumnAttribute> joinColumnTargetAttributes = relationshipJoinTable.getJoinColumnTargetAttributes();
	List<AttributeValue> owningIdAttributeValues = attributeValueConverter
		.convert(new AttributeValue(owningId, joinTableForeignKey));
	LOG.info("generateSelectByJoinTableFromTarget: owningIdAttributeValues=" + owningIdAttributeValues);

	List<AbstractAttributeValue> attributeValues = new ArrayList<>();
	for (AttributeValue av : owningIdAttributeValues) {
	    int index = AttributeUtil.indexOfJoinColumnAttribute(joinColumnTargetAttributes, av.getAttribute());
	    attributeValues.add(new AbstractAttributeValue(joinColumnTargetAttributes.get(index), av.getValue()));
	}

	List<QueryParameter> parameters = metaEntityHelper.convertAbstractAVToQP(attributeValues);
	List<TableColumn> tableColumns = metaEntityHelper.queryParametersToTableColumns(parameters, joinTable);
	List<Condition> conditions = tableColumns.stream().map(t -> {
	    return new BinaryCondition.Builder(ConditionType.EQUAL).withLeftColumn(t).withRightExpression(QM).build();
	}).collect(Collectors.toList());

	Condition condition = Condition.toAnd(conditions);
	List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
	List<ColumnNameValue> fetchColumnNameValues = metaEntityHelper.convertAttributes(expandedAttributes);
	return new SqlSelect.SqlSelectBuilder(fromTable).withValues(metaEntityHelper.toValues(entity, fromTable))
		.withFetchParameters(fetchColumnNameValues).withConditions(Arrays.asList(condition))
		.withParameters(parameters).withResult(entity).build();
    }

    public SqlInsert generateJoinTableInsert(RelationshipJoinTable relationshipJoinTable, Object owningInstance,
	    Object targetInstance) throws Exception {
	LOG.info("generateJoinTableInsert: owningInstance=" + owningInstance);
	LOG.info("generateJoinTableInsert: targetInstance=" + targetInstance);
	List<QueryParameter> parameters = new ArrayList<>();
	MetaAttribute owningId = relationshipJoinTable.getOwningAttribute();
	parameters
		.addAll(metaEntityHelper.createJoinColumnAVSToQP(relationshipJoinTable.getJoinColumnOwningAttributes(),
			owningId, AttributeUtil.getIdValue(owningId, owningInstance)));
	MetaAttribute targetId = relationshipJoinTable.getTargetAttribute();
	parameters
		.addAll(metaEntityHelper.createJoinColumnAVSToQP(relationshipJoinTable.getJoinColumnTargetAttributes(),
			targetId, AttributeUtil.getIdValue(targetId, targetInstance)));
	List<Column> columns = parameters.stream().map(p -> {
	    return new Column(p.getColumnName());
	}).collect(Collectors.toList());

	return new SqlInsert(new FromTableImpl(relationshipJoinTable.getTableName()), columns, parameters);
    }

    public SqlUpdate generateUpdate(MetaEntity entity, List<AttributeValue> attrValues, Object idValue)
	    throws Exception {
	LOG.info("generateUpdate: attrValues=" + attrValues);
	AttributeValue attrValueId = new AttributeValue(entity.getId(), idValue);
	FromTable fromTable = FromTable.of(entity);
	List<TableColumn> columns = attrValues.stream().map(p -> {
	    return new TableColumn(fromTable, new Column(p.getAttribute().getColumnName()));
	}).collect(Collectors.toList());

	Condition condition = createAttributeEqualCondition(entity, attrValueId);

	attrValues.add(attrValueId);
	List<QueryParameter> parameters = metaEntityHelper.convertAVToQP(attrValues);
	return new SqlUpdate(fromTable, Optional.of(parameters), columns, Optional.of(condition));
    }

    public SqlDelete generateDeleteById(MetaEntity entity, Object idValue) throws Exception {
	AttributeValue attrValueId = new AttributeValue(entity.getId(), idValue);
	FromTable fromTable = FromTable.of(entity);
	List<QueryParameter> parameters = metaEntityHelper.convertAVToQP(attrValueId);
	Condition condition = createAttributeEqualCondition(entity, attrValueId);
	return new SqlDelete(fromTable, Optional.of(parameters), Optional.of(condition));
    }

    private Condition createAttributeEqualCondition(MetaEntity entity, AttributeValue attributeValue) throws Exception {
	List<QueryParameter> parameters = metaEntityHelper.convertAVToQP(attributeValue);
	if (parameters.size() == 1)
	    return new BinaryCondition.Builder(ConditionType.EQUAL)
		    .withLeftColumn(
			    new TableColumn(FromTable.of(entity), new Column(parameters.get(0).getColumnName())))
		    .withRightExpression(QM).build();

	List<Condition> conditions = new ArrayList<>();
	for (QueryParameter parameter : parameters) {
	    BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
		    .withLeftColumn(new TableColumn(FromTable.of(entity), new Column(parameter.getColumnName())))
		    .withRightExpression(QM).build();
	    conditions.add(binaryCondition);
	}

	return new BinaryLogicConditionImpl(ConditionType.AND, conditions);
    }

    private Optional<Value> createSelectionValue(FromTable fromTable, Selection<?> selection) {
	if (selection == null)
	    return Optional.empty();

	if (selection instanceof MiniPath<?>) {
	    MiniPath<?> miniPath = (MiniPath<?>) selection;
	    MetaAttribute metaAttribute = miniPath.getMetaAttribute();
	    return Optional.of(new TableColumn(fromTable, new Column(metaAttribute.getColumnName())));
	} else if (selection instanceof AggregateFunctionExpression<?>) {
	    AggregateFunctionExpression<?> aggregateFunctionExpression = (AggregateFunctionExpression<?>) selection;
	    Expression<?> expr = aggregateFunctionExpression.getX();
	    if (aggregateFunctionExpression.getAggregateFunctionType() == AggregateFunctionType.COUNT) {
		if (expr instanceof MiniPath<?>) {
		    MiniPath<?> miniPath = (MiniPath<?>) expr;
		    MetaAttribute metaAttribute = miniPath.getMetaAttribute();
		    return Optional.of(new Count(new TableColumn(fromTable, new Column(metaAttribute.getColumnName())),
			    aggregateFunctionExpression.isDistinct()));
		} else if (expr instanceof MiniRoot<?>) {
		    MiniRoot<?> miniRoot = (MiniRoot<?>) expr;
		    MetaEntity metaEntity = miniRoot.getMetaEntity();
		    List<MetaAttribute> idAttrs = metaEntity.getId().expand();
		    return Optional.of(
			    new Count(new TableColumn(FromTable.of(metaEntity), new Column(idAttrs.get(0).getColumnName())),
				    aggregateFunctionExpression.isDistinct()));
		}
	    } else if (expr instanceof MiniPath<?>) {
		MiniPath<?> miniPath = (MiniPath<?>) expr;
		MetaAttribute metaAttribute = miniPath.getMetaAttribute();
		return Optional.of(new BasicAggregateFunction(getAggregateFunction(aggregateFunctionExpression.getAggregateFunctionType()), new TableColumn(fromTable, new Column(metaAttribute.getColumnName())), false));
	    }
	} else if (selection instanceof BinaryExpression) {
	    BinaryExpression binaryExpression = (BinaryExpression) selection;
	    SqlBinaryExpressionBuilder builder = new SqlBinaryExpressionBuilder(getSqlExpressionOperator(binaryExpression.getExpressionOperator()));
	    if (binaryExpression.getX().isPresent()) {
		MiniPath<?> miniPath = (MiniPath<?>) binaryExpression.getX().get();
		MetaAttribute metaAttribute = miniPath.getMetaAttribute();
		builder.setLeftTableColumn(new TableColumn(FromTable.of(miniPath.getMetaEntity()), new Column(metaAttribute.getColumnName())));
	    }

	    if (binaryExpression.getxValue().isPresent())
		if (requireQM(binaryExpression.getxValue().get()))
		    builder.setLeftExpression(QM);
		else
		    builder.setLeftExpression(buildValue(binaryExpression.getxValue().get()));

	    if (binaryExpression.getY().isPresent()) {
		MiniPath<?> miniPath = (MiniPath<?>) binaryExpression.getY().get();
		MetaAttribute metaAttribute = miniPath.getMetaAttribute();
		builder.setRightTableColumn(new TableColumn(FromTable.of(miniPath.getMetaEntity()), new Column(metaAttribute.getColumnName())));
	    }

	    if (binaryExpression.getyValue().isPresent())
		if (requireQM(binaryExpression.getyValue().get()))
		    builder.setRightExpression(QM);
		else
		    builder.setRightExpression(buildValue(binaryExpression.getyValue().get()));

	    SqlBinaryExpression sqlBinaryExpression = builder.build();
	    return Optional.of(sqlBinaryExpression);
	}

	return Optional.empty();
    }

    private List<Value> createSelectionValues(FromTable fromTable, Selection<?> selection) {
	if (selection == null)
	    return Collections.emptyList();

	List<Value> values = new ArrayList<>();
	if (selection.isCompoundSelection()) {
	    List<Selection<?>> selections = selection.getCompoundSelectionItems();
	    for (Selection<?> s : selections) {
		Optional<Value> optional = createSelectionValue(fromTable, s);
		if (optional.isPresent())
		    values.add(optional.get());
	    }
	} else {
	    Optional<Value> optional = createSelectionValue(fromTable, selection);
	    if (optional.isPresent())
		values.add(optional.get());
	}

	return values;
    }

    private Optional<ColumnNameValue> createFetchParameter(Selection<?> selection) {
	if (selection == null)
	    return Optional.empty();

	if (selection instanceof MiniPath<?>) {
	    MiniPath<?> miniPath = (MiniPath<?>) selection;
	    MetaAttribute metaAttribute = miniPath.getMetaAttribute();
	    ColumnNameValue columnNameValue = ColumnNameValue.build(metaAttribute);
	    return Optional.of(columnNameValue);
	} else if (selection instanceof AggregateFunctionExpression<?>) {
	    AggregateFunctionExpression<?> aggregateFunctionExpression = (AggregateFunctionExpression<?>) selection;
	    if (aggregateFunctionExpression.getAggregateFunctionType() == AggregateFunctionType.COUNT) {
		ColumnNameValue cnv = new ColumnNameValue("count", null, Long.class, Long.class,
			JdbcTypes.sqlTypeFromClass(Long.class), null, null);
		return Optional.of(cnv);
	    } else if (aggregateFunctionExpression.getAggregateFunctionType() == AggregateFunctionType.AVG) {
		ColumnNameValue cnv = new ColumnNameValue("avg", null, Double.class, Double.class,
			JdbcTypes.sqlTypeFromClass(Double.class), null, null);
		return Optional.of(cnv);
	    } else {
		Expression<?> expr = aggregateFunctionExpression.getX();
		if (expr instanceof MiniPath<?>) {
		    MiniPath<?> miniPath = (MiniPath<?>) expr;
		    MetaAttribute metaAttribute = miniPath.getMetaAttribute();
		    ColumnNameValue columnNameValue = ColumnNameValue.build(metaAttribute);
		    return Optional.of(columnNameValue);
		}
	    }
	} else if (selection instanceof BinaryExpression) {
	    BinaryExpression binaryExpression = (BinaryExpression) selection;
	    MiniPath<?> miniPath = null;
	    if (binaryExpression.getX().isPresent())
		miniPath = (MiniPath<?>) binaryExpression.getX().get();
	    else if (binaryExpression.getY().isPresent())
		miniPath = (MiniPath<?>) binaryExpression.getY().get();

	    if (miniPath == null)
		throw new IllegalArgumentException("Binary expression without data type");

	    MetaAttribute metaAttribute = miniPath.getMetaAttribute();
	    ColumnNameValue columnNameValue = ColumnNameValue.build(metaAttribute);
	    return Optional.of(columnNameValue);
	}

	return Optional.empty();
    }

    private List<ColumnNameValue> createFetchParameters(Selection<?> selection) {
	if (selection == null)
	    return Collections.emptyList();

	List<ColumnNameValue> values = new ArrayList<>();
	if (selection.isCompoundSelection()) {
	    List<Selection<?>> selections = selection.getCompoundSelectionItems();
	    for (Selection<?> s : selections) {
		Optional<ColumnNameValue> optional = createFetchParameter(s);
		if (optional.isPresent())
		    values.add(optional.get());
	    }
	} else {
	    Optional<ColumnNameValue> optional = createFetchParameter(selection);
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

    private void addParameter(ParameterExpression<?> parameterExpression, MetaAttribute attribute,
	    List<QueryParameter> parameters, Query query) {
	Object value = null;
	if (parameterExpression.getName() != null)
	    value = query.getParameterValue(parameterExpression.getName());
	else if (parameterExpression.getPosition() != null)
	    value = query.getParameter(parameterExpression.getPosition());

	QueryParameter queryParameter = new QueryParameter(attribute.getColumnName(), value, attribute.getType(),
		attribute.getSqlType(), attribute.getJdbcAttributeMapper());
	parameters.add(queryParameter);
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

    private TableColumn createTableColumnFromPath(MiniPath<?> miniPath) {
	MetaAttribute attribute1 = miniPath.getMetaAttribute();
	return new TableColumn(FromTable.of(miniPath.getMetaEntity()), new Column(attribute1.getColumnName()));
    }

    private ConditionType getOperator(PredicateType predicateType) {
	switch (predicateType) {
	    case EQUAL:
		return ConditionType.EQUAL;
	    case NOT_EQUAL:
		return ConditionType.NOT_EQUAL;
	    case AND:
		return ConditionType.AND;
	    case IS_FALSE:
		return ConditionType.IS_FALSE;
	    case IS_NOT_NULL:
		return ConditionType.IS_NOT_NULL;
	    case IS_NULL:
		return ConditionType.IS_NULL;
	    case IS_TRUE:
		return ConditionType.IS_TRUE;
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

    private AggregateFunctionBasicType getAggregateFunction(AggregateFunctionType aggregateFunctionType) {
	switch (aggregateFunctionType) {
	    case AVG:
		return AggregateFunctionBasicType.AVG;
	    case MAX:
		return AggregateFunctionBasicType.MAX;
	    case MIN:
		return AggregateFunctionBasicType.MIN;
	    case COUNT:
		return AggregateFunctionBasicType.COUNT;
	    case SUM:
		return AggregateFunctionBasicType.SUM;
	    default:
		break;
	}

	throw new IllegalArgumentException("Unknown aggregate function type for predicate type: " + aggregateFunctionType);
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
	    List<QueryParameter> parameters, Query query) {
	Expression<?> expression1 = comparisonPredicate.getX();
	Expression<?> expression2 = comparisonPredicate.getY();

	ConditionType conditionType = getOperator(comparisonPredicate.getPredicateType());
	BinaryCondition.Builder builder = new BinaryCondition.Builder(conditionType);
	if (expression1 instanceof MiniPath<?>) {
	    MiniPath<?> miniPath = (MiniPath<?>) expression1;
	    MetaAttribute attribute1 = miniPath.getMetaAttribute();
	    TableColumn tableColumn1 = createTableColumnFromPath(miniPath);
	    if (expression2 != null) {
		if (expression2 instanceof MiniPath<?>) {
		    miniPath = (MiniPath<?>) expression2;
		    TableColumn tableColumn2 = createTableColumnFromPath(miniPath);
		    builder.withLeftColumn(tableColumn1).withRightColumn(tableColumn2);
		} else if (expression2 instanceof ParameterExpression<?>) {
		    ParameterExpression<?> parameterExpression = (ParameterExpression<?>) expression2;
		    addParameter(parameterExpression, attribute1, parameters, query);
		    builder.withLeftColumn(tableColumn1).withRightExpression(QM);
		}
	    } else if (comparisonPredicate.getValue() != null)
		if (requireQM(comparisonPredicate.getValue())) {
		    QueryParameter queryParameter = new QueryParameter(attribute1.getColumnName(),
			    comparisonPredicate.getValue(), attribute1.getType(), attribute1.getSqlType(), attribute1.getJdbcAttributeMapper());
		    parameters.add(queryParameter);
		    builder.withLeftColumn(tableColumn1).withRightExpression(QM);
		} else
		    builder.withLeftColumn(tableColumn1)
			    .withRightExpression(buildValue(comparisonPredicate.getValue()));

	    return Optional.of(builder.build());
	}
//			else if (expression1 instanceof ParameterExpression<?>) {
//				ParameterExpression<?> parameterExpression = (ParameterExpression<?>) expression1;
////				MiniPath<?> miniPath = (MiniPath<?>) expression1;
////				MetaAttribute attribute1 = miniPath.getMetaAttribute();
//				addParameter(parameterExpression, attribute1, parameters, query);
//				if (expression2 instanceof MiniPath<?>) {
//					MiniPath<?> miniPath = (MiniPath<?>) expression2;
////					MetaAttribute attribute2 = miniPath.getMetaAttribute();
////					FromTable fromTable2 = FromTable.of(miniPath.getMetaEntity());
////					Column column1 = new Column(attribute2.getColumnName(), fromTable2.getAlias().get());
////					TableColumn tableColumn = new TableColumn(fromTable2, column1);
//					TableColumn tableColumn = createTableColumnFromPath(miniPath);
//					return Optional.of(new EqualExprColumnCondition(QM, tableColumn));
//				} else if (expression2 instanceof ParameterExpression<?>) {
//					parameterExpression = (ParameterExpression<?>) expression2;
//					miniPath = (MiniPath<?>) expression2;
//					MetaAttribute attribute2 = miniPath.getMetaAttribute();
//					addParameter(parameterExpression, attribute2, parameters, query);
//					return Optional.of(new EqualExprExprCondition(QM, QM));
//				}
//			}

	return Optional.empty();
    }

    private Optional<Condition> translateBetweenExpressionsPredicate(
	    BetweenExpressionsPredicate betweenExpressionsPredicate, List<QueryParameter> parameters, Query query) {
	Expression<?> expression1 = betweenExpressionsPredicate.getX();
	Expression<?> expression2 = betweenExpressionsPredicate.getY();
	MiniPath<?> miniPath = (MiniPath<?>) betweenExpressionsPredicate.getV();
	MetaAttribute attribute = miniPath.getMetaAttribute();

	BetweenCondition.Builder builder = new BetweenCondition.Builder(createTableColumnFromPath(miniPath));
	if (expression1 instanceof MiniPath<?>)
	    builder.withLeftColumn(createTableColumnFromPath((MiniPath<?>) expression1));
	else if (expression1 instanceof ParameterExpression<?>) {
	    ParameterExpression<?> parameterExpression = (ParameterExpression<?>) expression1;
	    addParameter(parameterExpression, attribute, parameters, query);
	    builder.withLeftExpression(QM);
	}

	if (expression2 instanceof MiniPath<?>)
	    builder.withRightColumn(createTableColumnFromPath((MiniPath<?>) expression2));
	else if (expression2 instanceof ParameterExpression<?>) {
	    ParameterExpression<?> parameterExpression = (ParameterExpression<?>) expression2;
	    addParameter(parameterExpression, attribute, parameters, query);
	    builder.withLeftExpression(QM);
	}

	return Optional.of(builder.build());
    }

    private Optional<Condition> translateBetweenValuesPredicate(BetweenValuesPredicate betweenValuesPredicate,
	    List<QueryParameter> parameters) {
	Object x = betweenValuesPredicate.getX();
	Object y = betweenValuesPredicate.getY();
	MiniPath<?> miniPath = (MiniPath<?>) betweenValuesPredicate.getV();
	MetaAttribute attribute = miniPath.getMetaAttribute();

	BetweenCondition.Builder builder = new BetweenCondition.Builder(createTableColumnFromPath(miniPath));
	if (requireQM(x)) {
	    QueryParameter queryParameter = new QueryParameter(attribute.getColumnName(), x, attribute.getType(),
		    attribute.getSqlType(), attribute.getJdbcAttributeMapper());
	    parameters.add(queryParameter);
	    builder.withLeftExpression(QM);
	} else
	    builder.withLeftExpression(buildValue(x));

	if (requireQM(y)) {
	    QueryParameter queryParameter = new QueryParameter(attribute.getColumnName(), y, attribute.getType(),
		    attribute.getSqlType(), attribute.getJdbcAttributeMapper());
	    parameters.add(queryParameter);
	    builder.withRightExpression(QM);
	} else
	    builder.withRightExpression(buildValue(y));

	return Optional.of(builder.build());
    }

    private Optional<Condition> translateBooleanExprPredicate(BooleanExprPredicate booleanExprPredicate,
	    List<QueryParameter> parameters, Query query) {
	Expression<Boolean> x = booleanExprPredicate.getX();

	if (x instanceof Predicate) {
	    Optional<Condition> optional = createConditions((Predicate) x, parameters, query);
	    if (optional.isPresent())
		return Optional.of(new UnaryLogicConditionImpl(getOperator(booleanExprPredicate.getPredicateType()),
			optional.get()));
	}

	if (x instanceof MiniPath<?>) {
	    MiniPath<?> miniPath = (MiniPath<?>) x;
	    return Optional.of(new UnaryCondition(getOperator(booleanExprPredicate.getPredicateType()),
		    createTableColumnFromPath(miniPath)));
	}

	return Optional.empty();
    }

    private Optional<Condition> translateExprPredicate(ExprPredicate exprPredicate, List<QueryParameter> parameters,
	    Query query) {
	Expression<?> x = exprPredicate.getX();

	if (x instanceof MiniPath<?>) {
	    MiniPath<?> miniPath = (MiniPath<?>) x;
	    return Optional.of(new UnaryCondition(getOperator(exprPredicate.getPredicateType()),
		    createTableColumnFromPath(miniPath)));
	}

	return Optional.empty();
    }

    private Optional<Condition> translateMultiplePredicate(MultiplePredicate multiplePredicate,
	    List<QueryParameter> parameters, Query query) {
	Predicate[] predicates = multiplePredicate.getRestrictions();
	List<Condition> conditions = new ArrayList<>();
	LOG.info("translateMultiplePredicate: conditions.size()=" + conditions.size());
	for (Predicate p : predicates) {
	    Optional<Condition> optional = createConditions(p, parameters, query);
	    if (optional.isPresent())
		conditions.add(optional.get());
	}

	if (!conditions.isEmpty())
	    return Optional.of(
		    new BinaryLogicConditionImpl(getOperator(multiplePredicate.getPredicateType()), conditions, true));

	return Optional.empty();
    }

    private Optional<Condition> translateBinaryBooleanExprPredicate(
	    BinaryBooleanExprPredicate binaryBooleanExprPredicate, List<QueryParameter> parameters, Query query) {
	Expression<Boolean> x = binaryBooleanExprPredicate.getX();
	Expression<Boolean> y = binaryBooleanExprPredicate.getY();

	LOG.info("translateBinaryBooleanExprPredicate: x=" + x);
	LOG.info("translateBinaryBooleanExprPredicate: y=" + y);
	List<Condition> conditions = new ArrayList<>();
	if (x instanceof Predicate) {
	    Optional<Condition> optional = createConditions((Predicate) x, parameters, query);
	    if (optional.isPresent())
		conditions.add(optional.get());
	}

	if (y instanceof Predicate) {
	    Optional<Condition> optional = createConditions((Predicate) y, parameters, query);
	    if (optional.isPresent())
		conditions.add(optional.get());
	}

	if (!conditions.isEmpty())
	    return Optional.of(new BinaryLogicConditionImpl(getOperator(binaryBooleanExprPredicate.getPredicateType()),
		    conditions, true));

	return Optional.empty();
    }

    private Optional<Condition> translateLikePatternPredicate(LikePatternPredicate likePatternPredicate, Query query) {
	String pattern = likePatternPredicate.getPattern();
	MiniPath<?> miniPath = (MiniPath<?>) likePatternPredicate.getX();
	BinaryCondition.Builder builder = new BinaryCondition.Builder(ConditionType.LIKE)
		.withLeftColumn(createTableColumnFromPath(miniPath)).withRightExpression(buildValue(pattern));
	if (likePatternPredicate.isNot())
	    builder.not();

	return Optional.of(builder.build());
    }

    private Optional<Condition> translateInPredicate(InPredicate<?> inPredicate, List<QueryParameter> parameters,
	    Query query) {
	Expression<?> expression = inPredicate.getExpression();
	LOG.info("translateInPredicate: expression=" + expression);
	TableColumn tableColumn = null;
	if (expression instanceof MiniPath<?>) {
	    MiniPath<?> miniPath = (MiniPath<?>) expression;
	    MetaAttribute attribute = miniPath.getMetaAttribute();
	    tableColumn = createTableColumnFromPath(miniPath);

	    List<String> list = inPredicate.getValues().stream().map(v -> {
		return QM;
	    }).collect(Collectors.toList());

	    List<QueryParameter> queryParameters = inPredicate.getValues().stream().map(v -> {
		return new QueryParameter(attribute.getColumnName(), v, attribute.getType(), attribute.getSqlType(), attribute.getJdbcAttributeMapper());
	    }).collect(Collectors.toList());
	    parameters.addAll(queryParameters);

	    if (inPredicate.isNot())
		return Optional.of(new InCondition(tableColumn, list, true));

	    return Optional.of(new InCondition(tableColumn, list));
	}

	return Optional.empty();
    }

    private Optional<Condition> translateLikePatternExprPredicate(LikePatternExprPredicate likePatternExprPredicate,
	    List<QueryParameter> parameters, Query query) {
	Expression<String> pattern = likePatternExprPredicate.getPatternEx();
	MiniPath<?> miniPath = (MiniPath<?>) likePatternExprPredicate.getX();
	MetaAttribute attribute = miniPath.getMetaAttribute();
	if (pattern instanceof ParameterExpression<?>) {
	    ParameterExpression<?> parameterExpression = (ParameterExpression<?>) pattern;
	    addParameter(parameterExpression, attribute, parameters, query);
	}

	BinaryCondition.Builder builder = new BinaryCondition.Builder(ConditionType.LIKE)
		.withLeftColumn(createTableColumnFromPath(miniPath)).withRightExpression(buildValue(QM));
	if (likePatternExprPredicate.isNot())
	    builder.not();

	return Optional.of(builder.build());
    }

    private Optional<Condition> createConditions(Predicate predicate, List<QueryParameter> parameters, Query query) {
	PredicateTypeInfo predicateTypeInfo = (PredicateTypeInfo) predicate;
	PredicateType predicateType = predicateTypeInfo.getPredicateType();
	if (predicateType == PredicateType.EQUAL || predicateType == PredicateType.NOT_EQUAL
		|| predicateType == PredicateType.GREATER_THAN
		|| predicateType == PredicateType.GREATER_THAN_OR_EQUAL_TO || predicateType == PredicateType.GT
		|| predicateType == PredicateType.LESS_THAN || predicateType == PredicateType.LESS_THAN_OR_EQUAL_TO
		|| predicateType == PredicateType.LT)
	    return translateComparisonPredicate((ComparisonPredicate) predicate, parameters, query);
	else if (predicateType == PredicateType.BETWEEN_EXPRESSIONS) {
	    BetweenExpressionsPredicate betweenExpressionsPredicate = (BetweenExpressionsPredicate) predicate;
	    return translateBetweenExpressionsPredicate(betweenExpressionsPredicate, parameters, query);
	} else if (predicateType == PredicateType.BETWEEN_VALUES) {
	    BetweenValuesPredicate betweenValuesPredicate = (BetweenValuesPredicate) predicate;
	    return translateBetweenValuesPredicate(betweenValuesPredicate, parameters);
	} else if (predicateType == PredicateType.LIKE_PATTERN) {
	    LikePatternPredicate likePatternPredicate = (LikePatternPredicate) predicate;
	    return translateLikePatternPredicate(likePatternPredicate, query);
	} else if (predicateType == PredicateType.LIKE_PATTERN_EXPR) {
	    LikePatternExprPredicate likePatternExprPredicate = (LikePatternExprPredicate) predicate;
	    return translateLikePatternExprPredicate(likePatternExprPredicate, parameters, query);
	} else if (predicateType == PredicateType.OR || predicateType == PredicateType.AND)
	    if (predicate instanceof MultiplePredicate)
		return translateMultiplePredicate((MultiplePredicate) predicate, parameters, query);
	    else
		return translateBinaryBooleanExprPredicate((BinaryBooleanExprPredicate) predicate, parameters, query);
	else if (predicateType == PredicateType.NOT)
	    return translateBooleanExprPredicate((BooleanExprPredicate) predicate, parameters, query);
	else if (predicateType == PredicateType.IS_NULL || predicateType == PredicateType.IS_NOT_NULL)
	    return translateExprPredicate((ExprPredicate) predicate, parameters, query);
	else if (predicateType == PredicateType.IS_TRUE || predicateType == PredicateType.IS_FALSE)
	    return translateBooleanExprPredicate((BooleanExprPredicate) predicate, parameters, query);
	else if (predicateType == PredicateType.EMPTY_CONJUNCTION
		|| predicateType == PredicateType.EMPTY_DISJUNCTION)
	    return Optional.of(new EmptyCondition(getOperator(predicateType)));
	else if (predicateType == PredicateType.IN)
	    return translateInPredicate((InPredicate<?>) predicate, parameters, query);

	return Optional.empty();
    }

    private Optional<List<OrderBy>> createOrderByList(CriteriaQuery<?> criteriaQuery) {
	if (criteriaQuery.getOrderList() == null || criteriaQuery.getOrderList().isEmpty())
	    return Optional.empty();

	List<OrderBy> result = new ArrayList<>();
	List<Order> orders = criteriaQuery.getOrderList();
	for (Order order : orders) {
	    Expression<?> expression = order.getExpression();
	    if (expression instanceof MiniPath<?>) {
		MiniPath<?> miniPath = (MiniPath<?>) expression;
		MetaAttribute metaAttribute = miniPath.getMetaAttribute();
		TableColumn tableColumn = new TableColumn(FromTable.of(miniPath.getMetaEntity()),
			new Column(metaAttribute.getColumnName()));
		OrderBy orderBy = new OrderBy(tableColumn, order.isAscending());
		result.add(orderBy);
	    }
	}

	if (result.isEmpty())
	    return Optional.empty();

	return Optional.of(result);
    }

    public SqlSelect select(Query query) {
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
	    optionalCondition = createConditions(restriction, parameters, query);

	List<Condition> conditions = null;
	if (optionalCondition.isPresent())
	    conditions = Arrays.asList(optionalCondition.get());

	Selection<?> selection = criteriaQuery.getSelection();
	if (selection instanceof MiniRoot<?>) {
	    List<ColumnNameValue> fetchColumnNameValues = metaEntityHelper.convertAllAttributes(entity);

	    FromTable fromTable = FromTable.of(entity);
	    return new SqlSelect.SqlSelectBuilder(fromTable).withValues(metaEntityHelper.toValues(entity, fromTable))
		    .withFetchParameters(fetchColumnNameValues).withConditions(conditions).withParameters(parameters)
		    .withResult(entity).build();
	}

	FromTable fromTable = FromTable.of(entity);
	List<Value> values = createSelectionValues(fromTable, selection);
	List<ColumnNameValue> fetchParameters = createFetchParameters(selection);
	Optional<List<OrderBy>> optionalOrderBy = createOrderByList(criteriaQuery);

	SqlSelect.SqlSelectBuilder builder = new SqlSelect.SqlSelectBuilder(fromTable).withValues(values)
		.withFetchParameters(fetchParameters).withConditions(conditions).withParameters(parameters);

	if (optionalOrderBy.isPresent())
	    builder.withOrderBy(optionalOrderBy.get());

	if (criteriaQuery.isDistinct())
	    builder.distinct();

	return builder.build();
    }

    private QueryParameter createQueryParameter(MiniPath<?> miniPath, Object value) {
	MetaAttribute a = miniPath.getMetaAttribute();
	return new QueryParameter(a.getColumnName(), value, a.getType(), a.getSqlType(), a.getJdbcAttributeMapper());
    }

    public SqlUpdate update(Query query) {
	CriteriaUpdate<?> criteriaUpdate = ((UpdateQuery) query).getCriteriaUpdate();
	MetaEntity entity = ((MiniRoot<?>) criteriaUpdate.getRoot()).getMetaEntity();

	FromTable fromTable = FromTable.of(entity);
	List<QueryParameter> parameters = new ArrayList<>();
	List<TableColumn> columns = new ArrayList<>();
	Map<Path<?>, Object> setValues = ((MiniCriteriaUpdate) criteriaUpdate).getSetValues();
	setValues.forEach((k, v) -> {
	    MiniPath<?> miniPath = (MiniPath<?>) k;
	    QueryParameter qp = createQueryParameter(miniPath, v);
	    parameters.add(qp);
	    TableColumn tableColumn = new TableColumn(fromTable,
		    new Column(miniPath.getMetaAttribute().getColumnName()));
	    columns.add(tableColumn);
	});

	Predicate restriction = criteriaUpdate.getRestriction();
	Optional<Condition> optionalCondition = Optional.empty();
	if (restriction != null)
	    optionalCondition = createConditions(restriction, parameters, query);

	return new SqlUpdate(fromTable, Optional.of(parameters), columns, optionalCondition);
    }

    public SqlDelete delete(Query query) {
	CriteriaDelete<?> criteriaDelete = ((DeleteQuery) query).getCriteriaDelete();
	MetaEntity entity = ((MiniRoot<?>) criteriaDelete.getRoot()).getMetaEntity();

	FromTable fromTable = FromTable.of(entity);
	List<QueryParameter> parameters = new ArrayList<>();

	Predicate restriction = criteriaDelete.getRestriction();
	Optional<Condition> optionalCondition = Optional.empty();
	if (restriction != null)
	    optionalCondition = createConditions(restriction, parameters, query);

	return new SqlDelete(fromTable, Optional.of(parameters), optionalCondition);
    }
}
