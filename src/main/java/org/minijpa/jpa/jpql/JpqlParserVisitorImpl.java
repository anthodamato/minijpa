package org.minijpa.jpa.jpql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.minijpa.jdbc.AttributeUtil;
import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.MetaEntityHelper;
import org.minijpa.jdbc.model.FromTable;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.model.Value;
import org.minijpa.jdbc.model.condition.BinaryCondition;
import org.minijpa.jdbc.model.condition.Condition;
import org.minijpa.jdbc.model.condition.ConditionType;
import org.minijpa.jdbc.model.condition.NestedCondition;
import org.minijpa.jdbc.model.condition.NotCondition;
import org.minijpa.jdbc.model.join.FromJoin;
import org.minijpa.jpa.db.SqlStatementFactory;
import org.minijpa.metadata.PersistenceUnitContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpqlParserVisitorImpl implements JpqlParserVisitor {

	private final Logger LOG = LoggerFactory.getLogger(JpqlParserVisitorImpl.class);

	private final PersistenceUnitContext persistenceUnitContext;
	private final SqlStatementFactory sqlStatementFactory;

	public JpqlParserVisitorImpl(PersistenceUnitContext persistenceUnitContext, SqlStatementFactory sqlStatementFactory) {
		this.persistenceUnitContext = persistenceUnitContext;
		this.sqlStatementFactory = sqlStatementFactory;
	}

	@Override
	public Object visit(ASTQLStatement node, Object data) {
		LOG.debug("visit: ASTQLStatement data=" + data);
		LOG.debug("visit: ASTQLStatement node=" + node);
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTSelectStatement node, Object data) {
		JpqlVisitorParameters jpqlVisitorParameters = new JpqlVisitorParameters();
		SqlSelect.SqlSelectBuilder selectBuilder = new SqlSelect.SqlSelectBuilder();

		ASTSelectClause selectClause = (ASTSelectClause) node.jjtGetChild(0);
		if (selectClause.isDistinct())
			selectBuilder.distinct();

		ASTSelectItem selectItem = (ASTSelectItem) selectClause.jjtGetChild(0);
		String alias = selectItem.getAlias();
		LOG.debug("visit: alias=" + alias);
		ASTSelectExpression selectExpression = (ASTSelectExpression) selectItem.jjtGetChild(0);
		String identificationVariable = selectExpression.getIdentificationVariable();
		LOG.debug("visit: identificationVariable=" + identificationVariable);

		LOG.debug("visit: node.jjtGetNumChildren()=" + node.jjtGetNumChildren());
		ASTFromClause fromClause = (ASTFromClause) node.jjtGetChild(1);
		LOG.debug("visit: fromClause=" + fromClause);
		ASTIdentificationVariableDeclaration identificationVariableDeclaration = (ASTIdentificationVariableDeclaration) fromClause.jjtGetChild(0);
		ASTRangeVariableDeclaration rangeVariableDeclaration = (ASTRangeVariableDeclaration) identificationVariableDeclaration.jjtGetChild(0);
		String rvdEntityName = rangeVariableDeclaration.getEntityName();
		LOG.debug("visit: rvdEntityName=" + rvdEntityName);
		String rvdAlias = rangeVariableDeclaration.getAlias();
		LOG.debug("visit: rvdAlias=" + rvdAlias);
		Optional<MetaEntity> optional = persistenceUnitContext.findMetaEntityByName(rvdEntityName);
		MetaEntity sourceEntity = optional.get();
		selectBuilder.withResult(sourceEntity);
		jpqlVisitorParameters.aliases.put(rvdAlias, sourceEntity.getAlias());

		fillFetchParameters(jpqlVisitorParameters, selectClause);
		List<Object> list = (List< Object>) node.childrenAccept(this, jpqlVisitorParameters);

		selectBuilder.withFromTable(jpqlVisitorParameters.fromTable);
		if (jpqlVisitorParameters.fromJoins != null)
			selectBuilder.withJoins(jpqlVisitorParameters.fromJoins);

		selectBuilder.withValues(jpqlVisitorParameters.values);
		selectBuilder.withFetchParameters(jpqlVisitorParameters.fetchParameters);

//		list.forEach(v -> LOG.debug("visit: ASTSelectStatement v=" + v));
		if (node.jjtGetNumChildren() > 2) {
			Node n2 = node.jjtGetChild(2);
			if (n2 instanceof ASTWhereClause) {
				Condition condition = (Condition) list.get(2);
				LOG.debug("visit: ASTSelectStatement condition=" + condition);
				selectBuilder.withConditions(Arrays.asList(condition));
			}
		}

		return selectBuilder.build();
	}

	private void fillFetchParameters(JpqlVisitorParameters jpqlVisitorParameters, ASTSelectClause selectClause) {
		for (int i = 0; i < selectClause.jjtGetNumChildren(); ++i) {
			ASTSelectItem selectItem = (ASTSelectItem) selectClause.jjtGetChild(i);
			String result_variable = selectItem.getAlias();
			LOG.debug("fillFetchParameters: result_variable=" + result_variable);
			ASTSelectExpression selectExpression = (ASTSelectExpression) selectItem.jjtGetChild(0);
			String identificationVariable = selectExpression.getIdentificationVariable();
			LOG.debug("fillFetchParameters: identificationVariable=" + identificationVariable);
			if (identificationVariable != null) {
				MetaEntity metaEntity = findMetaEntityByJpqlAlias(identificationVariable, jpqlVisitorParameters.aliases);
				List<Value> values = MetaEntityHelper.toValues(metaEntity.getId().getAttributes(), FromTable.of(metaEntity));
				jpqlVisitorParameters.values = values;

				List<FetchParameter> fetchParameters = new ArrayList<>();
				metaEntity.getId().getAttributes().forEach(v -> {
					fetchParameters.add(MetaEntityHelper.toFetchParameter(v));
				});
				jpqlVisitorParameters.fetchParameters = fetchParameters;
			}
		}
	}

	private MetaEntity findMetaEntityByJpqlAlias(String jpqlAlias, Map<String, String> aliases) {
		String entityAlias = aliases.get(jpqlAlias);
		Optional<MetaEntity> optional = persistenceUnitContext.findMetaEntityByAlias(entityAlias);
		if (optional.isEmpty())
			throw new SemanticException("Entity not found for alias '" + jpqlAlias + "'");

		return optional.get();
	}

	@Override
	public Object visit(ASTFromClause node, Object data) {
		Object object = node.childrenAccept(this, data);
		return object;
	}

	@Override
	public Object visit(ASTSelectClause node, Object data) {
		LOG.debug("visit: ASTSelectClause node.jjtGetValue()=" + node.jjtGetValue());
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTSelectExpression node, Object data) {
		LOG.debug("visit: ASTSelectExpression data=" + data);
		LOG.debug("visit: ASTSelectExpression node.jjtGetValue()=" + node.jjtGetValue());
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTSelectItem node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTRangeVariableDeclaration node, Object data) {
		Optional<MetaEntity> optional = persistenceUnitContext.findMetaEntityByName(node.getEntityName());
		if (optional.isEmpty())
			throw new SemanticException("Entity name '" + node.getEntityName() + "' not found");

		JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
		MetaEntity metaEntity = optional.get();
		FromTable fromTable = FromTable.of(metaEntity);
		jpqlVisitorParameters.fromTable = fromTable;

		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTIdentificationVariableDeclaration node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTSubtype node, Object data) {
		LOG.debug("visit: ASTSubtype data=" + data);
		LOG.debug("visit: ASTSubtype node=" + node);
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTJoin node, Object data) {
		Object object = node.childrenAccept(this, data);
		JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;

		JoinType jt = node.getJoinType();
		LOG.debug("visit: join.jjtGetNumChildren()=" + node.jjtGetNumChildren());
		ASTJoinAssociationPathExpression joinAssociationPathExpression = (ASTJoinAssociationPathExpression) node.jjtGetChild(0);
		String joinSingleValuedPathExpression = joinAssociationPathExpression.getJoinSingleValuedPathExpression();
		LOG.debug("visit: joinSingleValuedPathExpression=" + joinSingleValuedPathExpression);
		String joinAlias = node.getIdentificationVariable();
		LOG.debug("visit: joinAlias=" + joinAlias);
		String[] rPE = joinSingleValuedPathExpression.split("\\.");
		LOG.debug("visit: rPE[0]=" + rPE[0]);
		String attributePath = joinSingleValuedPathExpression.substring(rPE[0].length() + 1);

		MetaEntity metaEntity = findMetaEntityByJpqlAlias(rPE[0], jpqlVisitorParameters.aliases);
		MetaAttribute metaAttribute
				= AttributeUtil.findAttributeFromPath(attributePath, metaEntity);
		if (metaAttribute == null)
			throw new SemanticException("Attribute path '" + attributePath + "' on '" + metaEntity.getName() + "' entity not found");

		if (metaAttribute.getRelationship() == null)
			throw new SemanticException("Attribute '" + metaAttribute.getName() + "' is not a relationship attribute");

		if (metaAttribute.getRelationship().getJoinTable() != null) {
			jpqlVisitorParameters.aliases.put(joinAlias, metaAttribute.getRelationship().getJoinTable().getTargetEntity().getAlias());
			List<FromJoin> fromJoins = sqlStatementFactory.calculateJoins(metaEntity, metaAttribute);
			jpqlVisitorParameters.fromJoins.addAll(fromJoins);
		} else if (metaAttribute.getRelationship().getJoinColumnMapping().isPresent()) {
			jpqlVisitorParameters.aliases.put(joinAlias, metaAttribute.getRelationship().getAttributeType().getAlias());
			List<FromJoin> fromJoins = sqlStatementFactory.calculateJoins(metaEntity, metaAttribute);
			jpqlVisitorParameters.fromJoins.addAll(fromJoins);
		}

		return object;
	}

	@Override
	public Object visit(ASTJoinAssociationPathExpression node, Object data) {
		Object object = node.childrenAccept(this, data);
		return object;
	}

	@Override
	public Object visit(ASTJoinSingleValuedPathExpression node, Object data) {
		LOG.debug("visit: ASTJoinSingleValuedPathExpression node.jjtGetValue()=" + node.jjtGetValue());
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTMapFieldIdentificationVariable node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTGeneralIdentificationVariable node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTSimpleSubpath node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTGeneralSubpath node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTTreatedSubpath node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTStateFieldPathExpression node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTStateValuedPathExpression node, Object data) {
		Object object = node.childrenAccept(this, data);
		LOG.debug("visit: ASTStateValuedPathExpression node.jjtGetNumChildren()=" + node.jjtGetNumChildren());
		if (node.jjtGetNumChildren() == 0)
			return object;

		JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
		Node n0_0_0 = node.jjtGetChild(0);
		LOG.debug("visit: ASTStateValuedPathExpression n0_0_0=" + n0_0_0);
		if (n0_0_0 instanceof ASTStateFieldPathExpression) {
			ASTStateFieldPathExpression stateFieldPathExpression = (ASTStateFieldPathExpression) n0_0_0;
			ASTGeneralSubpath generalSubpath = (ASTGeneralSubpath) stateFieldPathExpression.jjtGetChild(0);
			String stateField = stateFieldPathExpression.getStateField();
			LOG.debug("visit: ASTStateValuedPathExpression stateField=" + stateField);
			Node n0_0_0_0 = generalSubpath.jjtGetChild(0);
			LOG.debug("visit: ASTStateValuedPathExpression n0_0_0_0=" + n0_0_0_0);
			if (n0_0_0_0 instanceof ASTSimpleSubpath) {
				ASTSimpleSubpath simpleSubpath = (ASTSimpleSubpath) n0_0_0_0;
				ASTGeneralIdentificationVariable generalIdentificationVariable = (ASTGeneralIdentificationVariable) simpleSubpath.jjtGetChild(0);
				if (generalIdentificationVariable.jjtGetNumChildren() == 0) {
					String identificationVariable = generalIdentificationVariable.getIdentificationVariable();

					MetaEntity metaEntity = findMetaEntityByJpqlAlias(identificationVariable, jpqlVisitorParameters.aliases);
					MetaAttribute metaAttribute
							= AttributeUtil.findAttributeFromPath(stateField, metaEntity);
					if (metaAttribute == null)
						throw new SemanticException("Attribute path '" + stateField + "' on '" + metaEntity.getName() + "' entity not found");

					String columnName = metaAttribute.getColumnName();
					LOG.debug("visit: ASTStateValuedPathExpression identificationVariable=" + identificationVariable);
					String r = replaceAlias(jpqlVisitorParameters.aliases, identificationVariable) + "." + columnName;
					LOG.debug("visit: ASTStateValuedPathExpression r=" + r);
					node.setPath(r);
				}
			}
		} else {
			ASTGeneralIdentificationVariable generalIdentificationVariable = (ASTGeneralIdentificationVariable) n0_0_0;
			String identificationVariable = generalIdentificationVariable.getIdentificationVariable();
			String r = identificationVariable;
			node.setPath(r);
		}

		return object;
	}

	@Override
	public Object visit(ASTWhenClause node, Object data) {
		return node.childrenAccept(this, data);
	}

	private String replaceAlias(Map<String, String> aliases, String oldAlias) {
		String newAlias = aliases.get(oldAlias);
		if (newAlias == null)
			return oldAlias;

		return newAlias;
	}

	@Override
	public Object visit(ASTStringExpression node, Object data) {
		LOG.debug("visit: ASTStringExpression node=" + node);
		Object object = node.childrenAccept(this, data);
		LOG.debug("visit: ASTStringExpression node.jjtGetNumChildren()=" + node.jjtGetNumChildren());
		if (node.jjtGetNumChildren() == 0)
			return object;

//		JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
		Node n0_0 = node.jjtGetChild(0);
		LOG.debug("visit: ASTStringExpression n0_0=" + n0_0);
		if (n0_0 instanceof ASTStateValuedPathExpression) {
			ASTStateValuedPathExpression stateValuedPathExpression = (ASTStateValuedPathExpression) n0_0;
			node.setPath(stateValuedPathExpression.getPath());
		}

		LOG.debug("visit: ASTStringExpression object=" + object);
		return object;
	}

	private ConditionType decodeConditionType(String comparisonOperator) {
		if (comparisonOperator.equals(">"))
			return ConditionType.GREATER_THAN;
		if (comparisonOperator.equals(">="))
			return ConditionType.GREATER_THAN_OR_EQUAL_TO;
		if (comparisonOperator.equals("<"))
			return ConditionType.LESS_THAN;
		if (comparisonOperator.equals("<="))
			return ConditionType.LESS_THAN_OR_EQUAL_TO;
		if (comparisonOperator.equals("<>"))
			return ConditionType.NOT_EQUAL;
		if (comparisonOperator.equals("="))
			return ConditionType.EQUAL;
		return null;
	}

	private Object decodeOperand(String operand) {
		if (operand.equalsIgnoreCase("FALSE"))
			return Boolean.FALSE;

		if (operand.equalsIgnoreCase("TRUE"))
			return Boolean.TRUE;

		return operand;
	}

	private Object decodeStringExpression(ASTStringExpression stringExpression) {
		if (stringExpression.getPath() != null)
			return decodeOperand(stringExpression.getPath());

		if (stringExpression.getStringLiteral() != null)
			return stringExpression.getStringLiteral();

		if (stringExpression.getInputParameter() != null)
			return stringExpression.getInputParameter();

		return "";
	}

	@Override
	public Object visit(ASTComparisonExpression node, Object data) {
		LOG.debug("visit: ASTComparisonExpression data=" + data);
		LOG.debug("visit: ASTComparisonExpression node=" + node);
		LOG.debug("visit: ASTComparisonExpression node.jjtGetNumChildren()=" + node.jjtGetNumChildren());
		Object object = node.childrenAccept(this, data);
		LOG.debug("visit: ASTComparisonExpression object=" + object);
		Node n0 = node.jjtGetChild(0);
		LOG.debug("visit: ASTComparisonExpression n0=" + n0);
		if (n0 instanceof ASTStringExpression) {
			ASTStringExpression stringExpression0 = (ASTStringExpression) n0;

			String comparisonOperator = node.getComparisonOperator();
			Node n1 = node.jjtGetChild(1);
			if (n1 instanceof ASTStringExpression) {
				ASTStringExpression stringExpression1 = (ASTStringExpression) n1;
				BinaryCondition.Builder builder = new BinaryCondition.Builder(decodeConditionType(comparisonOperator));
				builder.withLeft(decodeStringExpression(stringExpression0));
				builder.withRight(decodeStringExpression(stringExpression1));
				return builder.build();
			}
		} else if (n0 instanceof ASTArithmeticExpression) {
			ASTArithmeticExpression expression0 = (ASTArithmeticExpression) n0;

			String comparisonOperator = node.getComparisonOperator();
			LOG.debug("visit: ASTComparisonExpression comparisonOperator=" + comparisonOperator);
			Node n1 = node.jjtGetChild(1);
			if (n1 instanceof ASTArithmeticExpression) {
				ASTArithmeticExpression expression1 = (ASTArithmeticExpression) n1;
				BinaryCondition.Builder builder = new BinaryCondition.Builder(decodeConditionType(comparisonOperator));
				builder.withLeft(expression0.getValue());
				builder.withRight(expression1.getValue());
				return builder.build();
			}
		}

		return object;
	}

	@Override
	public Object visit(ASTSimpleCondExpression node, Object data) {
		Object object = node.childrenAccept(this, data);
		LOG.debug("visit: ASTSimpleCondExpression object=" + object);
		return object;
	}

	@Override
	public Object visit(ASTConditionalPrimary node, Object data) {
		LOG.debug("visit: ASTConditionalPrimary data=" + data);
		Condition condition = (Condition) node.childrenAccept(this, data);
		LOG.debug("visit: ASTConditionalPrimary condition=" + condition);
		Node n = node.jjtGetChild(0);
		if (n instanceof ASTSimpleCondExpression)
			return condition;

		return new NestedCondition(condition);
	}

	@Override
	public Object visit(ASTConditionalFactor node, Object data) {
		LOG.debug("visit: ASTConditionalFactor data=" + data);
		Condition condition = (Condition) node.childrenAccept(this, data);
		LOG.debug("visit: ASTConditionalFactor condition=" + condition);
		if (node.isNot())
			return new NotCondition(condition);

		return condition;
	}

	@Override
	public Object visit(ASTConditionalTerm node, Object data) {
		Object object = node.childrenAccept(this, data);
		LOG.debug("visit: ASTConditionalTerm object=" + object);
		return object;
	}

	@Override
	public Object visit(ASTConditionalExpression node, Object data) {
		Object object = node.childrenAccept(this, data);
		LOG.debug("visit: ASTConditionalExpression object=" + object);
		return object;
	}

	@Override
	public Object visit(ASTWhereClause node, Object data) {
		Object object = node.childrenAccept(this, data);
		LOG.debug("visit: ASTWhereClause object=" + object);
		return object;
	}

	@Override
	public Object visit(ASTHavingClause node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTSingleValuedObjectPathExpression node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTCollectionValuedPathExpression node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTArithmeticPrimary node, Object data) {
		Object object = node.childrenAccept(this, data);
		if (node.jjtGetNumChildren() == 0)
			return object;

		Node n0_0 = node.jjtGetChild(0);
		LOG.debug("visit: ASTArithmeticPrimary n0_0=" + n0_0);
		if (n0_0 instanceof ASTStateValuedPathExpression) {
			ASTStateValuedPathExpression stateValuedPathExpression = (ASTStateValuedPathExpression) n0_0;
			node.setValue(stateValuedPathExpression.getPath());
		}

		return object;
	}

	@Override
	public Object visit(ASTArithmeticFactor node, Object data) {
		Object object = node.childrenAccept(this, data);
		ASTArithmeticPrimary n0_0 = (ASTArithmeticPrimary) node.jjtGetChild(0);
		node.setValue(n0_0.getValue());
		return object;
	}

	@Override
	public Object visit(ASTArithmeticTerm node, Object data) {
		Object object = node.childrenAccept(this, data);
		StringBuilder sb = new StringBuilder();
		ASTArithmeticFactor n0_0 = (ASTArithmeticFactor) node.jjtGetChild(0);
		sb.append(n0_0.getSign());
		sb.append(n0_0.getValue());
		for (int i = 1; i < node.jjtGetNumChildren(); ++i) {
			ASTArithmeticFactor n_i = (ASTArithmeticFactor) node.jjtGetChild(i);
			sb.append(node.getSigns().get(i - 1));
			sb.append(n_i.getSign());
			sb.append(n_i.getValue());
		}

		LOG.debug("visit: ASTArithmeticTerm sb.toString()=" + sb.toString());
		node.setValue(sb.toString());
		return object;
	}

	@Override
	public Object visit(ASTArithmeticExpression node, Object data) {
		Object object = node.childrenAccept(this, data);
		StringBuilder sb = new StringBuilder();
		ASTArithmeticTerm n0_0 = (ASTArithmeticTerm) node.jjtGetChild(0);
		LOG.debug("visit: ASTArithmeticExpression n0_0.getValue()=" + n0_0.getValue());
		sb.append(n0_0.getValue());
		for (int i = 1; i < node.jjtGetNumChildren(); ++i) {
			ASTArithmeticTerm n_i = (ASTArithmeticTerm) node.jjtGetChild(i);
			sb.append(node.getSigns().get(i - 1));
			sb.append(n_i.getValue());
		}

		LOG.debug("visit: ASTArithmeticExpression sb.toString()=" + sb.toString());
		node.setValue(sb.toString());
		return object;
	}

	@Override
	public Object visit(SimpleNode node, Object data) {
		LOG.debug("visit: SimpleNode data=" + data);
		LOG.debug("visit: SimpleNode node=" + node);
		return node.childrenAccept(this, data);
	}

}
