package org.tinyjpa.jpa.db;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.ColumnNameValue;
import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.db.DbJdbc;
import org.tinyjpa.jdbc.db.StatementData;
import org.tinyjpa.jdbc.model.Column;
import org.tinyjpa.jdbc.model.FromTable;
import org.tinyjpa.jdbc.model.SqlDelete;
import org.tinyjpa.jdbc.model.SqlInsert;
import org.tinyjpa.jdbc.model.SqlSelect;
import org.tinyjpa.jdbc.model.SqlUpdate;
import org.tinyjpa.jdbc.model.TableColumn;
import org.tinyjpa.jdbc.model.aggregate.AggregateFunction;
import org.tinyjpa.jdbc.model.aggregate.Count;
import org.tinyjpa.jdbc.model.aggregate.Distinct;
import org.tinyjpa.jdbc.model.aggregate.GroupBy;
import org.tinyjpa.jdbc.model.aggregate.Sum;
import org.tinyjpa.jdbc.model.condition.AndCondition;
import org.tinyjpa.jdbc.model.condition.Condition;
import org.tinyjpa.jdbc.model.condition.EqualColumnExprCondition;
import org.tinyjpa.jdbc.model.condition.EqualColumnsCondition;
import org.tinyjpa.jdbc.model.condition.LikeCondition;
import org.tinyjpa.jdbc.model.condition.OrCondition;
import org.tinyjpa.jdbc.model.join.FromJoin;
import org.tinyjpa.jdbc.model.join.JoinType;
import org.tinyjpa.jpa.MiniTypedQuery;
import org.tinyjpa.jpa.criteria.BetweenExpressionsPredicate;
import org.tinyjpa.jpa.criteria.BetweenValuesPredicate;
import org.tinyjpa.jpa.criteria.BinaryBooleanExprPredicate;
import org.tinyjpa.jpa.criteria.BooleanExprPredicate;
import org.tinyjpa.jpa.criteria.ComparisonPredicate;
import org.tinyjpa.jpa.criteria.EmptyPredicate;
import org.tinyjpa.jpa.criteria.ExprPredicate;
import org.tinyjpa.jpa.criteria.LikePatternExprPredicate;
import org.tinyjpa.jpa.criteria.LikePatternPredicate;
import org.tinyjpa.jpa.criteria.MaxExpression;
import org.tinyjpa.jpa.criteria.MinExpression;
import org.tinyjpa.jpa.criteria.MiniPath;
import org.tinyjpa.jpa.criteria.MultiplePredicate;
import org.tinyjpa.jpa.criteria.PredicateType;
import org.tinyjpa.jpa.criteria.PredicateTypeInfo;

public class SqlStatementGenerator {
	private Logger LOG = LoggerFactory.getLogger(SqlStatementGenerator.class);

	private DbJdbc dbJdbc;

	public SqlStatementGenerator(DbJdbc dbJdbc) {
		super();
		this.dbJdbc = dbJdbc;
	}

	public String generate(SqlInsert sqlInsert) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(sqlInsert.getTableName());
		sb.append(" (");
		String cols = sqlInsert.getColumnNameValues().stream().map(a -> a.getColumnName())
				.collect(Collectors.joining(","));
		sb.append(cols);
		sb.append(") values (");

		for (int i = 0; i < sqlInsert.getColumnNameValues().size(); ++i) {
			if (i > 0)
				sb.append(",");

			sb.append("?");
		}

