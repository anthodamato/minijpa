/* Generated By:JJTree: Do not edit this line. ASTSimpleCondExpression.java Version 7.0 */
 /* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

import org.minijpa.jdbc.model.condition.Condition;

public class ASTSimpleCondExpression extends SimpleNode {

	private Condition condition;

	public ASTSimpleCondExpression(int id) {
		super(id);
	}

	public ASTSimpleCondExpression(JpqlParser p, int id) {
		super(p, id);
	}

	/**
	 * Accept the visitor. *
	 */
	@Override
	public Object jjtAccept(JpqlParserVisitor visitor, Object data) {

		return visitor.visit(this, data);
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

}
/* JavaCC - OriginalChecksum=e2f8eb8aaa8c1d4a56c62adce3224ea1 (do not edit this line) */
