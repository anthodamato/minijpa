/* Generated By:JJTree: Do not edit this line. ASTGeneralIdentificationVariable.java Version 7.0 */
 /* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

public class ASTGeneralIdentificationVariable extends SimpleNode {

	private String identificationVariable;
	private MapFieldIdentificationVariable mapFieldIdentificationVariable;

	public ASTGeneralIdentificationVariable(int id) {
		super(id);
	}

	public ASTGeneralIdentificationVariable(JpqlParser p, int id) {
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

	public MapFieldIdentificationVariable getMapFieldIdentificationVariable() {
		return mapFieldIdentificationVariable;
	}

	public void setMapFieldIdentificationVariable(MapFieldIdentificationVariable mapFieldIdentificationVariable) {
		this.mapFieldIdentificationVariable = mapFieldIdentificationVariable;
	}

}
/* JavaCC - OriginalChecksum=c00cfa84c1a7e3d90d5817585f195382 (do not edit this line) */
