/* Generated By:JJTree: Do not edit this line. ASTConditionalTerm.java Version 7.0 */
 /* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

import java.util.ArrayList;
import java.util.List;
import org.minijpa.jdbc.model.condition.BinaryLogicConditionImpl;
import org.minijpa.jdbc.model.condition.Condition;
import org.minijpa.jdbc.model.condition.ConditionType;

public class ASTConditionalTerm extends SimpleNode {

    public ASTConditionalTerm(int id) {
	super(id);
    }

    public ASTConditionalTerm(JpqlParser p, int id) {
	super(p, id);
    }

    /**
     * Accept the visitor. *
     */
    public Object jjtAccept(JpqlParserVisitor visitor, Object data) {

	return visitor.visit(this, data);
    }

    @Override
    public Object childrenAccept(JpqlParserVisitor visitor, Object data) {
	List<Condition> conditions = new ArrayList<>();
	if (children != null) {
	    for (int i = 0; i < children.length; ++i) {
		Condition condition = (Condition) children[i].jjtAccept(visitor, data);
		conditions.add(condition);
	    }
	}

	return new BinaryLogicConditionImpl(ConditionType.AND, conditions);
    }

}
/* JavaCC - OriginalChecksum=08595c5aec453ebce82822aa5a49ce2c (do not edit this line) */
