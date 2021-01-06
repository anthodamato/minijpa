package org.tinyjpa.jdbc.model.aggregate;

import org.tinyjpa.jdbc.model.TableColumn;
import org.tinyjpa.jdbc.model.Value;


public class Sum implements AggregateFunction, Value {
  private TableColumn tableColumn;


  public Sum(TableColumn tableColumn) {
    super();
    this.tableColumn=tableColumn;
  }


  public TableColumn getTableColumn() {
    return tableColumn;
  }

}
