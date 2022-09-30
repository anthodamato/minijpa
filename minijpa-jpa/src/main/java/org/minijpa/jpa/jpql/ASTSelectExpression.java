/* Generated By:JJTree: Do not edit this line. ASTSelectExpression.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

import java.util.List;

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.sql.model.Value;

public class ASTSelectExpression extends SimpleNode {

	private String identificationVariable;
	private String objectIdentificationVariable;
	private List<Value> values;
	private List<FetchParameter> fetchParameters;

	public ASTSelectExpression(int id) {
		super(id);
	}

	public ASTSelectExpression(JpqlParser p, int id) {
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

	public String getObjectIdentificationVariable() {
		return objectIdentificationVariable;
	}

	public void setObjectIdentificationVariable(String objectIdentificationVariable) {
		this.objectIdentificationVariable = objectIdentificationVariable;
	}

	public List<Value> getValues() {
		return values;
	}

	public void setValues(List<Value> values) {
		this.values = values;
	}

	public List<FetchParameter> getFetchParameters() {
		return fetchParameters;
	}

	public void setFetchParameters(List<FetchParameter> fetchParameters) {
		this.fetchParameters = fetchParameters;
	}

}
/*
 * JavaCC - OriginalChecksum=174844307eed48ffdd41c844b539585c (do not edit this
 * line)
 */
