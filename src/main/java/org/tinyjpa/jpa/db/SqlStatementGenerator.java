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

import org.tinyjpa.jdbc.AttributeUtil;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.ColumnNameValue;
import org.tinyjpa.jdbc.JoinColumnAttribute;
import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.db.DbJdbc;
import org.tinyjpa.jdbc.db.StatementData;
import org.tinyjpa.jdbc.model.SqlDelete;
import org.tinyjpa.jdbc.model.SqlInsert;
import org.tinyjpa.jdbc.model.SqlSelect;
import org.tinyjpa.jdbc.model.SqlSelectJoin;
import org.tinyjpa.jdbc.model.SqlUpdate;
import org.tinyjpa.jpa.criteria.BetweenExpressionsPredicate;
import org.tinyjpa.jpa.criteria.BetweenValuesPredicate;
import org.tinyjpa.jpa.criteria.BinaryBooleanExprPredicate;
import org.tinyjpa.jpa.criteria.BooleanExprPredicate;
import org.tinyjpa.jpa.criteria.ComparisonPredicate;
import org.tinyjpa.jpa.criteria.EmptyPredicate;
import org.tinyjpa.jpa.criteria.ExprPredicate;
import org.tinyjpa.jpa.criteria.LikePatternExprPredicate;
import org.tinyjpa.jpa.criteria.LikePatternPredicate;
import org.tinyjpa.jpa.criteria.MiniPath;
import org.tinyjpa.jpa.criteria.MultiplePredicate;
import org.tinyjpa.jpa.criteria.PredicateType;
import org.tinyjpa.jpa.criteria.PredicateTypeInfo;

public class SqlStatementGenerator {
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

