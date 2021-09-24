/* Generated By:JJTree: Do not edit this line. ASTGeneralSubpath.java Version 7.0 */
 /* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

public class ASTGeneralSubpath extends SimpleNode {

    private StringBuilder sb = new StringBuilder();

    public ASTGeneralSubpath(int id) {
	super(id);
    }

    public ASTGeneralSubpath(JpqlParser p, int id) {
	super(p, id);
    }

    /**
     * Accept the visitor. *
     */
    public Object jjtAccept(JpqlParserVisitor visitor, Object data) {

	return visitor.visit(this, data);
    }

    public void addPath(String path) {
	sb.append(".");
	sb.append(path);
    }

    public String getPath() {
	return sb.toString();
    }

}
/* JavaCC - OriginalChecksum=f0dd85c1a2fd54d2f4d26be6560888b3 (do not edit this line) */
