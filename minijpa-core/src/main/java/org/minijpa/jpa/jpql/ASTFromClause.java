/* Generated By:JJTree: Do not edit this line. ASTFromClause.java Version 7.0 */
 /* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

public class ASTFromClause extends SimpleNode {

	public ASTFromClause(int id) {
		super(id);
	}

	public ASTFromClause(JpqlParser p, int id) {
		super(p, id);
	}

	/**
	 * Accept the visitor. *
	 */
	public Object jjtAccept(JpqlParserVisitor visitor, Object data) {

		return visitor.visit(this, data);
	}
}
/* JavaCC - OriginalChecksum=f6c01c3ce1690b4a18529180b19bf19e (do not edit this line) */
