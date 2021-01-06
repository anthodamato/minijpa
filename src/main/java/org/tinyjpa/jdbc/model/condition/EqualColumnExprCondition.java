package org.tinyjpa.jdbc.model.condition;

import org.tinyjpa.jdbc.model.TableColumn;


public class EqualColumnExprCondition implements Condition {
  private TableColumn columnLeft;
  private String expression;


  public EqualColumnExprCondition(TableColumn columnLeft, String expression) {
    super();
    this.columnLeft=columnLeft;
    this.expression=expression;
  }


  public TableColumn getColumnLeft() {
    return columnLeft;
  }


  public String getExpression() {
    return expression;
  }

}
