/* Generated By:JJTree: Do not edit this line. ASTArithmeticTerm.java Version 7.0 */
 /* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

import java.util.ArrayList;
import java.util.List;

public class ASTArithmeticTerm extends SimpleNode {

	private List<String> signs = new ArrayList<>();
	private String result;

	public ASTArithmeticTerm(int id) {
		super(id);
	}

	public ASTArithmeticTerm(JpqlParser p, int id) {
		super(p, id);
	}

	/**
	 * Accept the visitor. *
	 */
	public Object jjtAccept(JpqlParserVisitor visitor, Object data) {

		return visitor.visit(this, data);
	}

	public List<String> getSigns() {
		return signs;
	}

	public void addSign(String sign) {
		this.signs.add(sign);
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

}
/* JavaCC - OriginalChecksum=00ed63835433a683b446ee9705568c1f (do not edit this line) */
