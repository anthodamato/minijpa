/* Generated By:JJTree: Do not edit this line. ASTJoinAssociationPathExpression.java Version 7.0 */
 /* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

public class ASTJoinAssociationPathExpression extends SimpleNode {

	private String joinSingleValuedPathExpression;

	public ASTJoinAssociationPathExpression(int id) {
		super(id);
	}

	public ASTJoinAssociationPathExpression(JpqlParser p, int id) {
		super(p, id);
	}

	/**
	 * Accept the visitor. *
	 */
	public Object jjtAccept(JpqlParserVisitor visitor, Object data) {

		return visitor.visit(this, data);
	}

	public String getJoinSingleValuedPathExpression() {
		return joinSingleValuedPathExpression;
	}

	public void setJoinSingleValuedPathExpression(String joinSingleValuedPathExpression) {
		this.joinSingleValuedPathExpression = joinSingleValuedPathExpression;
	}

}
/* JavaCC - OriginalChecksum=efc533ffc37be8557aed0daca89cbc7f (do not edit this line) */