	public String generate(SqlSelect sqlSelect) {
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
		sb.append(sqlSelect.getTableName());
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

	public String generate(SqlSelectJoin sqlSelectJoin) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		int i = 0;
		List<String> columns = createColumns(sqlSelectJoin.getFetchColumnNameValues());
		for (String c : columns) {
			if (i > 0)
				sb.append(", ");

			sb.append(dbJdbc.getNameTranslator().toColumnName(sqlSelectJoin.getTableAlias(), c));
			++i;
		}

		// select t1.id, t1.p1 from entity t1 inner join jointable j on t1.id=j.id1
		// where j.t2=fk
		sb.append(" from ");
		sb.append(sqlSelectJoin.getTableName());
		sb.append(" ");
		sb.append(sqlSelectJoin.getTableAlias());
		sb.append(" inner join ");
		sb.append(sqlSelectJoin.getJoinTable().getTableName());
		sb.append(" ");
		sb.append(sqlSelectJoin.getJoinTable().getAlias());
		sb.append(" on ");

		// handles multiple column pk
		List<JoinColumnAttribute> joinColumnTargetAttributes = sqlSelectJoin.getJoinTable()
				.getJoinColumnTargetAttributes();
		int index = -1;
		for (int k = 0; k < sqlSelectJoin.getIdAttributes().size(); ++k) {
			if (k > 0)
				sb.append(" and ");

			sb.append(dbJdbc.getNameTranslator().toColumnName(sqlSelectJoin.getTableAlias(),
					sqlSelectJoin.getIdAttributes().get(k).getColumnName()));
			sb.append(" = ");
			index = AttributeUtil.indexOfJoinColumnAttribute(joinColumnTargetAttributes,
					sqlSelectJoin.getIdAttributes().get(k));
			JoinColumnAttribute joinColumnAttribute = joinColumnTargetAttributes.get(index);
			sb.append(dbJdbc.getNameTranslator().toColumnName(sqlSelectJoin.getJoinTable().getAlias(),
					joinColumnAttribute.getColumnName()));
		}

		sb.append(" where ");

		List<JoinColumnAttribute> joinColumnOwningAttributes = sqlSelectJoin.getJoinTable()
				.getJoinColumnOwningAttributes();
		List<AttributeValue> attributeValues = new ArrayList<>();
		i = 0;
		for (AttributeValue av : sqlSelectJoin.getOwningIdAttributeValues()) {
			if (i > 0)
				sb.append(" and ");

			index = AttributeUtil.indexOfJoinColumnAttribute(joinColumnOwningAttributes, av.getAttribute());
			attributeValues.add(
					new AttributeValue(joinColumnOwningAttributes.get(index).getForeignKeyAttribute(), av.getValue()));
			sb.append(dbJdbc.getNameTranslator().toColumnName(sqlSelectJoin.getJoinTable().getAlias(),
					joinColumnOwningAttributes.get(index).getColumnName()));
			sb.append(" = ?");
			++i;
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
			return dbJdbc.isFalseOperator();
		case IS_NOT_NULL:
			return dbJdbc.isNotNullOperator();
		case IS_NULL:
			return dbJdbc.isNullOperator();
		case IS_TRUE:
			return dbJdbc.isTrueOperator();
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

	protected StringBuilder createAllFieldsQuery(SqlSelect sqlSelect) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		List<String> columns = createColumns(sqlSelect.getFetchColumnNameValues());

		int i = 0;
		for (String c : columns) {
			if (i > 0)
				sb.append(", ");

			sb.append(dbJdbc.getNameTranslator().toColumnName(sqlSelect.getTableAlias(), c));
			++i;
		}

		sb.append(" from ");
		sb.append(sqlSelect.getTableName());
		sb.append(" ");
		sb.append(sqlSelect.getTableAlias());
		return sb;
	}

	public StatementData generateByCriteria(SqlSelect sqlSelect, Query query) throws Exception {
		CriteriaQuery<?> criteriaQuery = sqlSelect.getCriteriaQuery();
		Predicate restriction = criteriaQuery.getRestriction();
		List<ColumnNameValue> parameters = new ArrayList<>();
		if (restriction != null) {
			StringBuilder sb = createAllFieldsQuery(sqlSelect);
			sb.append(" where");
			createExpressionString(sqlSelect, restriction, sb, parameters, query);
			return new StatementData(sb.toString(), parameters);
		}

		StringBuilder sb = createAllFieldsQuery(sqlSelect);
		String sql = sb.toString();
		return new StatementData(sql, parameters);
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
				sb.append(
						dbJdbc.getNameTranslator().toColumnName(sqlSelect.getTableAlias(), attribute.getColumnName()));
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
			addColumnNameAndOperator(sqlSelect.getTableAlias(), attribute.getColumnName(), operator, sb);
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
			translateComparisonPredicate((ComparisonPredicate) predicate, parameters, sb, sqlSelect.getTableAlias(),
					query);
		} else if (predicateType == PredicateType.BETWEEN_EXPRESSIONS) {
			BetweenExpressionsPredicate betweenExpressionsPredicate = (BetweenExpressionsPredicate) predicate;
			translateBetweenExpressionsPredicate(betweenExpressionsPredicate, parameters, sb, sqlSelect.getTableAlias(),
					query);
		} else if (predicateType == PredicateType.BETWEEN_VALUES) {
			BetweenValuesPredicate betweenValuesPredicate = (BetweenValuesPredicate) predicate;
			translateBetweenValuesPredicate(betweenValuesPredicate, parameters, sb, sqlSelect.getTableAlias(), query);
		} else if (predicateType == PredicateType.LIKE_PATTERN) {
			LikePatternPredicate likePatternPredicate = (LikePatternPredicate) predicate;
			translateLikePatternPredicate(likePatternPredicate, sb, sqlSelect.getTableAlias(), query);
		} else if (predicateType == PredicateType.LIKE_PATTERN_EXPR) {
			LikePatternExprPredicate likePatternExprPredicate = (LikePatternExprPredicate) predicate;
			translateLikePatternExprPredicate(likePatternExprPredicate, sb, parameters, sqlSelect.getTableAlias(),
					query);
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
