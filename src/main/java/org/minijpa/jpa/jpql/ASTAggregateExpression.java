/* Generated By:JJTree: Do not edit this line. ASTAggregateExpression.java Version 7.0 */
 /* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

import org.minijpa.jdbc.model.Value;
import org.minijpa.jdbc.model.aggregate.AggregateFunctionBasicType;

public class ASTAggregateExpression extends SimpleNode {

	private AggregateFunctionBasicType aggregateFunction;
	private boolean distinct = false;
	private String identificationVariable;
	private Value value;

	public ASTAggregateExpression(int id) {
		super(id);
	}

	public ASTAggregateExpression(JpqlParser p, int id) {
		super(p, id);
	}

	/**
	 * Accept the visitor. *
	 */
	public Object jjtAccept(JpqlParserVisitor visitor, Object data) {

		return visitor.visit(this, data);
	}

	public AggregateFunctionBasicType getAggregateFunction() {
		return aggregateFunction;
	}

	public void setAggregateFunction(AggregateFunctionBasicType aggregateFunction) {
		this.aggregateFunction = aggregateFunction;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	public String getIdentificationVariable() {
		return identificationVariable;
	}

	public void setIdentificationVariable(String identificationVariable) {
		this.identificationVariable = identificationVariable;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

}
/* JavaCC - OriginalChecksum=becf5640b9495b710ae2de8664346acd (do not edit this line) */
