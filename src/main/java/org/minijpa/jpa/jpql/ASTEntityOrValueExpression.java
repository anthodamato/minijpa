/* Generated By:JJTree: Do not edit this line. ASTEntityOrValueExpression.java Version 7.0 */
 /* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

public class ASTEntityOrValueExpression extends SimpleNode {

	private String identificationVariable;
	private String inputParameter;

	public ASTEntityOrValueExpression(int id) {
		super(id);
	}

	public ASTEntityOrValueExpression(JpqlParser p, int id) {
		super(p, id);
	}

	/**
	 * Accept the visitor. *
	 */
	public Object jjtAccept(JpqlParserVisitor visitor, Object data) {

		return visitor.visit(this, data);
	}

	public String getIdentificationVariable() {
		return identificationVariable;
	}

	public void setIdentificationVariable(String identificationVariable) {
		this.identificationVariable = identificationVariable;
	}

	public String getInputParameter() {
		return inputParameter;
	}

	public void setInputParameter(String inputParameter) {
		this.inputParameter = inputParameter;
	}

}
/* JavaCC - OriginalChecksum=aaca0450b439e245ddf1d1de0b1416f1 (do not edit this line) */
