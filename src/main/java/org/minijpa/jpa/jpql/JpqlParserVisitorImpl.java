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
import org.minijpa.jdbc.model.condition.BinaryLogicConditionImpl;
import org.minijpa.jdbc.model.condition.Condition;
import org.minijpa.jdbc.model.condition.ConditionType;
import org.minijpa.jdbc.model.condition.NestedCondition;
import org.minijpa.jdbc.model.condition.NotCondition;
import org.minijpa.jdbc.model.expression.SqlExpressionImpl;
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
		jpqlVisitorParameters.aliases.put(rvdAlias, sourceEntity.getAlias());

		node.childrenAccept(this, jpqlVisitorParameters);
		if (jpqlVisitorParameters.identificationVariableEntity != null
				&& jpqlVisitorParameters.identificationVariableEntity == sourceEntity)
			selectBuilder.withResult(sourceEntity);

		selectBuilder.withFromTable(jpqlVisitorParameters.fromTable);
		if (jpqlVisitorParameters.fromJoins != null)
			selectBuilder.withJoins(jpqlVisitorParameters.fromJoins);

		selectBuilder.withValues(jpqlVisitorParameters.values);
		selectBuilder.withFetchParameters(jpqlVisitorParameters.fetchParameters);

		selectBuilder.withConditions(jpqlVisitorParameters.conditions);
		LOG.debug("visit: jpqlVisitorParameters.conditions=" + jpqlVisitorParameters.conditions);

		return selectBuilder.build();
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
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTSelectExpression node, Object data) {
		Object object = node.childrenAccept(this, data);
		JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
		LOG.debug("visit: ASTSelectExpression node.jjtGetNumChildren()=" + node.jjtGetNumChildren());

		if (node.jjtGetNumChildren() > 0) {
			Node node0 = node.jjtGetChild(0);
			LOG.debug("visit: ASTSelectExpression node0=" + node0);
			if (node0 instanceof ASTSingleValuedPathExpression) {
				ASTSingleValuedPathExpression singleValuedPathExpression = (ASTSingleValuedPathExpression) node0;
				Node node1 = singleValuedPathExpression.jjtGetChild(0);
				if (node1 instanceof ASTStateFieldPathExpression) {
					ASTStateFieldPathExpression stateFieldPathExpression = (ASTStateFieldPathExpression) node1;
					// identification variable.path.stateField
					String identificationVariable = stateFieldPathExpression.getIdentificationVariable();
					if (identificationVariable != null) {
						MetaEntity metaEntity = findMetaEntityByJpqlAlias(identificationVariable, jpqlVisitorParameters.aliases);
						String attributePath = stateFieldPathExpression.getStateField();
						if (!stateFieldPathExpression.getPath().isEmpty())
							attributePath = stateFieldPathExpression.getPath() + "." + stateFieldPathExpression.getStateField();

						if (AttributeUtil.isAttributePathPk(attributePath, metaEntity)) {
							List<Value> values = MetaEntityHelper.toValues(metaEntity.getId().getAttributes(), FromTable.of(metaEntity));
							node.setValues(values);

							List<FetchParameter> fetchParameters = new ArrayList<>();
							metaEntity.getId().getAttributes().forEach(v -> {
								fetchParameters.add(MetaEntityHelper.toFetchParameter(v));
							});
							node.setFetchParameters(fetchParameters);
						} else {
							MetaAttribute metaAttribute
									= AttributeUtil.findAttributeFromPath(attributePath, metaEntity);
							if (metaAttribute == null)
								throw new SemanticException("Attribute path '" + attributePath + "' on '" + metaEntity.getName() + "' entity not found");

							Value value = MetaEntityHelper.toValue(metaAttribute, FromTable.of(metaEntity));
							node.setValues(Arrays.asList(value));
							node.setFetchParameters(Arrays.asList(MetaEntityHelper.toFetchParameter(metaAttribute)));
						}
					}
				}
			} else if (node0 instanceof ASTScalarExpression) {
				Node node0_0 = node.jjtGetChild(0);
				LOG.debug("visit: ASTSelectExpression node0_0=" + node0_0);
				LOG.debug("visit: ASTSelectExpression node0_0.jjtGetNumChildren()=" + node0_0.jjtGetNumChildren());
				if (node0_0.jjtGetNumChildren() > 0)
					LOG.debug("visit: ASTSelectExpression node0_0.jjtGetChild(0)=" + node0_0.jjtGetChild(0));

				// ScalarExpression Node is duplicated. JavaCC bug?
				node0_0 = node0_0.jjtGetChild(0);

				if (node0_0 instanceof ASTArithmeticExpression) {
					ASTArithmeticExpression arithmeticExpression = (ASTArithmeticExpression) node0_0;
					Value value = new SqlExpressionImpl(arithmeticExpression.getResult());
					node.setValues(Arrays.asList(value));
				} else if (node0_0 instanceof ASTDatetimeExpression) {
					ASTDatetimeExpression datetimeExpression = (ASTDatetimeExpression) node0_0;
					Value value = new SqlExpressionImpl(decodeExpression(datetimeExpression));
					LOG.debug("visit: ASTSelectExpression value=" + value);
					node.setValues(Arrays.asList(value));
				} else if (node0_0 instanceof ASTStringExpression) {
					ASTStringExpression expression = (ASTStringExpression) node0_0;
					Value value = new SqlExpressionImpl(decodeExpression(expression));
					node.setValues(Arrays.asList(value));
				} else if (node0_0 instanceof ASTBooleanExpression) {
					ASTBooleanExpression expression = (ASTBooleanExpression) node0_0;
					Value value = new SqlExpressionImpl(decodeExpression(expression));
					node.setValues(Arrays.asList(value));
				}
			}
		} else {
			String identificationVariable = node.getIdentificationVariable();
			LOG.debug("visit: ASTSelectItem identificationVariable=" + identificationVariable);
			if (identificationVariable != null) {
				MetaEntity metaEntity = findMetaEntityByJpqlAlias(identificationVariable, jpqlVisitorParameters.aliases);
				List<Value> values = MetaEntityHelper.toValues(metaEntity.getId().getAttributes(), FromTable.of(metaEntity));
				node.setValues(values);

				List<FetchParameter> fetchParameters = new ArrayList<>();
				metaEntity.getId().getAttributes().forEach(v -> {
					fetchParameters.add(MetaEntityHelper.toFetchParameter(v));
				});
				node.setFetchParameters(fetchParameters);
				jpqlVisitorParameters.identificationVariableEntity = metaEntity;
			}
		}

		return object;
	}

	@Override
	public Object visit(ASTSelectItem node, Object data) {
		Object object = node.childrenAccept(this, data);
		JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;

		String result_variable = node.getAlias();
		LOG.debug("visit: ASTSelectItem result_variable=" + result_variable);
		ASTSelectExpression selectExpression = (ASTSelectExpression) node.jjtGetChild(0);
		List<Value> values = selectExpression.getValues();
		List<FetchParameter> fetchParameters = selectExpression.getFetchParameters();
		LOG.debug("visit: ASTSelectItem values=" + values);
		if (values != null) {
			jpqlVisitorParameters.values.addAll(values);
			if (result_variable != null && result_variable.length() > 0)
				jpqlVisitorParameters.resultVariables.put(result_variable, values);
		}

		if (fetchParameters != null)
			jpqlVisitorParameters.fetchParameters.addAll(fetchParameters);

		return object;
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
		LOG.debug("visit: ASTJoin data=" + data);
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
		Object object = node.childrenAccept(this, data);
		ASTGeneralIdentificationVariable generalIdentificationVariable = (ASTGeneralIdentificationVariable) node.jjtGetChild(0);
		node.setIdentificationVariable(generalIdentificationVariable.getIdentificationVariable());
		node.setMapFieldIdentificationVariable(generalIdentificationVariable.getMapFieldIdentificationVariable());
		return object;
	}

	@Override
	public Object visit(ASTGeneralSubpath node, Object data) {
		LOG.debug("visit: ASTGeneralSubpath");
		Object object = node.childrenAccept(this, data);
		Node node0 = node.jjtGetChild(0);
		if (node0 instanceof ASTSimpleSubpath) {
			ASTSimpleSubpath simpleSubpath = (ASTSimpleSubpath) node0;
			node.setIdentificationVariable(simpleSubpath.getIdentificationVariable());
			node.setMapFieldIdentificationVariable(simpleSubpath.getMapFieldIdentificationVariable());
			node.setPath(simpleSubpath.getPath());
		}

		return object;
	}

	@Override
	public Object visit(ASTTreatedSubpath node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTStateFieldPathExpression node, Object data) {
		Object object = node.childrenAccept(this, data);
		ASTGeneralSubpath generalSubpath = (ASTGeneralSubpath) node.jjtGetChild(0);
		node.setIdentificationVariable(generalSubpath.getIdentificationVariable());
		node.setMapFieldIdentificationVariable(generalSubpath.getMapFieldIdentificationVariable());
		node.setPath(generalSubpath.getPath());

		return object;
	}

	@Override
	public Object visit(ASTStateValuedPathExpression node, Object data) {
		Object object = node.childrenAccept(this, data);
		LOG.debug("visit: ASTStateValuedPathExpression node.jjtGetNumChildren()=" + node.jjtGetNumChildren());
		LOG.debug("visit: ASTStateValuedPathExpression data=" + data);
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

		Node n0_0 = node.jjtGetChild(0);
		LOG.debug("visit: ASTStringExpression n0_0=" + n0_0);
		if (n0_0 instanceof ASTStateValuedPathExpression) {
			ASTStateValuedPathExpression stateValuedPathExpression = (ASTStateValuedPathExpression) n0_0;
			node.setPath(stateValuedPathExpression.getPath());
		}

		return object;
	}

	@Override
	public Object visit(ASTStringExpressionComparison node, Object data) {
		Object object = node.childrenAccept(this, data);
		LOG.debug("visit: ASTStringExpressionComparison object=" + object);
		ASTStringExpression stringExpression0 = (ASTStringExpression) node.jjtGetChild(0);

		String comparisonOperator = node.getComparisonOperator();
		Node n1 = node.jjtGetChild(1);
		if (n1 instanceof ASTStringExpression) {
			ASTStringExpression stringExpression1 = (ASTStringExpression) n1;
			BinaryCondition.Builder builder = new BinaryCondition.Builder(decodeConditionType(comparisonOperator));
			builder.withLeft(decodeExpression(stringExpression0));
			builder.withRight(decodeExpression(stringExpression1));
			node.setCondition(builder.build());
		}

		return object;
	}

	@Override
	public Object visit(ASTBooleanExpression node, Object data) {
		Object object = node.childrenAccept(this, data);
		if (node.jjtGetNumChildren() == 0)
			return object;

		Node n0_0 = node.jjtGetChild(0);
		LOG.debug("visit: ASTBooleanExpression n0_0=" + n0_0);
		if (n0_0 instanceof ASTStateValuedPathExpression) {
			ASTStateValuedPathExpression stateValuedPathExpression = (ASTStateValuedPathExpression) n0_0;
			node.setPath(stateValuedPathExpression.getPath());
		}

		return object;
	}

	@Override
	public Object visit(ASTBooleanExpressionComparison node, Object data) {
		Object object = node.childrenAccept(this, data);
		ASTBooleanExpression stringExpression0 = (ASTBooleanExpression) node.jjtGetChild(0);

		String comparisonOperator = node.getComparisonOperator();
		Node n1 = node.jjtGetChild(1);
		if (n1 instanceof ASTBooleanExpression) {
			ASTBooleanExpression stringExpression1 = (ASTBooleanExpression) n1;
			BinaryCondition.Builder builder = new BinaryCondition.Builder(decodeConditionType(comparisonOperator));
			builder.withLeft(decodeExpression(stringExpression0));
			builder.withRight(decodeExpression(stringExpression1));
			node.setCondition(builder.build());
		}

		return object;
	}

	@Override
	public Object visit(ASTArithmeticExpressionComparison node, Object data) {
		Object object = node.childrenAccept(this, data);
		Node n0 = node.jjtGetChild(0);
		ASTArithmeticExpression expression0 = (ASTArithmeticExpression) n0;

		String comparisonOperator = node.getComparisonOperator();
		Node n1 = node.jjtGetChild(1);
		if (n1 instanceof ASTArithmeticExpression) {
			ASTArithmeticExpression expression1 = (ASTArithmeticExpression) n1;
			BinaryCondition.Builder builder = new BinaryCondition.Builder(decodeConditionType(comparisonOperator));
			builder.withLeft(expression0.getResult());
			builder.withRight(expression1.getResult());
			node.setCondition(builder.build());
		}

		return object;
	}

	@Override
	public Object visit(ASTDatetimeExpression node, Object data) {
		Object object = node.childrenAccept(this, data);
		if (node.jjtGetNumChildren() == 0)
			return object;

		Node n0_0 = node.jjtGetChild(0);
		LOG.debug("visit: ASTDatetimeExpression n0_0=" + n0_0);
		if (n0_0 instanceof ASTStateValuedPathExpression) {
			ASTStateValuedPathExpression stateValuedPathExpression = (ASTStateValuedPathExpression) n0_0;
			node.setPath(stateValuedPathExpression.getPath());
		}

		return object;
	}

	@Override
	public Object visit(ASTDatetimeExpressionComparison node, Object data) {
		Object object = node.childrenAccept(this, data);
		Node n0 = node.jjtGetChild(0);
		ASTDatetimeExpression expression0 = (ASTDatetimeExpression) n0;

		String comparisonOperator = node.getComparisonOperator();
		Node n1 = node.jjtGetChild(1);
		if (n1 instanceof ASTDatetimeExpression) {
			ASTDatetimeExpression expression1 = (ASTDatetimeExpression) n1;
			BinaryCondition.Builder builder = new BinaryCondition.Builder(decodeConditionType(comparisonOperator));
			builder.withLeft(decodeExpression(expression0));
			builder.withRight(decodeExpression(expression1));
			node.setCondition(builder.build());
		}

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

	private Object decodeExpression(ASTStringExpression expression) {
		if (expression.getPath() != null)
			return expression.getPath();

		if (expression.getStringLiteral() != null)
			return expression.getStringLiteral();

		if (expression.getInputParameter() != null)
			return expression.getInputParameter();

		return "";
	}

	private Object decodeExpression(ASTBooleanExpression expression) {
		if (expression.getBooleanValue() != null)
			return expression.getBooleanValue();

		if (expression.getPath() != null)
			return expression.getPath();

		if (expression.getInputParameter() != null)
			return expression.getInputParameter();

		return "";
	}

	private Object decodeExpression(ASTDatetimeExpression expression) {
		if (expression.getSqlFunction() != null)
			return expression.getSqlFunction();

		if (expression.getPath() != null)
			return expression.getPath();

		if (expression.getInputParameter() != null)
			return expression.getInputParameter();

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
		node.setCondition(((ConditionNode) n0).getCondition());
		return object;
	}

	@Override
	public Object visit(ASTSimpleCondExpression node, Object data) {
		Object object = node.childrenAccept(this, data);
		LOG.debug("visit: ASTSimpleCondExpression object=" + object);
		node.setCondition(((ConditionNode) node.jjtGetChild(0)).getCondition());
		return object;
	}

	@Override
	public Object visit(ASTConditionalPrimary node, Object data) {
		LOG.debug("visit: ASTConditionalPrimary data=" + data);
		Object object = node.childrenAccept(this, data);
		LOG.debug("visit: ASTConditionalPrimary object=" + object);
		Node n = node.jjtGetChild(0);
		if (n instanceof ASTSimpleCondExpression) {
			node.setCondition(((ASTSimpleCondExpression) n).getCondition());
			return object;
		}

		Condition condition = new NestedCondition(((ASTConditionalExpression) n).getCondition());
		node.setCondition(condition);
		return object;
	}

	@Override
	public Object visit(ASTConditionalFactor node, Object data) {
		LOG.debug("visit: ASTConditionalFactor data=" + data);
		Object object = node.childrenAccept(this, data);
		LOG.debug("visit: ASTConditionalFactor object=" + object);
		ASTConditionalPrimary conditionalPrimary = (ASTConditionalPrimary) node.jjtGetChild(0);
		if (node.isNot())
			node.setCondition(new NotCondition(conditionalPrimary.getCondition()));
		else
			node.setCondition(conditionalPrimary.getCondition());

		return object;
	}

	@Override
	public Object visit(ASTConditionalTerm node, Object data) {
		Object object = node.childrenAccept(this, data);
		LOG.debug("visit: ASTConditionalTerm object=" + object);
		if (node.jjtGetNumChildren() == 1)
			node.setCondition(((ASTConditionalFactor) node.jjtGetChild(0)).getCondition());
		else {
			List<Condition> conditions = new ArrayList<>();
			for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
				Condition condition = ((ASTConditionalFactor) node.jjtGetChild(i)).getCondition();
				conditions.add(condition);
			}

			node.setCondition(new BinaryLogicConditionImpl(ConditionType.AND, conditions));
		}

		return object;
	}

	@Override
	public Object visit(ASTConditionalExpression node, Object data) {
		Object object = node.childrenAccept(this, data);
		LOG.debug("visit: ASTConditionalExpression object=" + object);
		if (node.jjtGetNumChildren() == 1)
			node.setCondition(((ASTConditionalTerm) node.jjtGetChild(0)).getCondition());
		else {
			List<Condition> conditions = new ArrayList<>();
			for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
				Condition condition = ((ASTConditionalTerm) node.jjtGetChild(i)).getCondition();
				conditions.add(condition);
			}

			node.setCondition(new BinaryLogicConditionImpl(ConditionType.OR, conditions));
		}

		return object;
	}

	@Override
	public Object visit(ASTWhereClause node, Object data) {
		Object object = node.childrenAccept(this, data);
		LOG.debug("visit: ASTWhereClause object=" + object);
		JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
		jpqlVisitorParameters.conditions.add(((ASTConditionalExpression) node.jjtGetChild(0)).getCondition());
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
		LOG.debug("visit: ASTArithmeticPrimary node.getResult()=" + node.getResult());
		if (node.jjtGetNumChildren() == 0)
			return object;

		Node n0_0 = node.jjtGetChild(0);
		LOG.debug("visit: ASTArithmeticPrimary n0_0=" + n0_0);
		if (n0_0 instanceof ASTStateValuedPathExpression) {
			ASTStateValuedPathExpression stateValuedPathExpression = (ASTStateValuedPathExpression) n0_0;
			node.setResult(stateValuedPathExpression.getPath());
		}

		return object;
	}

	@Override
	public Object visit(ASTArithmeticFactor node, Object data) {
		Object object = node.childrenAccept(this, data);
		ASTArithmeticPrimary n0_0 = (ASTArithmeticPrimary) node.jjtGetChild(0);
		node.setResult(n0_0.getResult());
		return object;
	}

	@Override
	public Object visit(ASTArithmeticTerm node, Object data) {
		Object object = node.childrenAccept(this, data);
		StringBuilder sb = new StringBuilder();
		ASTArithmeticFactor n0_0 = (ASTArithmeticFactor) node.jjtGetChild(0);
		LOG.debug("visit: ASTArithmeticTerm n0_0.getResult()=" + n0_0.getResult());
		sb.append(n0_0.getSign());
		sb.append(n0_0.getResult());
		for (int i = 1; i < node.jjtGetNumChildren(); ++i) {
			ASTArithmeticFactor n_i = (ASTArithmeticFactor) node.jjtGetChild(i);
			sb.append(node.getSigns().get(i - 1));
			sb.append(n_i.getSign());
			sb.append(n_i.getResult());
		}

		LOG.debug("visit: ASTArithmeticTerm sb.toString()=" + sb.toString());
		node.setResult(sb.toString());
		return object;
	}

	@Override
	public Object visit(ASTArithmeticExpression node, Object data) {
		Object object = node.childrenAccept(this, data);
		StringBuilder sb = new StringBuilder();
		ASTArithmeticTerm n0_0 = (ASTArithmeticTerm) node.jjtGetChild(0);
		LOG.debug("visit: ASTArithmeticExpression n0_0.getResult()=" + n0_0.getResult());
		sb.append(n0_0.getResult());
		for (int i = 1; i < node.jjtGetNumChildren(); ++i) {
			ASTArithmeticTerm n_i = (ASTArithmeticTerm) node.jjtGetChild(i);
			sb.append(node.getSigns().get(i - 1));
			sb.append(n_i.getResult());
		}

		LOG.debug("visit: ASTArithmeticExpression sb.toString()=" + sb.toString());
		node.setResult(sb.toString());
		return object;
	}

	@Override
	public Object visit(ASTQualifiedIdentificationVariable node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTScalarExpression node, Object data) {
		LOG.debug("visit: ASTScalarExpression");
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTEnumExpression node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTAllOrAnyExpression node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTExistsExpression node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTNullComparisonExpression node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTLikeExpression node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTInItem node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTInExpression node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTSingleValuedPathExpression node, Object data) {
		LOG.debug("visit: ASTSingleValuedPathExpression");
		Object object = node.childrenAccept(this, data);
		Node node0 = node.jjtGetChild(0);
		LOG.debug("visit: ASTSingleValuedPathExpression node0=" + node0);
		if (node0 instanceof ASTStateFieldPathExpression) {
			ASTStateFieldPathExpression stateFieldPathExpression = (ASTStateFieldPathExpression) node0;
			node.setIdentificationVariable(stateFieldPathExpression.getIdentificationVariable());
			node.setMapFieldIdentificationVariable(stateFieldPathExpression.getMapFieldIdentificationVariable());
			node.setPath(stateFieldPathExpression.getPath());
			node.setStateField(stateFieldPathExpression.getStateField());
		}

		return object;
	}

	@Override
	public Object visit(ASTSubquery node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTGroupbyItem node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTGroupbyClause node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTFunctionArg node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTGeneralCaseExpression node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTCaseExpression node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTSimpleSelectClause node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(SimpleNode node, Object data) {
		LOG.debug("visit: SimpleNode data=" + data);
		LOG.debug("visit: SimpleNode node=" + node);
		return node.childrenAccept(this, data);
	}

}
