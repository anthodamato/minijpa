/* Generated By:JJTree: Do not edit this line. ASTQLStatement.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

public class ASTQLStatement extends SimpleNode {

	public ASTQLStatement(int id) {
		super(id);
	}

	public ASTQLStatement(JpqlParser p, int id) {
		super(p, id);
	}

	/**
	 * Accept the visitor. *
	 */
	@Override
	public Object jjtAccept(JpqlParserVisitor visitor, Object data) {

		return visitor.visit(this, data);
	}

	@Override
	public Object childrenAccept(JpqlParserVisitor visitor, Object data) {
		return children[0].jjtAccept(visitor, data);
	}

}
/*
 * JavaCC - OriginalChecksum=f7f0f2666adf3775425c2d52c96e2992 (do not edit this
 * line)
 */
