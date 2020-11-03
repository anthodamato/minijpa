package org.tinyjpa.jpa.criteria;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.ColumnNameValue;
import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jdbc.db.DbJdbc;
import org.tinyjpa.jdbc.db.SqlStatementFactory;

public class SqlStatementCriteriaFactory extends SqlStatementFactory {
	private Logger LOG = LoggerFactory.getLogger(SqlStatementCriteriaFactory.class);

	public SqlStatementCriteriaFactory(DbJdbc dbJdbc) {
		super(dbJdbc);
	}

	public SqlStatement select(CriteriaQuery<?> criteriaQuery, Map<String, MetaEntity> entities) throws Exception {
		LOG.info("select: this=" + this);
		List<Expression<Boolean>> restrictions = ((CriteriaQueryImpl<?>) criteriaQuery).getRestrictions();
		LOG.info("select: restrictions=" + restrictions);
		if (!restrictions.isEmpty()) {
			Expression<Boolean> expression = restrictions.get(0);
			Predicate predicate = (Predicate) expression;
			LOG.info("select: criteriaQuery.getResultType()=" + criteriaQuery.getResultType());
			MetaEntity entity = entities.get(criteriaQuery.getResultType().getName());

			List<MetaAttribute> expandedAttributes = entity.getId().expand();
			expandedAttributes.addAll(entity.expandAttributes());
			StringBuilder sb = createAllFieldsQuery(entity, expandedAttributes);
			sb.append(" where");
			List<ColumnNameValue> columnNameValues = createExpressionString(predicate, entity, sb);
			LOG.info("select: sb.toString()=" + sb.toString());
			List<ColumnNameValue> fetchColumnNameValues = convertAttributes(expandedAttributes);
			String sql = sb.toString();
			return new SqlStatement.Builder().withSql(sql).withFetchColumnNameValues(fetchColumnNameValues)
					.withColumnNameValues(columnNameValues).build();
		}

		MetaEntity entity = entities.get(criteriaQuery.getResultType().getName());
		List<MetaAttribute> expandedAttributes = entity.getId().expand();
		expandedAttributes.addAll(entity.expandAttributes());
		StringBuilder sb = createAllFieldsQuery(entity, expandedAttributes);
		List<ColumnNameValue> fetchColumnNameValues = convertAttributes(expandedAttributes);
		String sql = sb.toString();
		return new SqlStatement.Builder().withSql(sql).withFetchColumnNameValues(fetchColumnNameValues).build();
	}

	private String getOperator(PredicateType predicateType) {
		switch (predicateType) {
		case EQUAL:
			return equalOperator();
		case NOT_EQUAL:
			return notEqualOperator();
		case AND:
			return andOperator();
		case IS_FALSE:
			return isFalseOperator();
		case IS_NOT_NULL:
			return isNotNullOperator();
		case IS_NULL:
			return isNullOperator();
		case IS_TRUE:
			return isTrueOperator();
		case NOT:
			return notOperator();
		case OR:
			return orOperator();
		case EMPTY_CONJUNCTION:
			return emptyConjunctionOperator();
		case EMPTY_DISJUNCTION:
			return emptyDisjunctionOperator();
		case GREATER_THAN:
		case GT:
			return greaterThanOperator();
		case LESS_THAN:
		case LT:
			return lessThanOperator();
		case BETWEEN_EXPRESSIONS:
		case BETWEEN_VALUES:
			return betweenOperator();
		case LIKE_PATTERN:
		case LIKE_PATTERN_EXPR:
			return likeOperator();
		default:
			break;
		}

		return "";
	}

