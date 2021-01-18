package org.minijpa.jdbc.model.aggregate;

import org.minijpa.jdbc.model.TableColumn;
import org.minijpa.jdbc.model.Value;


public class Min implements AggregateFunction, Value {
  private TableColumn tableColumn;


  public Min(TableColumn tableColumn) {
    super();
    this.tableColumn=tableColumn;
  }


  public TableColumn getTableColumn() {
    return tableColumn;
  }

}