		sb.append(")");
		return sb.toString();
	}

	public String generate(SqlUpdate sqlUpdate) {
		StringBuilder sb = new StringBuilder();
		sb.append("update ");
		sb.append(sqlUpdate.getTableName());
		sb.append(" set ");
		int i = 0;
		for (ColumnNameValue columnNameValue : sqlUpdate.getColumnNameValues()) {
			if (columnNameValue.getAttribute().isId())
				continue;

			if (i > 0)
				sb.append(",");

			sb.append(columnNameValue.getAttribute().getColumnName());
			sb.append(" = ?");
			++i;
		}

		sb.append(" where ");
		i = 0;
		for (ColumnNameValue columnNameValue : sqlUpdate.getColumnNameValues()) {
			if (!columnNameValue.getAttribute().isId())
				continue;

			if (i > 0)
				sb.append(" and ");

			sb.append(columnNameValue.getAttribute().getColumnName());
			sb.append(" = ?");
			++i;
		}

		return sb.toString();
	}

	public String generate(SqlDelete sqlDelete) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ");
		sb.append(sqlDelete.getTableName());
		sb.append(" where ");

		int i = 0;
		for (ColumnNameValue columnNameValue : sqlDelete.getColumnNameValues()) {
			if (i > 0)
				sb.append(" and ");

			sb.append(columnNameValue.getAttribute().getColumnName());
			sb.append(" = ?");
			++i;
		}

		return sb.toString();
	}

	private List<String> createColumns(List<ColumnNameValue> columnNameValues) {
		List<String> list = new ArrayList<>();
		for (ColumnNameValue a : columnNameValues) {
			MetaAttribute m = a.getAttribute();
			if (m != null && m.getRelationship() != null) {
				if (m.getRelationship().getJoinColumn() != null)
					list.add(m.getRelationship().getJoinColumn());
			} else {
				list.add(a.getColumnName());
			}
		}

		return list;
	}

	public String generateSql(SqlSelect sqlSelect) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		List<String> columns = createColumns(sqlSelect.getFetchColumnNameValues());
		int i = 0;
		for (String c : columns) {
			if (i > 0)
				sb.append(", ");

			sb.append(c);
			++i;
		}

		sb.append(" from ");
		sb.append(sqlSelect.getFromTable().getName());
		if (sqlSelect.getColumnNameValues().isEmpty())
			return sb.toString();

		sb.append(" where ");

		i = 0;
		for (ColumnNameValue cnv : sqlSelect.getColumnNameValues()) {
			if (i > 0)
				sb.append(" and ");

			sb.append(cnv.getColumnName());
			sb.append(" = ?");
			++i;
		}

		return sb.toString();
	}

	private String exportColumn(Column column) {
		if (column.getAlias().isPresent())
			return column.getName() + " AS " + column.getAlias().get();

		return column.getName();
	}

	private String exportTableColumn(TableColumn tableColumn) {
		if (tableColumn.getTable().isPresent() && tableColumn.getTable().get().getAlias().isPresent())
			return tableColumn.getTable().get().getAlias().get() + "." + exportColumn(tableColumn.getColumn());

		if (tableColumn.getSubQuery().isPresent() && tableColumn.getSubQuery().get().getAlias().isPresent())
			return tableColumn.getSubQuery().get().getAlias().get() + "." + exportColumn(tableColumn.getColumn());

		return exportColumn(tableColumn.getColumn());
	}

	private String exportAggregateFunction(AggregateFunction aggregateFunction) {
		if (aggregateFunction instanceof Sum)
			return "sum(" + exportTableColumn(((Sum) aggregateFunction).getTableColumn()) + ")";

		if (aggregateFunction instanceof Distinct)
			return "distinct " + exportTableColumn(((Distinct) aggregateFunction).getTableColumn());

		if (aggregateFunction instanceof Count) {
			Count count = (Count) aggregateFunction;
			if (count.getExpression().isPresent())
				return "count(" + count.getExpression().get() + ")";

			if (count.getAggregateFunction().isPresent())
				return exportAggregateFunction(count.getAggregateFunction().get());
		}

		throw new IllegalArgumentException("Aggregate function '" + aggregateFunction + "'not supported");
	}

	private String exportCondition(Condition condition) {
		LOG.info("exportCondition: condition=" + condition);
		if (condition instanceof AndCondition) {
			AndCondition andCondition = (AndCondition) condition;
			StringBuilder sb = new StringBuilder();
			if (andCondition.nested())
				sb.append("(");

			String cc = andCondition.getConditions().stream().map(c -> {
				return exportCondition(c);
			}).collect(Collectors.joining(" and "));
			sb.append(cc);

			if (andCondition.nested())
				sb.append(")");

			return sb.toString();
		}

		if (condition instanceof OrCondition) {
			OrCondition orCondition = (OrCondition) condition;
			StringBuilder sb = new StringBuilder();
			if (orCondition.nested())
				sb.append("(");

			String cc = orCondition.getConditions().stream().map(c -> {
				return exportCondition(c);
			}).collect(Collectors.joining(" or "));
			sb.append(cc);

			if (orCondition.nested())
				sb.append(")");

			return sb.toString();
		}

		if (condition instanceof LikeCondition) {
			LikeCondition likeCondition = (LikeCondition) condition;
			return exportColumn(likeCondition.getColumn()) + dbJdbc.likeOperator() + " '"
					+ likeCondition.getExpression() + "'";
		}

		if (condition instanceof EqualColumnExprCondition) {
			EqualColumnExprCondition equalColumnExprCondition = (EqualColumnExprCondition) condition;
			StringBuilder sb = new StringBuilder(exportTableColumn(equalColumnExprCondition.getLeftColumn()));
			sb.append(" ");
			sb.append(dbJdbc.equalOperator());
			sb.append(" ");
			sb.append(equalColumnExprCondition.getExpression());
			return sb.toString();
		}

		if (condition instanceof EqualColumnsCondition) {
			EqualColumnsCondition equalColumnsCondition = (EqualColumnsCondition) condition;
			StringBuilder sb = new StringBuilder(exportColumn(equalColumnsCondition.getColumnLeft()));
			sb.append(" ");
			sb.append(dbJdbc.equalOperator());
			sb.append(" ");
			sb.append(exportColumn(equalColumnsCondition.getColumnRight()));
			return sb.toString();
		}

		throw new IllegalArgumentException("Condition '" + condition + "'not supported");
	}

	private String exportJoins(FromTable fromTable) {
		StringBuilder sb = new StringBuilder();
		if (!fromTable.getJoins().isPresent())
			return sb.toString();

		List<FromJoin> fromJoins = fromTable.getJoins().get();
		for (FromJoin fromJoin : fromJoins) {
			if (fromJoin.getType() == JoinType.InnerJoin) {
				sb.append(" INNER JOIN ");
				FromTable toTable = fromJoin.getToTable();
				sb.append(toTable.getName());
				if (toTable.getAlias().isPresent()) {
					sb.append(" AS ");
					sb.append(toTable.getAlias().get());
				}

				sb.append(" ON ");
				List<Column> fromColumns = fromJoin.getFromColumns();
				List<Column> toColumns = fromJoin.getToColumns();
				for (int i = 0; i < fromColumns.size(); ++i) {
					if (i > 0) {
						sb.append(" AND ");
					}

					if (fromTable.getAlias().isPresent()) {
						sb.append(fromTable.getAlias().get());
						sb.append(".");
					}

					sb.append(fromColumns.get(i).getName());
					sb.append(" = ");
					if (toTable.getAlias().isPresent()) {
						sb.append(toTable.getAlias().get());
						sb.append(".");
					}

					sb.append(toColumns.get(i).getName());
				}
			}
		}

		return sb.toString();
	}

	private String exportFromTable(FromTable fromTable) {
		StringBuilder sb = new StringBuilder(fromTable.getName());
		if (fromTable.getAlias().isPresent()) {
			sb.append(" AS ");
			sb.append(fromTable.getAlias().get());
		}

		sb.append(exportJoins(fromTable));
		return sb.toString();
	}

	private String exportGroupBy(GroupBy groupBy) {
		return "group by "
				+ groupBy.getColumns().stream().map(c -> exportTableColumn(c)).collect(Collectors.joining(", "));
	}

	public String export(SqlSelect sqlSelect) {
		StringBuilder sb = new StringBuilder("select ");
		String cc = sqlSelect.getValues().stream().map(c -> {
			if (c instanceof TableColumn)
				return exportTableColumn((TableColumn) c);
			if (c instanceof AggregateFunction)
				return exportAggregateFunction((AggregateFunction) c);

			throw new IllegalArgumentException("Value type '" + c + "'not supported");
		}).collect(Collectors.joining(", "));

		sb.append(cc);
		sb.append(" from ");
		sb.append(exportFromTable(sqlSelect.getFromTable()));

		if (sqlSelect.getConditions().isPresent()) {
			sb.append(" where ");
			String ccs = sqlSelect.getConditions().get().stream().map(c -> exportCondition(c))
					.collect(Collectors.joining(" "));
			sb.append(ccs);
		}

		if (sqlSelect.getGroupBy().isPresent()) {
			sb.append(" ");
			sb.append(exportGroupBy(sqlSelect.getGroupBy().get()));
		}

		return sb.toString();
	}

	private String getOperator(PredicateType predicateType) {
		switch (predicateType) {
		case EQUAL:
			return dbJdbc.equalOperator();
		case NOT_EQUAL:
			return dbJdbc.notEqualOperator();
		case AND:
			return dbJdbc.andOperator();
		case IS_FALSE:
			return dbJdbc.falseOperator();
		case IS_NOT_NULL:
			return dbJdbc.notNullOperator();
		case IS_NULL:
			return dbJdbc.nullOperator();
		case IS_TRUE:
			return dbJdbc.trueOperator();
		case NOT:
			return dbJdbc.notOperator();
		case OR:
			return dbJdbc.orOperator();
		case EMPTY_CONJUNCTION:
			return dbJdbc.emptyConjunctionOperator();
		case EMPTY_DISJUNCTION:
			return dbJdbc.emptyDisjunctionOperator();
		case GREATER_THAN:
		case GT:
			return dbJdbc.greaterThanOperator();
		case LESS_THAN:
		case LT:
			return dbJdbc.lessThanOperator();
		case BETWEEN_EXPRESSIONS:
		case BETWEEN_VALUES:
			return dbJdbc.betweenOperator();
		case LIKE_PATTERN:
		case LIKE_PATTERN_EXPR:
			return dbJdbc.likeOperator();
		default:
			break;
		}

		throw new IllegalArgumentException("Unknown operator for predicate type: " + predicateType);
	}

	private StringBuilder createAllFieldsQuery(SqlSelect sqlSelect) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		List<String> columns = createColumns(sqlSelect.getFetchColumnNameValues());

		int i = 0;
		for (String c : columns) {
			if (i > 0)
				sb.append(", ");

			sb.append(dbJdbc.getNameTranslator().toColumnName(sqlSelect.getFromTable().getAlias().get(), c));
			++i;
		}

		sb.append(" from ");
		sb.append(sqlSelect.getFromTable().getName());
		sb.append(" ");
		sb.append(sqlSelect.getFromTable().getAlias().get());
		return sb;
	}

	private void createSelectionFields(SqlSelect sqlSelect, CriteriaQuery<?> criteriaQuery, StringBuilder sb) {
		Selection<?> selection = criteriaQuery.getSelection();
		if (selection == null)
			return;

		if (selection instanceof MaxExpression<?>) {
			MaxExpression<Number> maxExpression = (MaxExpression<Number>) selection;
			Expression<Number> expr = maxExpression.getX();
			if (expr instanceof MiniPath<?>) {
				MiniPath<?> miniPath = (MiniPath<?>) expr;
				MetaAttribute metaAttribute = miniPath.getMetaAttribute();
				sb.append(" max(");
				sb.append(sqlSelect.getFromTable().getAlias().get());
				sb.append(".");
				sb.append(metaAttribute.getColumnName());
				sb.append(")");
			}
		} else if (selection instanceof MinExpression<?>) {
			MinExpression<Number> minExpression = (MinExpression<Number>) selection;
			Expression<Number> expr = minExpression.getX();
			if (expr instanceof MiniPath<?>) {
				MiniPath<?> miniPath = (MiniPath<?>) expr;
				MetaAttribute metaAttribute = miniPath.getMetaAttribute();
				sb.append(" min(");
				sb.append(sqlSelect.getFromTable().getAlias().get());
				sb.append(".");
				sb.append(metaAttribute.getColumnName());
				sb.append(")");
			}
		}

	}

	private StringBuilder createSelectionQuery(SqlSelect sqlSelect, CriteriaQuery<?> criteriaQuery) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		createSelectionFields(sqlSelect, criteriaQuery, sb);
		sb.append(" from ");
		sb.append(sqlSelect.getFromTable().getName());
		sb.append(" ");
		sb.append(sqlSelect.getFromTable().getAlias().get());
		return sb;
	}

	public StatementData generateByCriteria(SqlSelect sqlSelect, Query query) throws Exception {
		CriteriaQuery<?> criteriaQuery = null;
		if (query instanceof MiniTypedQuery<?>)
			criteriaQuery = ((MiniTypedQuery<?>) query).getCriteriaQuery();
		StringBuilder sb = null;
		if (sqlSelect.getResult() == null)
			sb = createSelectionQuery(sqlSelect, criteriaQuery);
		else
			sb = createAllFieldsQuery(sqlSelect);

		Predicate restriction = criteriaQuery.getRestriction();
		List<ColumnNameValue> parameters = new ArrayList<>();
		if (restriction != null) {
			sb.append(" where");
			createExpressionString(sqlSelect, restriction, sb, parameters, query);
		}

		return new StatementData(sb.toString(), parameters);
	}

	private void translateComparisonPredicate(ComparisonPredicate comparisonPredicate, List<ColumnNameValue> parameters,
			StringBuilder sb, String tableAlias, Query query) {
		MetaAttribute attribute = null;
		Expression<?> expression1 = comparisonPredicate.getX();
		Expression<?> expression2 = comparisonPredicate.getY();
		if (expression1 instanceof MiniPath<?>) {
			MiniPath<?> miniPath = (MiniPath<?>) expression1;
			attribute = miniPath.getMetaAttribute();
		} else if (expression2 instanceof MiniPath<?>) {
			MiniPath<?> miniPath = (MiniPath<?>) expression2;
			attribute = miniPath.getMetaAttribute();
		}

		if (expression1 instanceof MiniPath<?>) {
			addColumnNameAndOperator(tableAlias, attribute.getColumnName(),
					getOperator(comparisonPredicate.getPredicateType()), sb);
		} else if (expression1 instanceof ParameterExpression<?>) {
			ParameterExpression<?> parameterExpression = (ParameterExpression<?>) expression1;
			addParameterExpression(parameterExpression, attribute, parameters, query, sb);
		}

		if (expression2 != null) {
			if (expression2 instanceof MiniPath<?>) {
				sb.append(" ");
				sb.append(dbJdbc.getNameTranslator().toColumnName(tableAlias, attribute.getColumnName()));
			} else if (expression2 instanceof ParameterExpression<?>) {
				ParameterExpression<?> parameterExpression = (ParameterExpression<?>) expression2;
				addParameterExpression(parameterExpression, attribute, parameters, query, sb);
			}
		} else if (comparisonPredicate.getValue() != null) {
			if (requireQM(comparisonPredicate.getValue())) {
				sb.append(" ?");
				ColumnNameValue columnNameValue = new ColumnNameValue(attribute.getColumnName(),
						comparisonPredicate.getValue(), attribute.getType(), attribute.getReadWriteDbType(),
						attribute.getSqlType(), null, attribute);
				parameters.add(columnNameValue);
			} else {
				addValue(comparisonPredicate.getValue(), sb);
			}
		}
	}

	private void translateBetweenExpressionsPredicate(BetweenExpressionsPredicate betweenExpressionsPredicate,
			List<ColumnNameValue> parameters, StringBuilder sb, String tableAlias, Query query) {
		Expression<?> expression1 = betweenExpressionsPredicate.getX();
		Expression<?> expression2 = betweenExpressionsPredicate.getY();
		MiniPath<?> miniPath = (MiniPath<?>) betweenExpressionsPredicate.getV();
		MetaAttribute attribute = miniPath.getMetaAttribute();

		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(tableAlias, attribute.getColumnName()));
		sb.append(" ");
		sb.append(getOperator(betweenExpressionsPredicate.getPredicateType()));
		if (expression1 instanceof MiniPath<?>) {
			sb.append(" ");
			sb.append(dbJdbc.getNameTranslator().toColumnName(tableAlias, attribute.getColumnName()));
		} else if (expression1 instanceof ParameterExpression<?>) {
			ParameterExpression<?> parameterExpression = (ParameterExpression<?>) expression1;
			addParameterExpression(parameterExpression, attribute, parameters, query, sb);
		}

		sb.append(" AND ");
		if (expression2 instanceof MiniPath<?>) {
			sb.append(" ");
			sb.append(dbJdbc.getNameTranslator().toColumnName(tableAlias, attribute.getColumnName()));
		} else if (expression2 instanceof ParameterExpression<?>) {
			ParameterExpression<?> parameterExpression = (ParameterExpression<?>) expression2;
			addParameterExpression(parameterExpression, attribute, parameters, query, sb);
		}
	}

	private void translateBetweenValuesPredicate(BetweenValuesPredicate betweenValuesPredicate,
			List<ColumnNameValue> parameters, StringBuilder sb, String tableAlias, Query query) {
		Object x = betweenValuesPredicate.getX();
		Object y = betweenValuesPredicate.getY();
		MiniPath<?> miniPath = (MiniPath<?>) betweenValuesPredicate.getV();
		MetaAttribute attribute = miniPath.getMetaAttribute();

		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(tableAlias, attribute.getColumnName()));
		sb.append(" ");
		sb.append(getOperator(betweenValuesPredicate.getPredicateType()));
		if (requireQM(x)) {
			sb.append(" ?");
			ColumnNameValue columnNameValue = new ColumnNameValue(attribute.getColumnName(), x, attribute.getType(),
					attribute.getReadWriteDbType(), attribute.getSqlType(), null, attribute);
			parameters.add(columnNameValue);
		} else {
			addValue(x, sb);
		}

		sb.append(" AND ");
		if (requireQM(y)) {
			sb.append(" ?");
			ColumnNameValue columnNameValue = new ColumnNameValue(attribute.getColumnName(), y, attribute.getType(),
					attribute.getReadWriteDbType(), attribute.getSqlType(), null, attribute);
			parameters.add(columnNameValue);
		} else {
			addValue(y, sb);
		}
	}

	private void translateLikePatternPredicate(LikePatternPredicate likePatternPredicate, StringBuilder sb,
			String tableAlias, Query query) {
		String pattern = likePatternPredicate.getPattern();
		MiniPath<?> miniPath = (MiniPath<?>) likePatternPredicate.getX();
		MetaAttribute attribute = miniPath.getMetaAttribute();
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(tableAlias, attribute.getColumnName()));
		sb.append(" ");
		sb.append(getOperator(likePatternPredicate.getPredicateType()));
		sb.append(" '");
		sb.append(pattern);
		sb.append("'");
	}

	private void translateLikePatternExprPredicate(LikePatternExprPredicate likePatternExprPredicate, StringBuilder sb,
			List<ColumnNameValue> parameters, String tableAlias, Query query) {
		Expression<String> pattern = likePatternExprPredicate.getPatternEx();
		MiniPath<?> miniPath = (MiniPath<?>) likePatternExprPredicate.getX();
		MetaAttribute attribute = miniPath.getMetaAttribute();
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(tableAlias, attribute.getColumnName()));
		sb.append(" ");
		sb.append(getOperator(likePatternExprPredicate.getPredicateType()));
		sb.append(" '");
		if (pattern instanceof ParameterExpression<?>) {
			ParameterExpression<?> parameterExpression = (ParameterExpression<?>) pattern;
			addParameter(parameterExpression, attribute, parameters, query);
			sb.append("?");
		}

		sb.append("'");
	}

	private void translateMultiplePredicate(MultiplePredicate multiplePredicate, StringBuilder sb,
			List<ColumnNameValue> parameters, Query query, SqlSelect sqlSelect) {
		Predicate[] predicates = multiplePredicate.getRestrictions();
		String operator = getOperator(multiplePredicate.getPredicateType());
		int i = 0;
		for (Predicate p : predicates) {
			sb.append(" ");
			if (i > 0) {
				sb.append(operator);
			}

			sb.append(" (");
			createExpressionString(sqlSelect, p, sb, parameters, query);
			sb.append(")");
			++i;
		}
	}

	private void translateBinaryBooleanExprPredicate(BinaryBooleanExprPredicate binaryBooleanExprPredicate,
			StringBuilder sb, List<ColumnNameValue> parameters, Query query, SqlSelect sqlSelect) {
		Expression<Boolean> x = binaryBooleanExprPredicate.getX();
		Expression<Boolean> y = binaryBooleanExprPredicate.getY();
		String operator = getOperator(binaryBooleanExprPredicate.getPredicateType());

		sb.append(" (");
		if (x instanceof Predicate)
			createExpressionString(sqlSelect, (Predicate) x, sb, parameters, query);

		sb.append(")");
		sb.append(" ");
		sb.append(operator);
		sb.append(" (");
		if (y instanceof Predicate)
			createExpressionString(sqlSelect, (Predicate) y, sb, parameters, query);

		sb.append(")");
	}

	private void translateBooleanExprPredicate(BooleanExprPredicate booleanExprPredicate, StringBuilder sb,
			List<ColumnNameValue> parameters, Query query, SqlSelect sqlSelect, boolean prefixOperator) {
		Expression<Boolean> x = booleanExprPredicate.getX();
		String operator = getOperator(booleanExprPredicate.getPredicateType());

		if (prefixOperator) {
			sb.append(" ");
			sb.append(operator);
			sb.append(" (");
			if (x instanceof Predicate)
				createExpressionString(sqlSelect, (Predicate) x, sb, parameters, query);

			sb.append(")");
		} else {
			if (x instanceof MiniPath<?>) {
				MiniPath<?> miniPath = (MiniPath<?>) x;
				MetaAttribute attribute = miniPath.getMetaAttribute();

				sb.append(" ");
				sb.append(dbJdbc.getNameTranslator().toColumnName(sqlSelect.getFromTable().getAlias().get(),
						attribute.getColumnName()));
				sb.append(" ");
				sb.append(operator);
			}
		}
	}

	private void translateExprPredicate(ExprPredicate exprPredicate, StringBuilder sb, List<ColumnNameValue> parameters,
			Query query, SqlSelect sqlSelect) {
		Expression<?> x = exprPredicate.getX();
		String operator = getOperator(exprPredicate.getPredicateType());

		if (x instanceof MiniPath<?>) {
			MiniPath<?> miniPath = (MiniPath<?>) x;
			MetaAttribute attribute = miniPath.getMetaAttribute();
			addColumnNameAndOperator(sqlSelect.getFromTable().getAlias().get(), attribute.getColumnName(), operator,
					sb);
		}
//		else if (x instanceof ParameterExpression<?>) {
//			ParameterExpression<?> parameterExpression = (ParameterExpression<?>) x;
//			addParameterExpression(parameterExpression, attribute, parameters, query, sb);
//		}
	}

	private void translateEmptyPredicate(EmptyPredicate emptyPredicate, StringBuilder sb,
			List<ColumnNameValue> parameters, Query query, SqlSelect sqlSelect) {
		sb.append(" ");
		sb.append(getOperator(emptyPredicate.getPredicateType()));
	}

	private boolean requireQM(Object value) {
		if (value instanceof LocalDate)
			return true;

		return false;
	}

	private void addColumnNameAndOperator(String tableAlias, String columnName, String operator, StringBuilder sb) {
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(tableAlias, columnName));
		sb.append(" ");
		sb.append(operator);
	}

	private void addValue(Object value, StringBuilder sb) {
		sb.append(" ");
		if (value instanceof String) {
			sb.append("'");
			sb.append((String) value);
			sb.append("'");
		} else {
			sb.append(value.toString());
		}
	}

	private void addParameter(ParameterExpression<?> parameterExpression, MetaAttribute attribute,
			List<ColumnNameValue> parameters, Query query) {
		Object value = null;
		if (parameterExpression.getName() != null) {
			value = query.getParameterValue(parameterExpression.getName());
		}

		ColumnNameValue columnNameValue = new ColumnNameValue(attribute.getColumnName(), value, attribute.getType(),
				attribute.getReadWriteDbType(), attribute.getSqlType(), null, attribute);
		parameters.add(columnNameValue);
	}

	private void addParameterExpression(ParameterExpression<?> parameterExpression, MetaAttribute attribute,
			List<ColumnNameValue> parameters, Query query, StringBuilder sb) {
		addParameter(parameterExpression, attribute, parameters, query);
		sb.append(" ?");
	}

	private void createExpressionString(SqlSelect sqlSelect, Predicate predicate, StringBuilder sb,
			List<ColumnNameValue> parameters, Query query) {
		PredicateTypeInfo predicateTypeInfo = (PredicateTypeInfo) predicate;
		PredicateType predicateType = predicateTypeInfo.getPredicateType();
		if (predicateType == PredicateType.EQUAL || predicateType == PredicateType.NOT_EQUAL
				|| predicateType == PredicateType.GREATER_THAN || predicateType == PredicateType.GT
				|| predicateType == PredicateType.LESS_THAN || predicateType == PredicateType.LT) {
			translateComparisonPredicate((ComparisonPredicate) predicate, parameters, sb,
					sqlSelect.getFromTable().getAlias().get(), query);
		} else if (predicateType == PredicateType.BETWEEN_EXPRESSIONS) {
			BetweenExpressionsPredicate betweenExpressionsPredicate = (BetweenExpressionsPredicate) predicate;
			translateBetweenExpressionsPredicate(betweenExpressionsPredicate, parameters, sb,
					sqlSelect.getFromTable().getAlias().get(), query);
		} else if (predicateType == PredicateType.BETWEEN_VALUES) {
			BetweenValuesPredicate betweenValuesPredicate = (BetweenValuesPredicate) predicate;
			translateBetweenValuesPredicate(betweenValuesPredicate, parameters, sb,
					sqlSelect.getFromTable().getAlias().get(), query);
		} else if (predicateType == PredicateType.LIKE_PATTERN) {
			LikePatternPredicate likePatternPredicate = (LikePatternPredicate) predicate;
			translateLikePatternPredicate(likePatternPredicate, sb, sqlSelect.getFromTable().getAlias().get(), query);
		} else if (predicateType == PredicateType.LIKE_PATTERN_EXPR) {
			LikePatternExprPredicate likePatternExprPredicate = (LikePatternExprPredicate) predicate;
			translateLikePatternExprPredicate(likePatternExprPredicate, sb, parameters,
					sqlSelect.getFromTable().getAlias().get(), query);
		} else if (predicateType == PredicateType.OR || predicateType == PredicateType.AND) {
			if (predicate instanceof MultiplePredicate) {
				translateMultiplePredicate(null, sb, parameters, query, sqlSelect);
			} else {
				translateBinaryBooleanExprPredicate((BinaryBooleanExprPredicate) predicate, sb, parameters, query,
						sqlSelect);
			}
		} else if (predicateType == PredicateType.NOT) {
			translateBooleanExprPredicate((BooleanExprPredicate) predicate, sb, parameters, query, sqlSelect, true);
		} else if (predicateType == PredicateType.IS_NULL || predicateType == PredicateType.IS_NOT_NULL) {
			translateExprPredicate((ExprPredicate) predicate, sb, parameters, query, sqlSelect);
		} else if (predicateType == PredicateType.IS_TRUE || predicateType == PredicateType.IS_FALSE) {
			translateBooleanExprPredicate((BooleanExprPredicate) predicate, sb, parameters, query, sqlSelect, false);
		} else if (predicateType == PredicateType.EMPTY_CONJUNCTION
				|| predicateType == PredicateType.EMPTY_DISJUNCTION) {
			translateEmptyPredicate((EmptyPredicate) predicate, sb, parameters, query, sqlSelect);
		}
	}

}