	private List<ColumnNameValue> createExpressionString(Predicate predicate, MetaEntity entity, StringBuilder sb) {
		PredicateTypeInfo predicateTypeInfo = (PredicateTypeInfo) predicate;
		PredicateImpl predicateImpl = null;
		switch (predicateTypeInfo.getPredicateType()) {
		case EQUAL:
		case NOT_EQUAL:
		case GREATER_THAN:
		case GT:
		case LESS_THAN:
		case LT:
			predicateImpl = (PredicateImpl) predicate;
			Expression<?> expression = predicateImpl.getX();
			LOG.info("createExpressionString: expression=" + expression);
			Object value = predicateImpl.getValue1();
			LOG.info("createExpressionString: object=" + value);
			PathImpl<?> pathImpl = (PathImpl<?>) expression;
			if (value instanceof LocalDate) {
				String attributeName = pathImpl.getAttributeName();
				MetaAttribute attribute = entity.getAttribute(attributeName);
				applyQMBinaryOperator(getOperator(predicateImpl.getPredicateType()), attribute, entity, sb);
				AttributeValue av = new AttributeValue(attribute, value);
				ColumnNameValue columnNameValue = ColumnNameValue.build(av);
				return Arrays.asList(columnNameValue);
			} else
				applyBinaryOperator(getOperator(predicateImpl.getPredicateType()), pathImpl, value, entity, sb);

			break;

		case BETWEEN_EXPRESSIONS:
			BetweenExpressionsPredicate betweenExpressionsPredicate = (BetweenExpressionsPredicate) predicate;
			Expression<?> x = betweenExpressionsPredicate.getX();
			Expression<?> y = betweenExpressionsPredicate.getY();
			pathImpl = (PathImpl<?>) betweenExpressionsPredicate.getV();
			applyBetweenExpressionsOperator(getOperator(betweenExpressionsPredicate.getPredicateType()), pathImpl, x, y,
					entity, sb);

			break;

		case BETWEEN_VALUES:
			BetweenValuesPredicate betweenValuesPredicate = (BetweenValuesPredicate) predicate;
			Object value1 = betweenValuesPredicate.getX();
			Object value2 = betweenValuesPredicate.getY();
			pathImpl = (PathImpl<?>) betweenValuesPredicate.getV();
			if (value1 instanceof LocalDate) {
				String attributeName = pathImpl.getAttributeName();
				MetaAttribute attribute = entity.getAttribute(attributeName);
				applyQMBetweenValuesOperator(getOperator(betweenValuesPredicate.getPredicateType()), attribute, entity,
						sb);
				AttributeValue av1 = new AttributeValue(attribute, value1);
				ColumnNameValue cnv1 = ColumnNameValue.build(av1);
				AttributeValue av2 = new AttributeValue(attribute, value2);
				ColumnNameValue cnv2 = ColumnNameValue.build(av2);
				return Arrays.asList(cnv1, cnv2);
			} else
				applyBetweenValuesOperator(getOperator(betweenValuesPredicate.getPredicateType()), pathImpl, value1,
						value2, entity, sb);

			break;

		case LIKE_PATTERN:
			LikePatternPredicate likePatternPredicate = (LikePatternPredicate) predicate;
			String pattern = likePatternPredicate.getPattern();
			pathImpl = (PathImpl<?>) likePatternPredicate.getX();
			applyLikePatternOperator(getOperator(likePatternPredicate.getPredicateType()), pathImpl, pattern, entity,
					sb);

			break;

		case LIKE_PATTERN_EXPR:
			LikePatternExprPredicate likePatternExprPredicate = (LikePatternExprPredicate) predicate;
			Expression<String> patternExpr = likePatternExprPredicate.getPatternEx();
			pathImpl = (PathImpl<?>) likePatternExprPredicate.getX();
			applyLikePatternExprOperator(getOperator(likePatternExprPredicate.getPredicateType()), pathImpl,
					patternExpr, entity, sb);

			break;

		case OR:
		case AND:
			predicateImpl = (PredicateImpl) predicate;
			applyBinaryOperator(getOperator(predicateImpl.getPredicateType()), predicateImpl, entity, sb);
			break;

		case NOT:
			predicateImpl = (PredicateImpl) predicate;
			applyPrefixUnaryOperator(getOperator(predicateImpl.getPredicateType()), predicateImpl, entity, sb);
			break;

		case IS_NULL:
		case IS_NOT_NULL:
		case IS_TRUE:
		case IS_FALSE:
			predicateImpl = (PredicateImpl) predicate;
			pathImpl = (PathImpl<?>) predicateImpl.getX();
			applyPostfixUnaryOperator(getOperator(predicateImpl.getPredicateType()), pathImpl, entity, sb);
			break;

		case EMPTY_CONJUNCTION:
		case EMPTY_DISJUNCTION:
			predicateImpl = (PredicateImpl) predicate;
			applyCondition(getOperator(predicateImpl.getPredicateType()), sb);
			break;
		}

		return new ArrayList<ColumnNameValue>();
	}

