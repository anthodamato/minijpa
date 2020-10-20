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

	private void createExpressionString(PredicateImpl predicateImpl, MetaEntity entity, StringBuilder sb) {
		if (predicateImpl.getPredicateType() == PredicateType.EQUAL) {
			Expression<?> expression = predicateImpl.getX();
			LOG.info("createExpressionString: expression=" + expression);
			Object value = predicateImpl.getValue();
			LOG.info("createExpressionString: object=" + value);
			if (expression instanceof PathImpl) {
				PathImpl<?> pathImpl = (PathImpl<?>) expression;
				applyBinaryOperator(equalOperator(), pathImpl, value, entity, sb);
			}
		} else if (predicateImpl.getPredicateType() == PredicateType.NOT_EQUAL) {
			Expression<?> expression = predicateImpl.getX();
			LOG.info("createExpressionString: expression=" + expression);
			Object value = predicateImpl.getValue();
			LOG.info("createExpressionString: object=" + value);
			if (expression instanceof PathImpl) {
				PathImpl<?> pathImpl = (PathImpl<?>) expression;
				applyBinaryOperator(notEqualOperator(), pathImpl, value, entity, sb);
			}
		} else if (predicateImpl.getPredicateType() == PredicateType.OR) {
			applyBinaryOperator(orOperator(), predicateImpl, entity, sb);
		} else if (predicateImpl.getPredicateType() == PredicateType.AND) {
			applyBinaryOperator(andOperator(), predicateImpl, entity, sb);
		} else if (predicateImpl.getPredicateType() == PredicateType.NOT) {
			applyPrefixUnaryOperator(notOperator(), predicateImpl, entity, sb);
		} else if (predicateImpl.getPredicateType() == PredicateType.IS_NULL) {
			Expression<?> expression = predicateImpl.getX();
			if (expression instanceof PathImpl) {
				PathImpl<?> pathImpl = (PathImpl<?>) expression;
				applyPostfixUnaryOperator(isNullOperator(), pathImpl, entity, sb);
			}
		} else if (predicateImpl.getPredicateType() == PredicateType.IS_NOT_NULL) {
			Expression<?> expression = predicateImpl.getX();
			if (expression instanceof PathImpl) {
				PathImpl<?> pathImpl = (PathImpl<?>) expression;
				applyPostfixUnaryOperator(isNotNullOperator(), pathImpl, entity, sb);
			}
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
