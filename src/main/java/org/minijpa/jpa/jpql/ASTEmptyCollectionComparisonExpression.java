/* Generated By:JJTree: Do not edit this line. ASTEmptyCollectionComparisonExpression.java Version 7.0 */
 /* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

public class ASTEmptyCollectionComparisonExpression extends SimpleNode {

	private boolean not = false;

	public ASTEmptyCollectionComparisonExpression(int id) {
		super(id);
	}

	public ASTEmptyCollectionComparisonExpression(JpqlParser p, int id) {
		super(p, id);
	}

	/**
	 * Accept the visitor. *
	 */
	public Object jjtAccept(JpqlParserVisitor visitor, Object data) {

		return visitor.visit(this, data);
	}

	public boolean isNot() {
		return not;
	}

	public void setNot(boolean not) {
		this.not = not;
	}

}
/* JavaCC - OriginalChecksum=0977f2b3cfe5dcfb0a5ea630d31657ee (do not edit this line) */
