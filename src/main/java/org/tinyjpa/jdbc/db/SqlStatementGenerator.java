package org.tinyjpa.jdbc.db;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.tinyjpa.jdbc.AttributeUtil;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.ColumnNameValue;
import org.tinyjpa.jdbc.JoinColumnAttribute;
import org.tinyjpa.jdbc.MetaAttribute;
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

	public StatementData generateByCriteria(SqlSelect sqlSelect) throws Exception {
		CriteriaQuery<?> criteriaQuery = sqlSelect.getCriteriaQuery();
		Predicate restriction = criteriaQuery.getRestriction();
		List<ColumnNameValue> parameters = new ArrayList<>();
		if (restriction != null) {
			StringBuilder sb = createAllFieldsQuery(sqlSelect);
			sb.append(" where");
			createExpressionString(sqlSelect, restriction, sb, parameters);
			return new StatementData(sb.toString(), parameters);
		}

		StringBuilder sb = createAllFieldsQuery(sqlSelect);
		String sql = sb.toString();
		return new StatementData(sql, parameters);
	}

	private void createExpressionString(SqlSelect sqlSelect, Predicate predicate, StringBuilder sb,
			List<ColumnNameValue> parameters) {
		PredicateTypeInfo predicateTypeInfo = (PredicateTypeInfo) predicate;
		PredicateType predicateType = predicateTypeInfo.getPredicateType();
		if (predicateType == PredicateType.EQUAL || predicateType == PredicateType.NOT_EQUAL
				|| predicateType == PredicateType.GREATER_THAN || predicateType == PredicateType.GT
				|| predicateType == PredicateType.LESS_THAN || predicateType == PredicateType.LT) {
			Expression<?> expression = ((ComparisonPredicate) predicate).getX();
//			LOG.info("createExpressionString: expression=" + expression);
			Object value = ((ComparisonPredicate) predicate).getValue1();
//			LOG.info("createExpressionString: object=" + value);
			MiniPath<?> miniPath = (MiniPath<?>) expression;
			MetaAttribute attribute = miniPath.getMetaAttribute();
			if (value instanceof LocalDate) {
				applyQMBinaryOperator(getOperator(((ComparisonPredicate) predicate).getPredicateType()),
						attribute.getColumnName(), sqlSelect.getTableAlias(), sb);
				ColumnNameValue columnNameValue = new ColumnNameValue(attribute.getColumnName(), value,
						attribute.getType(), attribute.getReadWriteDbType(), attribute.getSqlType(), null, attribute);
				parameters.add(columnNameValue);
			} else {
				applyBinaryOperator(getOperator(((ComparisonPredicate) predicate).getPredicateType()),
						attribute.getColumnName(), value, sqlSelect.getTableAlias(), sb);
			}
		} else if (predicateType == PredicateType.BETWEEN_EXPRESSIONS) {
			BetweenExpressionsPredicate betweenExpressionsPredicate = (BetweenExpressionsPredicate) predicate;
			Expression<?> x = betweenExpressionsPredicate.getX();
			Expression<?> y = betweenExpressionsPredicate.getY();
			MiniPath<?> miniPath = (MiniPath<?>) betweenExpressionsPredicate.getV();
			MetaAttribute attribute = miniPath.getMetaAttribute();
			applyBetweenExpressionsOperator(getOperator(betweenExpressionsPredicate.getPredicateType()),
					attribute.getColumnName(), x, y, sqlSelect.getTableAlias(), sb);
		} else if (predicateType == PredicateType.BETWEEN_VALUES) {
			BetweenValuesPredicate betweenValuesPredicate = (BetweenValuesPredicate) predicate;
			Object value1 = betweenValuesPredicate.getX();
			Object value2 = betweenValuesPredicate.getY();
			MiniPath<?> miniPath = (MiniPath<?>) betweenValuesPredicate.getV();
			MetaAttribute attribute = miniPath.getMetaAttribute();
			if (value1 instanceof LocalDate) {
				applyQMBetweenValuesOperator(getOperator(betweenValuesPredicate.getPredicateType()),
						attribute.getColumnName(), sqlSelect.getTableAlias(), sb);
				ColumnNameValue columnNameValue = new ColumnNameValue(attribute.getColumnName(), value1,
						attribute.getType(), attribute.getReadWriteDbType(), attribute.getSqlType(), null, attribute);
				parameters.add(columnNameValue);
				columnNameValue = new ColumnNameValue(attribute.getColumnName(), value2, attribute.getType(),
						attribute.getReadWriteDbType(), attribute.getSqlType(), null, attribute);
				parameters.add(columnNameValue);
			} else
				applyBetweenValuesOperator(getOperator(betweenValuesPredicate.getPredicateType()),
						attribute.getColumnName(), value1, value2, sqlSelect.getTableAlias(), sb);
		} else if (predicateType == PredicateType.LIKE_PATTERN) {
			LikePatternPredicate likePatternPredicate = (LikePatternPredicate) predicate;
			String pattern = likePatternPredicate.getPattern();
			MiniPath<?> miniPath = (MiniPath<?>) likePatternPredicate.getX();
			MetaAttribute attribute = miniPath.getMetaAttribute();
			applyLikePatternOperator(getOperator(likePatternPredicate.getPredicateType()), attribute.getColumnName(),
					pattern, sqlSelect.getTableAlias(), sb);
		} else if (predicateType == PredicateType.LIKE_PATTERN_EXPR) {
			LikePatternExprPredicate likePatternExprPredicate = (LikePatternExprPredicate) predicate;
			Expression<String> patternExpr = likePatternExprPredicate.getPatternEx();
			MiniPath<?> miniPath = (MiniPath<?>) likePatternExprPredicate.getX();
			MetaAttribute attribute = miniPath.getMetaAttribute();
			applyLikePatternExprOperator(getOperator(likePatternExprPredicate.getPredicateType()),
					attribute.getColumnName(), patternExpr, sqlSelect.getTableAlias(), sb);
		} else if (predicateType == PredicateType.OR || predicateType == PredicateType.AND) {
			if (predicate instanceof MultiplePredicate) {
				applyBinaryOperator(sqlSelect, getOperator(((MultiplePredicate) predicate).getPredicateType()),
						((MultiplePredicate) predicate).getRestrictions(), sb, parameters);
			}

			applyBinaryOperator(sqlSelect, getOperator(((BinaryBooleanExprPredicate) predicate).getPredicateType()),
					(Predicate) ((BinaryBooleanExprPredicate) predicate).getX(),
					(Predicate) ((BinaryBooleanExprPredicate) predicate).getY(), sb, parameters);
		} else if (predicateType == PredicateType.NOT) {
			applyPrefixUnaryOperator(sqlSelect, getOperator(((BooleanExprPredicate) predicate).getPredicateType()),
					((BooleanExprPredicate) predicate), sb, parameters);
		} else if (predicateType == PredicateType.IS_NULL || predicateType == PredicateType.IS_NOT_NULL) {
			MiniPath<?> miniPath = (MiniPath<?>) ((ExprPredicate) predicate).getX();
			MetaAttribute attribute = miniPath.getMetaAttribute();
			applyPostfixUnaryOperator(getOperator(((ExprPredicate) predicate).getPredicateType()),
					attribute.getColumnName(), sqlSelect.getTableAlias(), sb);
		} else if (predicateType == PredicateType.IS_TRUE || predicateType == PredicateType.IS_FALSE) {
			MiniPath<?> miniPath = (MiniPath<?>) ((BooleanExprPredicate) predicate).getX();
			MetaAttribute attribute = miniPath.getMetaAttribute();
			applyPostfixUnaryOperator(getOperator(((BooleanExprPredicate) predicate).getPredicateType()),
					attribute.getColumnName(), sqlSelect.getTableAlias(), sb);
		} else if (predicateType == PredicateType.EMPTY_CONJUNCTION
				|| predicateType == PredicateType.EMPTY_DISJUNCTION) {
			applyCondition(getOperator(((EmptyPredicate) predicate).getPredicateType()), sb);
		}
	}

	private void applyBinaryOperator(String operator, String columnName, Object value, String tableAlias,
			StringBuilder sb) {
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(tableAlias, columnName));
		sb.append(" ");
		sb.append(operator);
		sb.append(" ");
		if (value instanceof String) {
			sb.append("'");
			sb.append((String) value);
			sb.append("'");
		} else {
			sb.append(value.toString());
		}
	}

	private void applyBetweenValuesOperator(String operator, String columnName, Object value1, Object value2,
			String tableAlias, StringBuilder sb) {
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(tableAlias, columnName));
		sb.append(" ");
		sb.append(operator);
		sb.append(" ");
		if (value1 instanceof String) {
			sb.append("'");
			sb.append((String) value1);
			sb.append("'");
		} else {
			sb.append(value1.toString());
		}

		sb.append(" AND ");
		if (value2 instanceof String) {
			sb.append("'");
			sb.append((String) value2);
			sb.append("'");
		} else {
			sb.append(value2.toString());
		}
	}

	private void applyBetweenExpressionsOperator(String operator, String columnName, Expression<?> x, Expression<?> y,
			String tableAlias, StringBuilder sb) {
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(tableAlias, columnName));
		sb.append(" ");
		sb.append(operator);
		sb.append(" ");
		sb.append(calculateExpression(x));
		sb.append(" AND ");
		sb.append(calculateExpression(y));
	}

	private void applyQMBinaryOperator(String operator, String columnName, String tableAlias, StringBuilder sb) {
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(tableAlias, columnName));
		sb.append(" ");
		sb.append(operator);
		sb.append(" ?");
	}

	private void applyQMBetweenValuesOperator(String operator, String columnName, String tableAlias, StringBuilder sb) {
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(tableAlias, columnName));
		sb.append(" ");
		sb.append(operator);
		sb.append(" ? AND ?");
	}

	private void applyBinaryOperator(SqlSelect sqlSelect, String operator, Predicate p1, Predicate p2, StringBuilder sb,
			List<ColumnNameValue> parameters) {
		sb.append(" (");
		createExpressionString(sqlSelect, p1, sb, parameters);
		sb.append(")");
		sb.append(" ");
		sb.append(operator);
		sb.append(" (");
		createExpressionString(sqlSelect, p2, sb, parameters);
		sb.append(")");
	}

	private void applyBinaryOperator(SqlSelect sqlSelect, String operator, Predicate[] predicates, StringBuilder sb,
			List<ColumnNameValue> parameters) {
		int i = 0;
		for (Predicate p : predicates) {
			sb.append(" ");
			if (i > 0) {
				sb.append(operator);
			}

			sb.append(" (");
			createExpressionString(sqlSelect, p, sb, parameters);
			sb.append(")");
			++i;
		}
	}

	private void applyPrefixUnaryOperator(SqlSelect sqlSelect, String operator,
			BooleanExprPredicate unaryBooleanPredicate, StringBuilder sb, List<ColumnNameValue> parameters) {
		List<Expression<Boolean>> expressions = unaryBooleanPredicate.getExpressions();
		sb.append(" ");
		sb.append(operator);
		sb.append(" (");
		for (Expression<Boolean> expression : expressions) {
			ComparisonPredicate p = (ComparisonPredicate) expression;
			createExpressionString(sqlSelect, p, sb, parameters);
		}

		sb.append(")");
	}

	private void applyPostfixUnaryOperator(String operator, String columnName, String tableAlias, StringBuilder sb) {
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(tableAlias, columnName));
		sb.append(" ");
		sb.append(operator);
	}

	private void applyCondition(String condition, StringBuilder sb) {
		sb.append(" ");
		sb.append(condition);
	}

	private String calculateExpression(Expression<?> expression) {
		MiniPath<?> pathImpl = (MiniPath<?>) expression;
		return pathImpl.getMetaAttribute().getName();
	}

	private void applyLikePatternExprOperator(String operator, String columnName, Expression<String> pattern,
			String tableAlias, StringBuilder sb) {
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(tableAlias, columnName));
		sb.append(" ");
		sb.append(operator);
		sb.append(" '");
		sb.append(calculateExpression(pattern));
		sb.append("'");
	}

	private void applyLikePatternOperator(String operator, String columnName, String pattern, String tableAlias,
			StringBuilder sb) {
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(tableAlias, columnName));
		sb.append(" ");
		sb.append(operator);
		sb.append(" '");
		sb.append(pattern);
		sb.append("'");
	}

}
