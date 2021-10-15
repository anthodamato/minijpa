/* Generated By:JJTree: Do not edit this line. ASTQualifiedIdentificationVariable.java Version 7.0 */
 /* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

public class ASTQualifiedIdentificationVariable extends SimpleNode {

	private String entryIdentificationVariable;
	private MapFieldIdentificationVariable mapFieldIdentificationVariable;

	public ASTQualifiedIdentificationVariable(int id) {
		super(id);
	}

	public ASTQualifiedIdentificationVariable(JpqlParser p, int id) {
		super(p, id);
	}

	/**
	 * Accept the visitor. *
	 */
	public Object jjtAccept(JpqlParserVisitor visitor, Object data) {

		return visitor.visit(this, data);
	}

	public String getEntryIdentificationVariable() {
		return entryIdentificationVariable;
	}

	public void setEntryIdentificationVariable(String entryIdentificationVariable) {
		this.entryIdentificationVariable = entryIdentificationVariable;
	}

	public MapFieldIdentificationVariable getMapFieldIdentificationVariable() {
		return mapFieldIdentificationVariable;
	}

	public void setMapFieldIdentificationVariable(MapFieldIdentificationVariable mapFieldIdentificationVariable) {
		this.mapFieldIdentificationVariable = mapFieldIdentificationVariable;
	}

}
/* JavaCC - OriginalChecksum=cb36172698106f40d9a0f1e32323a40f (do not edit this line) */
