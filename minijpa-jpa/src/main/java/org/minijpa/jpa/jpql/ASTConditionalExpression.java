/* Generated By:JJTree: Do not edit this line. ASTConditionalExpression.java Version 7.0 */
 /* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

import org.minijpa.sql.model.condition.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ASTConditionalExpression extends SimpleNode {

	private Logger LOG = LoggerFactory.getLogger(ASTConditionalExpression.class);
	private Condition condition;

	public ASTConditionalExpression(int id) {
		super(id);
	}

	public ASTConditionalExpression(JpqlParser p, int id) {
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
/* JavaCC - OriginalChecksum=9ebc7846a556bb87c26e6954508eddbd (do not edit this line) */
