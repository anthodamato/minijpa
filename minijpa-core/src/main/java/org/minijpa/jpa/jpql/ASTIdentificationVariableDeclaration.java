/* Generated By:JJTree: Do not edit this line. ASTIdentificationVariableDeclaration.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.minijpa.jpa.jpql;

public
class ASTIdentificationVariableDeclaration extends SimpleNode {
  public ASTIdentificationVariableDeclaration(int id) {
    super(id);
  }

  public ASTIdentificationVariableDeclaration(JpqlParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(JpqlParserVisitor visitor, Object data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=b90b81777d41acda457e1e0156fddff9 (do not edit this line) */
