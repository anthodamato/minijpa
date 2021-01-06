package org.tinyjpa.jdbc.model.condition;

import org.tinyjpa.jdbc.model.Column;

public class EqualColumnsCondition implements Condition {
  private Column columnLeft;
  private Column columnRight;


  public EqualColumnsCondition(Column columnLeft, Column columnRight) {
    super();
    this.columnLeft=columnLeft;
    this.columnRight=columnRight;
  }


  public Column getColumnLeft() {
    return columnLeft;
  }


  public Column getColumnRight() {
    return columnRight;
  }


}
