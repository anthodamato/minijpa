package org.minijpa.jpa.jpql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.minijpa.jdbc.AttributeUtil;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.MetaEntityHelper;
import org.minijpa.jdbc.model.FromTable;
import org.minijpa.jdbc.model.FromTableImpl;
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

    private final List<String> rvdEntityName = new ArrayList<>();
    private final List<String> rvdAlias = new ArrayList<>();
    private final List<String> joinSingleValuedPathExpressions = new ArrayList<>();
    private final List<JoinType> joinTypes = new ArrayList<>();
    private final List<String> joinIdentificationVariables = new ArrayList<>();

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
	Map<String, String> aliases = new HashMap<>();
	SqlSelect.SqlSelectBuilder selectBuilder = new SqlSelect.SqlSelectBuilder();

	ASTSelectClause selectClause = (ASTSelectClause) node.jjtGetChild(0);
	if (selectClause.isDistinct())
	    selectBuilder.distinct();

	ASTSelectItem selectItem = (ASTSelectItem) selectClause.jjtGetChild(0);
	String alias = selectItem.getAlias();
	ASTSelectExpression selectExpression = (ASTSelectExpression) selectItem.jjtGetChild(0);
	String identificationVariable = selectExpression.getIdentificationVariable();

	LOG.debug("visit: node.jjtGetNumChildren()=" + node.jjtGetNumChildren());
	ASTFromClause fromClause = (ASTFromClause) node.jjtGetChild(1);
	LOG.debug("visit: fromClause=" + fromClause);
	ASTIdentificationVariableDeclaration identificationVariableDeclaration = (ASTIdentificationVariableDeclaration) fromClause.jjtGetChild(0);
	ASTRangeVariableDeclaration rangeVariableDeclaration = (ASTRangeVariableDeclaration) identificationVariableDeclaration.jjtGetChild(0);
	String rvdEntityName = rangeVariableDeclaration.getEntityName();
	LOG.debug("visit: rvdEntityName=" + rvdEntityName);
	String rvdAlias = rangeVariableDeclaration.getAlias();
	LOG.debug("visit: rvdAlias=" + rvdAlias);
	if (identificationVariableDeclaration.jjtGetNumChildren() > 1) {
	    SimpleNode simpleNode = (SimpleNode) identificationVariableDeclaration.jjtGetChild(1);
	    if (simpleNode instanceof ASTJoin) {
		ASTJoin join = (ASTJoin) simpleNode;
		JoinType jt = join.getJoinType();
		LOG.debug("visit: join.jjtGetNumChildren()=" + join.jjtGetNumChildren());
		ASTJoinAssociationPathExpression joinAssociationPathExpression = (ASTJoinAssociationPathExpression) join.jjtGetChild(0);
		String joinSingleValuedPathExpression = joinAssociationPathExpression.getJoinSingleValuedPathExpression();
		LOG.debug("visit: joinSingleValuedPathExpression=" + joinSingleValuedPathExpression);
		String joinAlias = join.getIdentificationVariable();
		LOG.debug("visit: joinAlias=" + joinAlias);
		String[] rPE = joinSingleValuedPathExpression.split("\\.");
		LOG.debug("visit: rPE[0]=" + rPE[0]);
		if (rPE[0].equals(rvdAlias)) {
		    Optional<MetaEntity> optional = persistenceUnitContext.findMetaEntityByName(rvdEntityName);
		    MetaEntity sourceEntity = optional.get();
		    aliases.put(rvdAlias, sourceEntity.getAlias());
		    String attributePath = joinSingleValuedPathExpression.substring(rPE[0].length() + 1);
		    MetaAttribute metaAttribute
			    = AttributeUtil.findAttributeFromPath(attributePath, sourceEntity);
		    if (metaAttribute == null)
			throw new SemanticException("Attribute path '" + attributePath + "' on '" + sourceEntity.getName() + "' entity not found");

		    if (metaAttribute.getRelationship() == null)
			throw new SemanticException("Attribute '" + metaAttribute.getName() + "' is not a relationship attribute");

		    if (metaAttribute.getRelationship().getJoinTable() != null) {
			aliases.put(joinAlias, metaAttribute.getRelationship().getJoinTable().getTargetEntity().getAlias());
			List<FromJoin> fromJoins = sqlStatementFactory.calculateFromTable(sourceEntity, metaAttribute);
			FromTable fromTable = FromTable.of(sourceEntity);

//			FromTable fromTable = sqlStatementFactory.calculateFromTable(sourceEntity, metaAttribute);
			selectBuilder.withFromTable(fromTable);
			selectBuilder.withJoins(fromJoins);

			if (identificationVariable.equals(rvdAlias)) {
			    List<Value> tableColumns = MetaEntityHelper.toValues(sourceEntity.getId().getAttributes(), FromTable.of(sourceEntity));
			    selectBuilder.withValues(tableColumns);
			}
		    }
		}
	    }
	}

	List<Object> list = (List< Object>) node.childrenAccept(this, aliases);
	list.forEach(v -> LOG.debug("visit: ASTSelectStatement v=" + v));
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
	rvdEntityName.add(node.getEntityName());
	rvdAlias.add(node.getAlias());
	Optional<MetaEntity> optional = persistenceUnitContext.findMetaEntityByName(node.getEntityName());
	if (optional.isEmpty())
	    throw new SemanticException("Entity name '" + node.getEntityName() + "' not found");

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
	joinTypes.add(node.getJoinType());
	joinIdentificationVariables.add(node.getIdentificationVariable());
	return object;
    }

    @Override
    public Object visit(ASTJoinAssociationPathExpression node, Object data) {
	Object object = node.childrenAccept(this, data);
	joinSingleValuedPathExpressions.add(node.getJoinSingleValuedPathExpression());
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
	return node.childrenAccept(this, data);
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
	Map<String, String> aliases = (HashMap<String, String>) data;

	Object object = node.childrenAccept(this, data);
	Node n0_0 = node.jjtGetChild(0);
	if (n0_0 instanceof ASTStateValuedPathExpression) {
	    ASTStateValuedPathExpression stateValuedPathExpression = (ASTStateValuedPathExpression) n0_0;
	    Node n0_0_0 = stateValuedPathExpression.jjtGetChild(0);
	    if (n0_0_0 instanceof ASTStateFieldPathExpression) {
		ASTStateFieldPathExpression stateFieldPathExpression = (ASTStateFieldPathExpression) n0_0_0;
		ASTGeneralSubpath generalSubpath = (ASTGeneralSubpath) stateFieldPathExpression.jjtGetChild(0);
		String stateField = stateFieldPathExpression.getStateField();
		Node n0_0_0_0 = generalSubpath.jjtGetChild(0);
		if (n0_0_0_0 instanceof ASTSimpleSubpath) {
		    ASTSimpleSubpath simpleSubpath = (ASTSimpleSubpath) n0_0_0_0;
		    ASTGeneralIdentificationVariable generalIdentificationVariable = (ASTGeneralIdentificationVariable) simpleSubpath.jjtGetChild(0);
		    if (generalIdentificationVariable.jjtGetNumChildren() == 0) {
			String identificationVariable = generalIdentificationVariable.getIdentificationVariable();
			String r = replaceAlias(aliases, identificationVariable) + "." + stateField;
			node.setPath(r);
		    }
		}
	    } else {
		ASTGeneralIdentificationVariable generalIdentificationVariable = (ASTGeneralIdentificationVariable) n0_0_0;
		String identificationVariable = generalIdentificationVariable.getIdentificationVariable();
		String r = identificationVariable;
		node.setPath(r);
	    }
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

    private Object decodeOperand(String operand) {
	if (operand.equalsIgnoreCase("FALSE"))
	    return Boolean.FALSE;

	if (operand.equalsIgnoreCase("TRUE"))
	    return Boolean.TRUE;

	return operand;
    }

    @Override
    public Object visit(ASTComparisonExpression node, Object data) {
	Object object = node.childrenAccept(this, data);
	Node n0 = node.jjtGetChild(0);
	if (n0 instanceof ASTStringExpression) {
	    ASTStringExpression stringExpression0 = (ASTStringExpression) n0;

	    String comparisonOperator = node.getComparisonOperator();
	    Node n1 = node.jjtGetChild(1);
	    if (n1 instanceof ASTStringExpression) {
		ASTStringExpression stringExpression1 = (ASTStringExpression) n1;
		BinaryCondition.Builder builder = new BinaryCondition.Builder(decodeConditionType(comparisonOperator));
		builder.withLeft(decodeOperand(stringExpression0.getPath()));
		builder.withRight(decodeOperand(stringExpression1.getPath()));
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
	Condition condition = (Condition) node.childrenAccept(this, data);
	LOG.debug("visit: ASTConditionalPrimary condition=" + condition);
	Node n = node.jjtGetChild(0);
	if (n instanceof ASTSimpleCondExpression)
	    return condition;

	return new NestedCondition(condition);
    }

    @Override
    public Object visit(ASTConditionalFactor node, Object data) {
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
    public Object visit(SimpleNode node, Object data) {
	LOG.debug("visit: SimpleNode data=" + data);
	LOG.debug("visit: SimpleNode node=" + node);
	return node.childrenAccept(this, data);
    }

}
