/* Generated By:JJTree: Do not edit this line. ASTSingleValuedObjectPathExpression.java Version 7.0 */
 /* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

public class ASTSingleValuedObjectPathExpression extends SimpleNode {

    private String singleValuedObjectField;

    public ASTSingleValuedObjectPathExpression(int id) {
	super(id);
    }

    public ASTSingleValuedObjectPathExpression(JpqlParser p, int id) {
	super(p, id);
    }

    /**
     * Accept the visitor. *
     */
    public Object jjtAccept(JpqlParserVisitor visitor, Object data) {

	return visitor.visit(this, data);
    }

    public void setSingleValuedObjectField(String singleValuedObjectField) {
	this.singleValuedObjectField = singleValuedObjectField;
    }

    public String getSingleValuedObjectField() {
	return singleValuedObjectField;
    }

}
/* JavaCC - OriginalChecksum=1d8c8a2c2ba6869037ce78a765171212 (do not edit this line) */
