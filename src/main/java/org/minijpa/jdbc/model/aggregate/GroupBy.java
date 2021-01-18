package org.minijpa.jdbc.model.aggregate;

import java.util.ArrayList;
import java.util.List;

import org.minijpa.jdbc.model.TableColumn;


public class GroupBy {
  private List<TableColumn> columns=new ArrayList<>();


  public GroupBy(TableColumn column) {
    super();
    addColumn(column);
  }


  public void addColumn(TableColumn column) {
    this.columns.add(column);
  }


  public List<TableColumn> getColumns() {
    return columns;
  }

}