	private void applyBinaryOperator(String operator, PathImpl<?> pathImpl, Object value, MetaEntity entity,
			StringBuilder sb) {
		String attributeName = pathImpl.getAttributeName();
		String columnName = entity.getAttribute(attributeName).getColumnName();
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(entity.getAlias(), columnName));
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

	private void applyBetweenValuesOperator(String operator, PathImpl<?> pathImpl, Object value1, Object value2,
			MetaEntity entity, StringBuilder sb) {
		String attributeName = pathImpl.getAttributeName();
		String columnName = entity.getAttribute(attributeName).getColumnName();
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(entity.getAlias(), columnName));
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

	private void applyBetweenExpressionsOperator(String operator, PathImpl<?> pathImpl, Expression<?> x,
			Expression<?> y, MetaEntity entity, StringBuilder sb) {
		String attributeName = pathImpl.getAttributeName();
		String columnName = entity.getAttribute(attributeName).getColumnName();
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(entity.getAlias(), columnName));
		sb.append(" ");
		sb.append(operator);
		sb.append(" ");
		sb.append(calculateExpression(x));
		sb.append(" AND ");
		sb.append(calculateExpression(y));
	}

	private void applyQMBinaryOperator(String operator, MetaAttribute metaAttribute, MetaEntity entity,
			StringBuilder sb) {
		String columnName = metaAttribute.getColumnName();
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(entity.getAlias(), columnName));
		sb.append(" ");
		sb.append(operator);
		sb.append(" ?");
	}

	private void applyQMBetweenValuesOperator(String operator, MetaAttribute metaAttribute, MetaEntity entity,
			StringBuilder sb) {
		String columnName = metaAttribute.getColumnName();
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(entity.getAlias(), columnName));
		sb.append(" ");
		sb.append(operator);
		sb.append(" ? AND ?");
	}

	private void applyBinaryOperator(String operator, PredicateImpl predicateImpl, MetaEntity entity,
			StringBuilder sb) {
		List<Expression<Boolean>> expressions = predicateImpl.getExpressions();
		int i = 0;
		for (Expression<Boolean> expression : expressions) {
			sb.append(" ");
			if (i > 0) {
				sb.append(operator);
			}

			sb.append(" (");
			PredicateImpl p = (PredicateImpl) expression;
			createExpressionString(p, entity, sb);
			sb.append(")");
			++i;
		}
	}

	private void applyPrefixUnaryOperator(String operator, PredicateImpl predicateImpl, MetaEntity entity,
			StringBuilder sb) {
		List<Expression<Boolean>> expressions = predicateImpl.getExpressions();
		sb.append(" ");
		sb.append(operator);
		sb.append(" (");
		for (Expression<Boolean> expression : expressions) {
			PredicateImpl p = (PredicateImpl) expression;
			createExpressionString(p, entity, sb);
		}

		sb.append(")");
	}

	private void applyPostfixUnaryOperator(String operator, PathImpl<?> pathImpl, MetaEntity entity, StringBuilder sb) {
		String attributeName = pathImpl.getAttributeName();
		String columnName = entity.getAttribute(attributeName).getColumnName();
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(entity.getAlias(), columnName));
		sb.append(" ");
		sb.append(operator);
	}

	private void applyCondition(String condition, StringBuilder sb) {
		sb.append(" ");
		sb.append(condition);
	}

	private String calculateExpression(Expression<?> expression) {
		PathImpl<?> pathImpl = (PathImpl<?>) expression;
		return pathImpl.getAttributeName();
	}

	private void applyLikePatternExprOperator(String operator, PathImpl<?> pathImpl, Expression<String> pattern,
			MetaEntity entity, StringBuilder sb) {
		String attributeName = pathImpl.getAttributeName();
		String columnName = entity.getAttribute(attributeName).getColumnName();
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(entity.getAlias(), columnName));
		sb.append(" ");
		sb.append(operator);
		sb.append(" '");
		sb.append(calculateExpression(pattern));
		sb.append("'");
	}

	private void applyLikePatternOperator(String operator, PathImpl<?> pathImpl, String pattern, MetaEntity entity,
			StringBuilder sb) {
		String attributeName = pathImpl.getAttributeName();
		String columnName = entity.getAttribute(attributeName).getColumnName();
		sb.append(" ");
		sb.append(dbJdbc.getNameTranslator().toColumnName(entity.getAlias(), columnName));
		sb.append(" ");
		sb.append(operator);
		sb.append(" '");
		sb.append(pattern);
		sb.append("'");
	}

}
