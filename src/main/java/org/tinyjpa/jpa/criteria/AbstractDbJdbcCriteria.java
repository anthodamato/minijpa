package org.tinyjpa.jpa.criteria;

import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.ColumnNameValue;
import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jdbc.db.AbstractDbJdbc;

public abstract class AbstractDbJdbcCriteria extends AbstractDbJdbc implements DbJdbcCriteria {
	private Logger LOG = LoggerFactory.getLogger(AbstractDbJdbcCriteria.class);

	@Override
	public SqlStatement select(CriteriaQuery<?> criteriaQuery, Map<String, MetaEntity> entities) throws Exception {
		LOG.info("select: this=" + this);
		List<Expression<Boolean>> restrictions = ((CriteriaQueryImpl) criteriaQuery).getRestrictions();
		LOG.info("select: restrictions=" + restrictions);
		if (!restrictions.isEmpty()) {
			Expression<Boolean> expression = restrictions.get(0);
			if (expression instanceof PredicateImpl) {
				PredicateImpl predicateImpl = (PredicateImpl) expression;
				LOG.info("select: predicateImpl.getPredicateType()=" + predicateImpl.getPredicateType());
				LOG.info("select: criteriaQuery.getResultType()=" + criteriaQuery.getResultType());
				MetaEntity entity = entities.get(criteriaQuery.getResultType().getName());

				List<MetaAttribute> expandedAttributes = entity.getId().expand();
				expandedAttributes.addAll(entity.expandAttributes());
				StringBuilder sb = createAllFieldsQuery(entity, expandedAttributes);
				sb.append(" where");
				createExpressionString(predicateImpl, entity, sb);
				LOG.info("select: sb.toString()=" + sb.toString());
				List<ColumnNameValue> fetchColumnNameValues = convertAttributes(expandedAttributes);
				String sql = sb.toString();
				return new SqlStatement.Builder().withSql(sql).withFetchColumnNameValues(fetchColumnNameValues).build();
			}
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
		}

		return "";
	}

	private void createExpressionString(PredicateImpl predicateImpl, MetaEntity entity, StringBuilder sb) {
		switch (predicateImpl.getPredicateType()) {
		case EQUAL:
		case NOT_EQUAL:
			Expression<?> expression = predicateImpl.getX();
			LOG.info("createExpressionString: expression=" + expression);
			Object value = predicateImpl.getValue();
			LOG.info("createExpressionString: object=" + value);
			if (expression instanceof PathImpl) {
				PathImpl<?> pathImpl = (PathImpl<?>) expression;
				applyBinaryOperator(getOperator(predicateImpl.getPredicateType()), pathImpl, value, entity, sb);
			}
			break;

		case OR:
		case AND:
			applyBinaryOperator(getOperator(predicateImpl.getPredicateType()), predicateImpl, entity, sb);
			break;

		case NOT:
			applyPrefixUnaryOperator(getOperator(predicateImpl.getPredicateType()), predicateImpl, entity, sb);
			break;

		case IS_NULL:
		case IS_NOT_NULL:
		case IS_TRUE:
		case IS_FALSE:
			expression = predicateImpl.getX();
			if (expression instanceof PathImpl) {
				PathImpl<?> pathImpl = (PathImpl<?>) expression;
				applyPostfixUnaryOperator(getOperator(predicateImpl.getPredicateType()), pathImpl, entity, sb);
			}
			break;
		}
	}

	private void applyBinaryOperator(String operator, PathImpl<?> pathImpl, Object value, MetaEntity entity,
			StringBuilder sb) {
		String attributeName = pathImpl.getAttributeName();
		LOG.info("expressionValueString: attributeName=" + attributeName);
		String columnName = entity.getAttribute(attributeName).getColumnName();
		sb.append(" ");
		sb.append(getNameTranslator().toColumnName(entity.getAlias(), columnName));
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
		sb.append(getNameTranslator().toColumnName(entity.getAlias(), columnName));
		sb.append(" ");
		sb.append(operator);
	}

}
