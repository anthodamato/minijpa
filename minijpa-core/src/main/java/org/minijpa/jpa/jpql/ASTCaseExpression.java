/* Generated By:JJTree: Do not edit this line. ASTCaseExpression.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

public
class ASTCaseExpression extends SimpleNode {
  public ASTCaseExpression(int id) {
    super(id);
  }

  public ASTCaseExpression(JpqlParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(JpqlParserVisitor visitor, Object data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=f8500bd1c9523fba12e9a53ab8d91310 (do not edit this line) */
