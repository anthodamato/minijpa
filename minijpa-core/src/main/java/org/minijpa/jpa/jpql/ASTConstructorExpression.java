/* Generated By:JJTree: Do not edit this line. ASTConstructorExpression.java Version 7.0 */
 /* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

public class ASTConstructorExpression extends SimpleNode {

	private String constructorName;

	public ASTConstructorExpression(int id) {
		super(id);
	}

	public ASTConstructorExpression(JpqlParser p, int id) {
		super(p, id);
	}

	/**
	 * Accept the visitor. *
	 */
	public Object jjtAccept(JpqlParserVisitor visitor, Object data) {

		return visitor.visit(this, data);
	}

	public String getConstructorName() {
		return constructorName;
	}

	public void setConstructorName(String constructorName) {
		this.constructorName = constructorName;
	}

}
/* JavaCC - OriginalChecksum=1c2e1be6d8cc405bc440d8767614b3c9 (do not edit this line) */
