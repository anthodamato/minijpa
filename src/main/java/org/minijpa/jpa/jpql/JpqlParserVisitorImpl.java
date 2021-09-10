package org.minijpa.jpa.jpql;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.minijpa.jdbc.MetaEntity;
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
	Object object = node.childrenAccept(this, data);
	return object;
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
    public Object visit(SimpleNode node, Object data) {
	LOG.debug("visit: SimpleNode data=" + data);
	LOG.debug("visit: SimpleNode node=" + node);
	return node.childrenAccept(this, data);
    }

}
