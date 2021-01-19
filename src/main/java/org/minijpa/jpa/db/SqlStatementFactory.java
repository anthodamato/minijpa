package org.minijpa.jpa.db;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
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
import org.minijpa.jdbc.model.aggregate.Count;
import org.minijpa.jdbc.model.aggregate.Max;
import org.minijpa.jdbc.model.aggregate.Min;
import org.minijpa.jdbc.model.condition.BetweenCondition;
import org.minijpa.jdbc.model.condition.BinaryCondition;
import org.minijpa.jdbc.model.condition.BinaryLogicConditionImpl;
import org.minijpa.jdbc.model.condition.Condition;
import org.minijpa.jdbc.model.condition.ConditionType;
import org.minijpa.jdbc.model.condition.EmptyCondition;
import org.minijpa.jdbc.model.condition.UnaryCondition;
import org.minijpa.jdbc.model.condition.UnaryLogicConditionImpl;
import org.minijpa.jdbc.model.join.FromJoin;
import org.minijpa.jdbc.model.join.FromJoinImpl;
import org.minijpa.jdbc.relationship.RelationshipJoinTable;
import org.minijpa.jpa.MiniTypedQuery;
import org.minijpa.jpa.criteria.BetweenExpressionsPredicate;
import org.minijpa.jpa.criteria.BetweenValuesPredicate;
import org.minijpa.jpa.criteria.BinaryBooleanExprPredicate;
import org.minijpa.jpa.criteria.BooleanExprPredicate;
import org.minijpa.jpa.criteria.ComparisonPredicate;
import org.minijpa.jpa.criteria.CountExpression;
import org.minijpa.jpa.criteria.ExprPredicate;
import org.minijpa.jpa.criteria.LikePatternExprPredicate;
import org.minijpa.jpa.criteria.LikePatternPredicate;
import org.minijpa.jpa.criteria.MaxExpression;
import org.minijpa.jpa.criteria.MinExpression;
import org.minijpa.jpa.criteria.MiniPath;
import org.minijpa.jpa.criteria.MiniRoot;
import org.minijpa.jpa.criteria.MultiplePredicate;
import org.minijpa.jpa.criteria.PredicateType;
import org.minijpa.jpa.criteria.PredicateTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlStatementFactory {
	public static final String QM = "?";

	private Logger LOG = LoggerFactory.getLogger(SqlStatementFactory.class);
	private AttributeValueConverter attributeValueConverter = new EmbeddedIdAttributeValueConverter();
	private MetaEntityHelper metaEntityHelper = new MetaEntityHelper();

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
		List<AttributeValue> attributeValues = new ArrayList<>();
		AttributeValue attrValue = new AttributeValue(foreignKeyAttribute, foreignKeyInstance);
		attributeValues.add(attrValue);
		List<ColumnNameValue> fetchColumnNameValues = metaEntityHelper.convertAllAttributes(entity);

		List<QueryParameter> parameters = metaEntityHelper.convertAVToQP(attrValue);
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
		int index = -1;
		for (AttributeValue av : owningIdAttributeValues) {
			index = AttributeUtil.indexOfJoinColumnAttribute(joinColumnOwningAttributes, av.getAttribute());
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
		if (parameters.size() == 1) {
			return new BinaryCondition.Builder(ConditionType.EQUAL)
					.withLeftColumn(
							new TableColumn(FromTable.of(entity), new Column(parameters.get(0).getColumnName())))
					.withRightExpression(QM).build();
		}

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
		} else if (selection instanceof MaxExpression<?>) {
			@SuppressWarnings("unchecked")
			MaxExpression<Number> maxExpression = (MaxExpression<Number>) selection;
			Expression<Number> expr = maxExpression.getX();
			if (expr instanceof MiniPath<?>) {
				MiniPath<?> miniPath = (MiniPath<?>) expr;
				MetaAttribute metaAttribute = miniPath.getMetaAttribute();
				return Optional.of(new Max(new TableColumn(fromTable, new Column(metaAttribute.getColumnName()))));
			}
		} else if (selection instanceof MinExpression<?>) {
			@SuppressWarnings("unchecked")
			MinExpression<Number> minExpression = (MinExpression<Number>) selection;
			Expression<Number> expr = minExpression.getX();
			if (expr instanceof MiniPath<?>) {
				MiniPath<?> miniPath = (MiniPath<?>) expr;
				MetaAttribute metaAttribute = miniPath.getMetaAttribute();
				return Optional.of(new Min(new TableColumn(fromTable, new Column(metaAttribute.getColumnName()))));
			}
		} else if (selection instanceof CountExpression) {
			CountExpression count = (CountExpression) selection;
			Expression<?> expr = count.getExpression();
			if (expr instanceof MiniPath<?>) {
				MiniPath<?> miniPath = (MiniPath<?>) expr;
				MetaAttribute metaAttribute = miniPath.getMetaAttribute();
				return Optional.of(new Count(new TableColumn(fromTable, new Column(metaAttribute.getColumnName())),
						count.isDistinct()));
			} else if (expr instanceof MiniRoot<?>) {
				MiniRoot<?> miniRoot = (MiniRoot<?>) expr;
				MetaEntity metaEntity = miniRoot.getMetaEntity();
				List<MetaAttribute> idAttrs = metaEntity.getId().expand();
				return Optional.of(
						new Count(new TableColumn(FromTable.of(metaEntity), new Column(idAttrs.get(0).getColumnName())),
								count.isDistinct()));
			}
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
		} else if (selection instanceof MaxExpression<?>) {
			@SuppressWarnings("unchecked")
			MaxExpression<Number> maxExpression = (MaxExpression<Number>) selection;
			Expression<Number> expr = maxExpression.getX();
			if (expr instanceof MiniPath<?>) {
				MiniPath<?> miniPath = (MiniPath<?>) expr;
				MetaAttribute metaAttribute = miniPath.getMetaAttribute();
				ColumnNameValue columnNameValue = ColumnNameValue.build(metaAttribute);
				return Optional.of(columnNameValue);
			}
		} else if (selection instanceof MinExpression<?>) {
			@SuppressWarnings("unchecked")
			MinExpression<Number> minExpression = (MinExpression<Number>) selection;
			Expression<Number> expr = minExpression.getX();
			if (expr instanceof MiniPath<?>) {
				MiniPath<?> miniPath = (MiniPath<?>) expr;
				MetaAttribute metaAttribute = miniPath.getMetaAttribute();
				ColumnNameValue columnNameValue = ColumnNameValue.build(metaAttribute);
				return Optional.of(columnNameValue);
			}
		} else if (selection instanceof CountExpression) {
			ColumnNameValue cnv = new ColumnNameValue("count", null, Long.class, Long.class,
					JdbcTypes.sqlTypeFromClass(Long.class), null, null);
			return Optional.of(cnv);
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
		if (parameterExpression.getName() != null) {
			value = query.getParameterValue(parameterExpression.getName());
		}

		QueryParameter queryParameter = new QueryParameter(attribute.getColumnName(), value, attribute.getType(),
				attribute.getSqlType());
		parameters.add(queryParameter);
	}

	private String buildValue(Object value) {
		StringBuilder sb = new StringBuilder();
		if (value instanceof String) {
			sb.append("'");
			sb.append((String) value);
			sb.append("'");
		} else {
			sb.append(value.toString());
		}

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
		case LESS_THAN:
		case LT:
			return ConditionType.LESS_THAN;
		case BETWEEN_EXPRESSIONS:
		case BETWEEN_VALUES:
			return ConditionType.BETWEEN;
		case LIKE_PATTERN:
		case LIKE_PATTERN_EXPR:
			return ConditionType.LIKE;
		default:
			break;
		}

		throw new IllegalArgumentException("Unknown condition type for predicate type: " + predicateType);
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
			} else if (comparisonPredicate.getValue() != null) {
				if (requireQM(comparisonPredicate.getValue())) {
					QueryParameter queryParameter = new QueryParameter(attribute1.getColumnName(),
							comparisonPredicate.getValue(), attribute1.getType(), attribute1.getSqlType());
					parameters.add(queryParameter);
					builder.withLeftColumn(tableColumn1).withRightExpression(QM);
				} else {
					builder.withLeftColumn(tableColumn1)
							.withRightExpression(buildValue(comparisonPredicate.getValue()));
				}
			}

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
		if (expression1 instanceof MiniPath<?>) {
			builder.withLeftColumn(createTableColumnFromPath((MiniPath<?>) expression1));
		} else if (expression1 instanceof ParameterExpression<?>) {
			ParameterExpression<?> parameterExpression = (ParameterExpression<?>) expression1;
			addParameter(parameterExpression, attribute, parameters, query);
			builder.withLeftExpression(QM);
		}

		if (expression2 instanceof MiniPath<?>) {
			builder.withRightColumn(createTableColumnFromPath((MiniPath<?>) expression2));
		} else if (expression2 instanceof ParameterExpression<?>) {
			ParameterExpression<?> parameterExpression = (ParameterExpression<?>) expression2;
			addParameter(parameterExpression, attribute, parameters, query);
			builder.withLeftExpression(QM);
		}

		return Optional.of(builder.build());
	}

	private Optional<Condition> translateBetweenValuesPredicate(BetweenValuesPredicate betweenValuesPredicate,
			List<QueryParameter> parameters, Query query) {
		Object x = betweenValuesPredicate.getX();
		Object y = betweenValuesPredicate.getY();
		MiniPath<?> miniPath = (MiniPath<?>) betweenValuesPredicate.getV();
		MetaAttribute attribute = miniPath.getMetaAttribute();

		BetweenCondition.Builder builder = new BetweenCondition.Builder(createTableColumnFromPath(miniPath));
		if (requireQM(x)) {
			QueryParameter queryParameter = new QueryParameter(attribute.getColumnName(), x, attribute.getType(),
					attribute.getSqlType());
			parameters.add(queryParameter);
			builder.withLeftExpression(QM);
		} else {
			builder.withLeftExpression(buildValue(x));
		}

		if (requireQM(y)) {
			QueryParameter queryParameter = new QueryParameter(attribute.getColumnName(), y, attribute.getType(),
					attribute.getSqlType());
			parameters.add(queryParameter);
			builder.withRightExpression(QM);
		} else {
			builder.withRightExpression(buildValue(y));
		}

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
		return Optional.of(new BinaryCondition.Builder(ConditionType.LIKE)
				.withLeftColumn(createTableColumnFromPath(miniPath)).withRightExpression(buildValue(pattern)).build());
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

		return Optional.of(new BinaryCondition.Builder(ConditionType.LIKE)
				.withLeftColumn(createTableColumnFromPath(miniPath)).withRightExpression(buildValue(QM)).build());
	}

	private Optional<Condition> createConditions(Predicate predicate, List<QueryParameter> parameters, Query query) {
		PredicateTypeInfo predicateTypeInfo = (PredicateTypeInfo) predicate;
		PredicateType predicateType = predicateTypeInfo.getPredicateType();
		if (predicateType == PredicateType.EQUAL || predicateType == PredicateType.NOT_EQUAL
				|| predicateType == PredicateType.GREATER_THAN || predicateType == PredicateType.GT
				|| predicateType == PredicateType.LESS_THAN || predicateType == PredicateType.LT) {
			return translateComparisonPredicate((ComparisonPredicate) predicate, parameters, query);
		} else if (predicateType == PredicateType.BETWEEN_EXPRESSIONS) {
			BetweenExpressionsPredicate betweenExpressionsPredicate = (BetweenExpressionsPredicate) predicate;
			return translateBetweenExpressionsPredicate(betweenExpressionsPredicate, parameters, query);
		} else if (predicateType == PredicateType.BETWEEN_VALUES) {
			BetweenValuesPredicate betweenValuesPredicate = (BetweenValuesPredicate) predicate;
			return translateBetweenValuesPredicate(betweenValuesPredicate, parameters, query);
		} else if (predicateType == PredicateType.LIKE_PATTERN) {
			LikePatternPredicate likePatternPredicate = (LikePatternPredicate) predicate;
			return translateLikePatternPredicate(likePatternPredicate, query);
		} else if (predicateType == PredicateType.LIKE_PATTERN_EXPR) {
			LikePatternExprPredicate likePatternExprPredicate = (LikePatternExprPredicate) predicate;
			return translateLikePatternExprPredicate(likePatternExprPredicate, parameters, query);
		} else if (predicateType == PredicateType.OR || predicateType == PredicateType.AND) {
			if (predicate instanceof MultiplePredicate) {
				return translateMultiplePredicate((MultiplePredicate) predicate, parameters, query);
			} else {
				return translateBinaryBooleanExprPredicate((BinaryBooleanExprPredicate) predicate, parameters, query);
			}
		} else if (predicateType == PredicateType.NOT) {
			return translateBooleanExprPredicate((BooleanExprPredicate) predicate, parameters, query);
		} else if (predicateType == PredicateType.IS_NULL || predicateType == PredicateType.IS_NOT_NULL) {
			return translateExprPredicate((ExprPredicate) predicate, parameters, query);
		} else if (predicateType == PredicateType.IS_TRUE || predicateType == PredicateType.IS_FALSE) {
			return translateBooleanExprPredicate((BooleanExprPredicate) predicate, parameters, query);
		} else if (predicateType == PredicateType.EMPTY_CONJUNCTION
				|| predicateType == PredicateType.EMPTY_DISJUNCTION) {
			return Optional.of(new EmptyCondition(getOperator(predicateType)));
		}

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
		if (restriction != null) {
			optionalCondition = createConditions(restriction, parameters, query);
		}

		List<Condition> conditions = null;
		if (optionalCondition.isPresent())
			conditions = Arrays.asList(optionalCondition.get());

		Selection<?> selection = criteriaQuery.getSelection();
		LOG.info("select: selection=" + selection);
		if (selection == null || selection instanceof MiniRoot<?>) {
			entity = ((MiniRoot<?>) root).getMetaEntity();
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
}
