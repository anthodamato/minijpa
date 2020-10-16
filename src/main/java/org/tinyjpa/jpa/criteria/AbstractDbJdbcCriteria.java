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
				if (predicateImpl.getPredicateType() == PredicateType.EQUAL) {
					LOG.info("select: criteriaQuery.getResultType()=" + criteriaQuery.getResultType());
					MetaEntity entity = entities.get(criteriaQuery.getResultType().getName());

					List<MetaAttribute> expandedAttributes = entity.getId().expand();
					expandedAttributes.addAll(entity.expandAttributes());
					StringBuilder sb = createAllFieldsQuery(entity, expandedAttributes);
					sb.append(" where ");
					sb.append(createExpressionString(predicateImpl, entity));
					List<ColumnNameValue> fetchColumnNameValues = convertAttributes(expandedAttributes);
					String sql = sb.toString();
					return new SqlStatement.Builder().withSql(sql).withFetchColumnNameValues(fetchColumnNameValues)
							.build();
				}
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

	private String createExpressionString(PredicateImpl predicateImpl, MetaEntity entity) {
		StringBuilder sb = new StringBuilder();
		if (predicateImpl.getPredicateType() == PredicateType.EQUAL) {
			Expression<?> expression = predicateImpl.getX();
			LOG.info("createExpressionString: expression=" + expression);
			Object object = predicateImpl.getY();
			LOG.info("createExpressionString: object=" + object);
			if (expression instanceof PathImpl) {
				PathImpl<?> pathImpl = (PathImpl<?>) expression;
				String attributeName = pathImpl.getAttributeName();
				LOG.info("createExpressionString: attributeName=" + attributeName);
				String columnName = entity.getAttribute(attributeName).getColumnName();
				sb.append(getNameTranslator().toColumnName(entity.getAlias(), columnName));
				sb.append("=");
				if (object instanceof String) {
					sb.append("'");
					sb.append((String) object);
					sb.append("'");
				}
			}
		}

		return sb.toString();
	}
}
