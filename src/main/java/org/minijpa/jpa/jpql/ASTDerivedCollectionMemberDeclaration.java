/* Generated By:JJTree: Do not edit this line. ASTDerivedCollectionMemberDeclaration.java Version 7.0 */
 /* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

public class ASTDerivedCollectionMemberDeclaration extends SimpleNode {

	private String identificationVariable;
	private StringBuilder sb = new StringBuilder();
	private String collectionValuedField;

	public ASTDerivedCollectionMemberDeclaration(int id) {
		super(id);
	}

	public ASTDerivedCollectionMemberDeclaration(JpqlParser p, int id) {
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

	public void addPath(String path) {
		if (sb.length() > 0)
			sb.append(".");

		sb.append(path);
	}

	public String getpath() {
		return sb.toString();
	}

	public String getCollectionValuedField() {
		return collectionValuedField;
	}

	public void setCollectionValuedField(String collectionValuedField) {
		this.collectionValuedField = collectionValuedField;
	}

}
/* JavaCC - OriginalChecksum=519ea98973ced6121e6dcfaa816c8422 (do not edit this line) */
