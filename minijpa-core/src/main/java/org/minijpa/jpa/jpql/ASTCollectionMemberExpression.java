/* Generated By:JJTree: Do not edit this line. ASTCollectionMemberExpression.java Version 7.0 */
 /* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

public class ASTCollectionMemberExpression extends SimpleNode {

	private boolean not = false;

	public ASTCollectionMemberExpression(int id) {
		super(id);
	}

	public ASTCollectionMemberExpression(JpqlParser p, int id) {
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
/* JavaCC - OriginalChecksum=019e48be73f57bc684ed8b455df43ca2 (do not edit this line) */
