/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.sql.model;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.minijpa.sql.model.aggregate.GroupBy;
import org.minijpa.sql.model.condition.BetweenCondition;
import org.minijpa.sql.model.condition.BinaryCondition;
import org.minijpa.sql.model.condition.BinaryLogicCondition;
import org.minijpa.sql.model.condition.Condition;
import org.minijpa.sql.model.condition.ConditionType;
import org.minijpa.sql.model.condition.InCondition;
import org.minijpa.sql.model.condition.LikeCondition;
import org.minijpa.sql.model.condition.UnaryCondition;
import org.minijpa.sql.model.condition.UnaryLogicCondition;
import org.minijpa.sql.model.expression.SqlBinaryExpression;
import org.minijpa.sql.model.expression.SqlExpression;
import org.minijpa.sql.model.expression.SqlExpressionOperator;
import org.minijpa.sql.model.function.Abs;
import org.minijpa.sql.model.function.Avg;
import org.minijpa.sql.model.function.Coalesce;
import org.minijpa.sql.model.function.Concat;
import org.minijpa.sql.model.function.Count;
import org.minijpa.sql.model.function.CurrentDate;
import org.minijpa.sql.model.function.CurrentTime;
import org.minijpa.sql.model.function.CurrentTimestamp;
import org.minijpa.sql.model.function.Function;
import org.minijpa.sql.model.function.Length;
import org.minijpa.sql.model.function.Locate;
import org.minijpa.sql.model.function.Lower;
import org.minijpa.sql.model.function.Max;
import org.minijpa.sql.model.function.Min;
import org.minijpa.sql.model.function.Mod;
import org.minijpa.sql.model.function.Negation;
import org.minijpa.sql.model.function.Nullif;
import org.minijpa.sql.model.function.Sqrt;
import org.minijpa.sql.model.function.Substring;
import org.minijpa.sql.model.function.Sum;
import org.minijpa.sql.model.function.Trim;
import org.minijpa.sql.model.function.Upper;
import org.minijpa.sql.model.join.FromJoin;
import org.minijpa.sql.model.join.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DefaultSqlStatementGenerator implements SqlStatementGenerator {

  private final Logger LOG = LoggerFactory.getLogger(DefaultSqlStatementGenerator.class);

  protected NameTranslator nameTranslator;

  public DefaultSqlStatementGenerator() {
    super();
  }

  @Override
  public void init() {
    this.nameTranslator = createNameTranslator();
  }

  @Override
  public String buildColumnDefinition(Class<?> type, Optional<JdbcDDLData> ddlData) {
    if (type == Integer.class || (type.isPrimitive() && type.getName().equals("int"))) {
      return "integer";
    }

    if (type == Long.class || (type.isPrimitive() && type.getName().equals("long"))) {
      return "bigint";
    }

    if (type == String.class) {
      if (ddlData.isEmpty() || ddlData.get().getLength().isEmpty()) {
        throw new IllegalArgumentException("Varchar length not specified");
      }

      return "varchar(" + ddlData.get().getLength().get() + ")";
    }

    if (type == Float.class || (type.isPrimitive() && type.getName().equals("float"))) {
      return "float";
    }

    if (type == Double.class || (type.isPrimitive() && type.getName().equals("double"))) {
      return "double";
    }

    if (type == BigDecimal.class) {
      int precision = getDefaultPrecision();
      int scale = getDefaultScale();
      if (ddlData.isPresent() && ddlData.get().getPrecision().isPresent()
          && ddlData.get().getPrecision().get() != 0) {
        precision = ddlData.get().getPrecision().get();
      }

      if (ddlData.isPresent() && ddlData.get().getScale().isPresent()
          && ddlData.get().getScale().get() != 0) {
        scale = ddlData.get().getScale().get();
      }

      return "decimal(" + precision + "," + scale + ")";
    }

    if (type == java.sql.Date.class) {
      return "date";
    }

    if (type == Timestamp.class) {
      return "timestamp";
    }

    if (type == Time.class) {
      return "time";
    }

    if (type == Boolean.class || (type.isPrimitive() && type.getName().equals("boolean"))) {
      return "boolean";
    }

    return "";
  }

  @Override
  public String buildIdentityColumnDefinition(Class<?> type, Optional<JdbcDDLData> ddlData) {
    return buildColumnDefinition(type, ddlData) + " generated by default as identity";
  }

  @Override
  public String export(SqlInsert sqlInsert) {
    StringBuilder sb = new StringBuilder();
    sb.append("insert into ");
    sb.append(sqlInsert.getFromTable().getName());
    sb.append(" (");
    String cols = sqlInsert.getColumns().stream().map(a -> a.getName())
        .collect(Collectors.joining(","));
    sb.append(cols);
    sb.append(") values (");

    for (int i = 0; i < sqlInsert.getColumns().size(); ++i) {
      if (i > 0) {
        sb.append(",");
      }

      sb.append("?");
    }

    sb.append(")");
    return sb.toString();
  }

  @Override
  public NameTranslator getNameTranslator() {
    return nameTranslator;
  }

  @Override
  public String export(SqlUpdate sqlUpdate) {
    return export(sqlUpdate, nameTranslator);
  }

  protected String export(SqlUpdate sqlUpdate, NameTranslator nameTranslator) {
    StringBuilder sb = new StringBuilder();
    sb.append("update ");
    sb.append(nameTranslator.toTableName(sqlUpdate.getFromTable().getAlias(),
        sqlUpdate.getFromTable().getName()));
    sb.append(" set ");

    String sv = sqlUpdate.getTableColumns().stream().map(c -> {
      return exportTableColumn(c, nameTranslator) + " = ?";
    }).collect(Collectors.joining(", "));
    sb.append(sv);

    if (sqlUpdate.getCondition().isPresent()) {
      sb.append(" where ");
      sb.append(exportCondition(sqlUpdate.getCondition().get(), nameTranslator));
    }

    return sb.toString();
  }

  @Override
  public String export(SqlDelete sqlDelete) {
    return export(sqlDelete, nameTranslator);
  }

  protected String export(SqlDelete sqlDelete, NameTranslator nameTranslator) {
    StringBuilder sb = new StringBuilder();
    sb.append("delete from ");
    sb.append(nameTranslator.toTableName(sqlDelete.getFromTable().getAlias(),
        sqlDelete.getFromTable().getName()));

    if (sqlDelete.getCondition().isPresent()) {
      sb.append(" where ");
      sb.append(exportCondition(sqlDelete.getCondition().get(), nameTranslator));
    }

    return sb.toString();
  }

  private String getSqlOperator(SqlExpressionOperator operator) {
    switch (operator) {
      case SUM:
        return "+";
      case PROD:
        return "*";
      case MINUS:
        return "-";
      case DIFF:
        return "-";
      case QUOT:
        return "/";

      default:
        break;
    }

    throw new IllegalArgumentException("Sql operator '" + operator + "' not supported");
  }

  protected String exportFunction(Abs abs) {
    return "ABS(" + exportExpression(abs.getArgument(), nameTranslator) + ")";
  }

  protected String exportFunction(Avg avg) {
    return "AVG(" + exportExpression(avg.getArgument(), nameTranslator) + ")";
  }

  protected String exportFunction(Concat concat) {
    return Arrays.stream(concat.getParams()).map(p -> exportExpression(p, nameTranslator))
        .collect(Collectors.joining("||"));
  }

  protected String exportFunction(Coalesce coalesce) {
    return "COALESCE(" + Arrays.stream(coalesce.getParams())
        .map(p -> exportExpression(p, nameTranslator))
        .collect(Collectors.joining(", ")) + ")";
  }

  protected String exportFunction(Nullif nullif) {
    return "NULLIF(" + exportExpression(nullif.getP1(), nameTranslator) + ","
        + exportExpression(nullif.getP2(), nameTranslator) + ")";
  }

  protected String exportFunction(Count count) {
    StringBuilder sb = new StringBuilder("COUNT(");
    if (count.isDistinct()) {
      sb.append("distinct ");
    }

    sb.append(exportExpression(count.getArgument(), nameTranslator));
    sb.append(")");
    return sb.toString();
  }

  protected String exportFunction(Length length) {
    return "LENGTH(" + exportExpression(length.getArgument(), nameTranslator) + ")";
  }

  protected String exportFunction(Locate locate) {
    StringBuilder sb = new StringBuilder("LOCATE(");

    sb.append(exportExpression(locate.getSearchString(), nameTranslator));
    sb.append(", ");
    sb.append(exportExpression(locate.getInputString(), nameTranslator));
    if (locate.getPosition().isPresent()) {
      sb.append(", ");
      sb.append(exportExpression(locate.getPosition().get(), nameTranslator));
    }

    sb.append(")");
    return sb.toString();
  }

  protected String exportFunction(Lower lower) {
    return "LOWER(" + exportExpression(lower.getArgument(), nameTranslator) + ")";
  }

  protected String exportFunction(Upper upper) {
    return "UPPER(" + exportExpression(upper.getArgument(), nameTranslator) + ")";
  }

  protected String exportFunction(Max max) {
    return "MAX(" + exportExpression(max.getArgument(), nameTranslator) + ")";
  }

  protected String exportFunction(Min min) {
    return "MIN(" + exportExpression(min.getArgument(), nameTranslator) + ")";
  }

  protected String exportFunction(Mod mod) {
    StringBuilder sb = new StringBuilder("MOD(");

    sb.append(exportExpression(mod.getDividend(), nameTranslator));
    sb.append(", ");
    sb.append(exportExpression(mod.getDivider(), nameTranslator));
    sb.append(")");
    return sb.toString();
  }

  protected String exportFunction(Sqrt sqrt) {
    return "SQRT(" + exportExpression(sqrt.getArgument(), nameTranslator) + ")";
  }

  protected String exportFunction(Negation negation) {
    return "-" + exportExpression(negation.getArgument(), nameTranslator);
  }

  protected String exportFunction(CurrentDate currentDate) {
    return "CURRENT_DATE";
  }

  protected String exportFunction(CurrentTime currentTime) {
    return "CURRENT_TIME";
  }

  protected String exportFunction(CurrentTimestamp currentTimestamp) {
    return "CURRENT_TIMESTAMP";
  }

  protected String exportFunction(Substring substring) {
    StringBuilder sb = new StringBuilder("SUBSTR(");
    sb.append(exportExpression(substring.getArgument(), nameTranslator));
    sb.append(", ");
    sb.append(exportExpression(substring.getStartIndex(), nameTranslator));
    if (substring.getLength().isPresent()) {
      sb.append(", ");
      sb.append(exportExpression(substring.getLength().get(), nameTranslator));
    }

    sb.append(")");
    return sb.toString();
  }

  protected String exportFunction(Sum sum) {
    return "SUM(" + exportExpression(sum.getArgument(), nameTranslator) + ")";
  }

  protected String exportFunction(Trim trim) {
    StringBuilder sb = new StringBuilder("TRIM(");
    if (trim.getTrimType().isPresent()) {
      switch (trim.getTrimType().get()) {
        case BOTH:
          sb.append("BOTH");
          break;
        case LEADING:
          sb.append("LEADING");
          break;
        case TRAILING:
          sb.append("TRAILING");
          break;
        default:
          break;
      }
    }

    if (trim.getTrimCharacter() != null) {
      sb.append(" ");
      sb.append(trim.getTrimCharacter());
    }

    if (trim.getTrimType().isPresent() || trim.getTrimCharacter() != null) {
      sb.append(" FROM ");
    }

    sb.append(exportExpression(trim.getArgument(), nameTranslator));
    sb.append(")");
    return sb.toString();
  }

  protected String exportFunction(Function function, NameTranslator nameTranslator) {
    if (function instanceof Abs) {
      return exportFunction((Abs) function);
    } else if (function instanceof Avg) {
      return exportFunction((Avg) function);
    } else if (function instanceof Concat) {
      return exportFunction((Concat) function);
    } else if (function instanceof Coalesce) {
      return exportFunction((Coalesce) function);
    } else if (function instanceof Count) {
      return exportFunction((Count) function);
    } else if (function instanceof Length) {
      return exportFunction((Length) function);
    } else if (function instanceof Locate) {
      return exportFunction((Locate) function);
    } else if (function instanceof Lower) {
      return exportFunction((Lower) function);
    } else if (function instanceof Max) {
      return exportFunction((Max) function);
    } else if (function instanceof Min) {
      return exportFunction((Min) function);
    } else if (function instanceof Mod) {
      return exportFunction((Mod) function);
    } else if (function instanceof Nullif) {
      return exportFunction((Nullif) function);
    } else if (function instanceof Sqrt) {
      return exportFunction((Sqrt) function);
    } else if (function instanceof Negation) {
      return exportFunction((Negation) function);
    } else if (function instanceof Substring) {
      return exportFunction((Substring) function);
    } else if (function instanceof Sum) {
      return exportFunction((Sum) function);
    } else if (function instanceof Trim) {
      return exportFunction((Trim) function);
    } else if (function instanceof Upper) {
      return exportFunction((Upper) function);
    } else if (function instanceof CurrentDate) {
      return exportFunction((CurrentDate) function);
    } else if (function instanceof CurrentTime) {
      return exportFunction((CurrentTime) function);
    } else if (function instanceof CurrentTimestamp) {
      return exportFunction((CurrentTimestamp) function);
    }

    return "";
  }

  protected String exportExpression(Object expression, NameTranslator nameTranslator) {
    LOG.debug("exportExpression: expression={}", expression);
    LOG.debug("exportExpression: nameTranslator={}", nameTranslator);
    if (expression instanceof TableColumn) {
      return exportTableColumn((TableColumn) expression, nameTranslator);
    }

    if (expression instanceof String) {
      return (String) expression;
    }

    if (expression instanceof Boolean) {
      return booleanValue((Boolean) expression);
    }

    if (expression instanceof SqlSelect) {
      StringBuilder sb = new StringBuilder();
      sb.append("(");
      sb.append(export((SqlSelect) expression));
      sb.append(")");
      return sb.toString();
    }

    if (expression instanceof List) {
      List<Object> list = (List) expression;
      StringBuilder sb = new StringBuilder();
      list.forEach(obj -> {
        sb.append(exportExpression(obj, nameTranslator));
      });

      return sb.toString();
    }

    if (expression instanceof Function) {
      return exportFunction((Function) expression, nameTranslator);
    }

    if (expression instanceof SqlBinaryExpression) {
      return exportSqlBinaryExpression((SqlBinaryExpression) expression, nameTranslator);
    }

    if (expression instanceof Integer) {
      return ((Integer) expression).toString();
    }

    return "";
  }

  private String exportSqlBinaryExpression(SqlBinaryExpression sqlBinaryExpression,
      NameTranslator nameTranslator) {
    StringBuilder sb = new StringBuilder();
    Object leftExpression = sqlBinaryExpression.getLeftExpression();
    sb.append(exportExpression(leftExpression, nameTranslator));

    sb.append(getSqlOperator(sqlBinaryExpression.getOperator()));

    sb.append(exportExpression(sqlBinaryExpression.getRightExpression(), nameTranslator));

    return sb.toString();
  }

  private String exportSqlExpression(SqlExpression sqlExpression) {
    return exportExpression(sqlExpression.getExpression(), nameTranslator);
  }

  protected String exportCondition(LikeCondition likeCondition, NameTranslator nameTranslator) {
    StringBuilder sb = new StringBuilder();
    if (likeCondition.isNot()) {
      sb.append("not ");
    }

    Object left = likeCondition.getLeft();
    LOG.debug("exportCondition: left={}", left);
    sb.append(exportExpression(left, nameTranslator));

    sb.append(" ");
    sb.append(getOperator(likeCondition.getConditionType()));
    sb.append(" ");
    Object right = likeCondition.getRight();
    LOG.debug("exportCondition: right={}", right);
    LOG.debug("exportCondition: likeCondition.getEscapeChar()={}", likeCondition.getEscapeChar());
    sb.append(exportExpression(right, nameTranslator));
//        if (likeCondition.getEscapeChar() != null && !likeCondition.getEscapeChar().equals("'\\'")) {
    if (likeCondition.getEscapeChar() != null) {
      sb.append(" escape ");
      sb.append(likeCondition.getEscapeChar());
    }

    return sb.toString();
  }

  protected String exportCondition(Condition condition, NameTranslator nameTranslator) {
    LOG.debug("exportCondition: condition={}", condition);
    if (condition instanceof BinaryLogicCondition) {
      BinaryLogicCondition binaryLogicCondition = (BinaryLogicCondition) condition;
      StringBuilder sb = new StringBuilder();
      if (binaryLogicCondition.nested()) {
        sb.append("(");
      }

//	    LOG.debug("exportCondition: binaryLogicCondition.getConditions().size="
//		    + binaryLogicCondition.getConditions().size());
      String operator = " " + getOperator(condition.getConditionType()) + " ";
      String cc = binaryLogicCondition.getConditions().stream().map(c -> {
        return exportCondition(c, nameTranslator);
      }).collect(Collectors.joining(operator));
      sb.append(cc);

      if (binaryLogicCondition.nested()) {
        sb.append(")");
      }

      return sb.toString();
    }

    if (condition instanceof UnaryLogicCondition) {
      UnaryLogicCondition unaryLogicCondition = (UnaryLogicCondition) condition;
      StringBuilder sb = new StringBuilder();
      if (unaryLogicCondition.getConditionType() == ConditionType.NOT) {
        sb.append(getOperator(condition.getConditionType()));
        sb.append(" (");
        sb.append(exportCondition(unaryLogicCondition.getCondition(), nameTranslator));
        sb.append(")");
      }
//			else if (unaryLogicCondition.getConditionType() == ConditionType.IS_TRUE
//					|| unaryLogicCondition.getConditionType() == ConditionType.IS_FALSE) {
//				sb.append(exportCondition(unaryLogicCondition.getCondition()));
//				sb.append(" ");
//				sb.append(getOperator(condition.getConditionType()));
//			}

      return sb.toString();
    }

    if (condition instanceof UnaryCondition) {
      UnaryCondition unaryCondition = (UnaryCondition) condition;
      StringBuilder sb = new StringBuilder();
      if (unaryCondition.getConditionType() == ConditionType.EQUALS_TRUE
          || unaryCondition.getConditionType() == ConditionType.EQUALS_FALSE
          || unaryCondition.getConditionType() == ConditionType.IS_NULL
          || unaryCondition.getConditionType() == ConditionType.IS_NOT_NULL) {
        sb.append(exportExpression(unaryCondition.getOperand(), nameTranslator));

        sb.append(" ");
        sb.append(getOperator(condition.getConditionType()));
      }

      return sb.toString();
    }

    if (condition instanceof LikeCondition) {
      LikeCondition likeCondition = (LikeCondition) condition;
      return exportCondition(likeCondition, nameTranslator);
    }

    if (condition instanceof BinaryCondition) {
      BinaryCondition binaryCondition = (BinaryCondition) condition;

      StringBuilder sb = new StringBuilder();
      if (binaryCondition.isNot()) {
        sb.append("not ");
      }

      Object left = binaryCondition.getLeft();
      LOG.debug("exportCondition: left={}", left);
      sb.append(exportExpression(left, nameTranslator));

      sb.append(" ");
      sb.append(getOperator(condition.getConditionType()));
      sb.append(" ");
      Object right = binaryCondition.getRight();
      LOG.debug("exportCondition: right={}", right);
      sb.append(exportExpression(right, nameTranslator));

      return sb.toString();
    }

    if (condition instanceof BetweenCondition) {
      BetweenCondition betweenCondition = (BetweenCondition) condition;
      StringBuilder sb = new StringBuilder();
      sb.append(exportExpression(betweenCondition.getOperand(), nameTranslator));
      sb.append(" ");
      if (betweenCondition.isNot()) {
        sb.append("not ");
      }

      sb.append(getOperator(condition.getConditionType()));
      sb.append(" ");

      sb.append(exportExpression(betweenCondition.getLeftExpression(), nameTranslator));
      sb.append(" and ");
      sb.append(exportExpression(betweenCondition.getRightExpression(), nameTranslator));

      return sb.toString();
    }

    if (condition instanceof InCondition) {
      InCondition inCondition = (InCondition) condition;
      StringBuilder sb = new StringBuilder();

      sb.append(exportTableColumn(inCondition.getLeftColumn(), nameTranslator));
      sb.append(" ");
      if (inCondition.isNot()) {
        sb.append("not ");
      }

      sb.append(getOperator(condition.getConditionType()));
      sb.append(" (");

      String s = inCondition.getRightExpressions().stream()
          .map(v -> exportExpression(v, nameTranslator))
          .collect(Collectors.joining(", "));
      sb.append(s);
      sb.append(")");
      return sb.toString();
    }

    throw new IllegalArgumentException("Condition '" + condition + "' not supported");
  }

  protected String exportJoin(FromJoin fromJoin) {
    StringBuilder sb = new StringBuilder();
    sb.append(" ");
    sb.append(exportJoinKeyword(fromJoin.getType()));
    sb.append(" ");

    FromTable toTable = fromJoin.getToTable();
    sb.append(nameTranslator.toTableName(toTable.getAlias(), toTable.getName()));
    sb.append(" ON ");
    List<Column> fromColumns = fromJoin.getFromColumns();
    List<Column> toColumns = fromJoin.getToColumns();
    for (int i = 0; i < fromColumns.size(); ++i) {
      if (i > 0) {
        sb.append(" AND ");
      }

      sb.append(fromJoin.getFromAlias());
      sb.append(".");

      sb.append(fromColumns.get(i).getName());
      sb.append(" = ");
      if (toTable.getAlias().isPresent()) {
        sb.append(toTable.getAlias().get());
        sb.append(".");
      }

      sb.append(toColumns.get(i).getName());
    }

    return sb.toString();
  }

  protected String exportJoinKeyword(JoinType joinType) {
    switch (joinType) {
      case Inner:
        return "INNER JOIN";
      case Left:
        return "LEFT OUTER JOIN";
      case Right:
        return "RIGHT OUTER JOIN";
    }

    return null;
  }

  protected String exportJoins(List<FromJoin> fromJoins) {
    StringBuilder sb = new StringBuilder();
    for (FromJoin fromJoin : fromJoins) {
      sb.append(exportJoin(fromJoin));
    }

    return sb.toString();
  }

  protected String exportFromTable(List<FromTable> fromTables) {
    return fromTables.stream().map(t -> {
      return nameTranslator.toTableName(t.getAlias(), t.getName());
    }).collect(Collectors.joining(", "));
  }

  protected String exportFrom(List<From> fromTables) {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for (From from : fromTables) {
      if (from instanceof FromTable) {
        FromTable fromTable = (FromTable) from;
        sb.append(nameTranslator.toTableName(fromTable.getAlias(), fromTable.getName()));
        if (i > 0) {
          sb.append(", ");
        }
      } else if (from instanceof FromJoin) {
        FromJoin fromJoin = (FromJoin) from;
        sb.append(exportJoin(fromJoin));
      }

      ++i;
    }

    return sb.toString();
  }

  private String exportGroupBy(GroupBy groupBy) {
    return "group by " + groupBy.getColumns().stream()
        .map(c -> exportTableColumn(c, nameTranslator))
        .collect(Collectors.joining(", "));
  }

  private String exportOrderBy(OrderBy orderBy) {
    String ad = "";
    if (orderBy.getOrderByType() != null) {
      ad = orderBy.getOrderByType() == OrderByType.ASC ? " ASC" : " DESC";
    }

    return exportTableColumn(orderBy.getTableColumn(), nameTranslator) + ad;
  }

  @Override
  public String sequenceNextValueStatement(Optional<String> optionalSchema, String sequenceName) {
    if (optionalSchema.isEmpty()) {
      return "VALUES (NEXT VALUE FOR " + sequenceName + ")";
    }

    return "VALUES (NEXT VALUE FOR " + optionalSchema.get() + "." + sequenceName + ")";
  }

  protected String exportTableColumn(TableColumn tableColumn, NameTranslator nameTranslator) {
    Optional<FromTable> optionalFromTable = tableColumn.getTable();
    LOG.debug("exportTableColumn: optionalFromTable={}", optionalFromTable);
    Column column = tableColumn.getColumn();
    LOG.debug("exportTableColumn: column={}", column);
    LOG.debug("exportTableColumn: nameTranslator={}", nameTranslator);
    LOG.debug("exportTableColumn: column.getName()={}", column.getName());

    if (tableColumn.getSubQuery().isPresent() && tableColumn.getSubQuery().get().getAlias()
        .isPresent()) {
      return nameTranslator.toColumnName(tableColumn.getSubQuery().get().getAlias(),
          column.getName(),
          column.getAlias());
    }

    String c = nameTranslator.toColumnName(optionalFromTable.get().getAlias(), column.getName(),
        column.getAlias());
    return c;
  }

  protected String exportItem(Object item) {
    if (item instanceof TableColumn) {
      return exportTableColumn((TableColumn) item, nameTranslator);
    }
    if (item instanceof Function) {
      return exportFunction((Function) item, nameTranslator);
    }
    if (item instanceof SqlExpression) {
      return exportSqlExpression((SqlExpression) item);
    }
    if (item instanceof SqlBinaryExpression) {
      return exportSqlBinaryExpression((SqlBinaryExpression) item, nameTranslator);
    }
    if (item instanceof SelectItem) {
      return exportSelectItem((SelectItem) item);
    }

    throw new IllegalArgumentException("Value type '" + item + "' not supported");
  }

  protected String exportSelectItem(SelectItem selectItem) {
    if (selectItem.getAlias().isPresent()) {
      return nameTranslator.toColumnName(Optional.empty(), exportItem(selectItem.getItem()),
          selectItem.getAlias());
    }

    return exportItem(selectItem.getItem());
  }

  @Override
  public String export(SqlSelect sqlSelect) {
    LOG.debug("export: SqlSelect sqlSelect={}", sqlSelect);
    StringBuilder sb = new StringBuilder("select ");
    if (sqlSelect.isDistinct()) {
      sb.append("distinct ");
    }

//		LOG.debug("export: SqlSelect sqlSelect.getValues()={}", sqlSelect.getValues());
//        LOG.debug("export: 2 sqlSelect.getValues()={}", sqlSelect.getValues());

    String cc = sqlSelect.getValues().stream().map(c -> {
      LOG.debug("export: SqlSelect c={}", c);
      return exportItem(c);
    }).collect(Collectors.joining(", "));

    LOG.debug("export: SqlSelect cc={}", cc);
    sb.append(cc);
    sb.append(" from ");
    sb.append(exportFrom(sqlSelect.getFrom()));

    if (sqlSelect.getConditions().isPresent()) {
      sb.append(" where ");
      String ccs = sqlSelect.getConditions().get().stream()
          .map(c -> exportCondition(c, nameTranslator))
          .collect(Collectors.joining(" "));
      sb.append(ccs);
      LOG.debug("export: ccs={}", ccs);
    }

    if (sqlSelect.getGroupBy().isPresent()) {
      sb.append(" ");
      sb.append(exportGroupBy(sqlSelect.getGroupBy().get()));
    }

    if (sqlSelect.getOrderByList().isPresent()) {
      sb.append(" order by ");
      String s = sqlSelect.getOrderByList().get().stream().map(o -> {
        return exportOrderBy(o);
      }).collect(Collectors.joining(", "));
      sb.append(s);
    }

    LOG.debug("export: SqlSelect sqlSelect.getOptionalForUpdate()={}",
        sqlSelect.getOptionalForUpdate());
    if (sqlSelect.getOptionalForUpdate().isPresent()) {
      String forUpdate = forUpdateClause(sqlSelect.getOptionalForUpdate().get());
      if (forUpdate != null && !forUpdate.isEmpty()) {
        sb.append(" ");
        sb.append(forUpdate);
      }
    }

    return sb.toString();
  }

  protected String getOperator(ConditionType conditionType) {
    switch (conditionType) {
      case EQUAL:
        return equalOperator();
      case NOT_EQUAL:
        return notEqualOperator();
      case AND:
        return andOperator();
      case IS_NOT_NULL:
        return notNullOperator();
      case IS_NULL:
        return isNullOperator();
      case EQUALS_TRUE:
        return equalsTrueOperator();
      case EQUALS_FALSE:
        return equalsFalseOperator();
      case NOT:
        return notOperator();
      case OR:
        return orOperator();
      case EMPTY_CONJUNCTION:
        return emptyConjunctionOperator();
      case EMPTY_DISJUNCTION:
        return emptyDisjunctionOperator();
      case GREATER_THAN:
        return greaterThanOperator();
      case GREATER_THAN_OR_EQUAL_TO:
        return greaterThanOrEqualToOperator();
      case LESS_THAN:
        return lessThanOperator();
      case LESS_THAN_OR_EQUAL_TO:
        return lessThanOrEqualToOperator();
      case BETWEEN:
        return betweenOperator();
      case LIKE:
        return likeOperator();
      case IN:
        return inOperator();
      default:
        break;
    }

    throw new IllegalArgumentException("Unknown operator for condition type: " + conditionType);
  }

  private String buildColumnDefinition(ColumnDeclaration columnDeclaration) {
    Optional<JdbcDDLData> ddlData = columnDeclaration.getOptionalJdbcDDLData();
    if (ddlData.isPresent()) {
      if (ddlData.get().getColumnDefinition().isPresent()) {
        return ddlData.get().getColumnDefinition().get();
      }
    }

    String s = buildColumnDefinition(columnDeclaration.getDatabaseType(),
        columnDeclaration.getOptionalJdbcDDLData());
    if (ddlData.isPresent() && ddlData.get().getNullable().isPresent()
        && ddlData.get().getNullable().get() == false) {
      return s + " not null";
    }

    return s;
  }

  protected String buildAttributeDeclaration(ColumnDeclaration columnDeclaration) {
    return nameTranslator.adjustName(columnDeclaration.getName()) + " " + buildColumnDefinition(
        columnDeclaration);
  }

  private String buildPkDeclaration(SqlPk jdbcPk) {
    if (jdbcPk.isIdentityColumn()) {
      return nameTranslator.adjustName(jdbcPk.getColumn().getName()) + " "
          + buildIdentityColumnDefinition(
          jdbcPk.getColumn().getDatabaseType(), jdbcPk.getColumn().getOptionalJdbcDDLData());
    }

    if (jdbcPk.isComposite()) {
      return jdbcPk.getColumns().stream().map(a -> buildAttributeDeclaration(a))
          .collect(Collectors.joining(", "));
    }

    return buildAttributeDeclaration(jdbcPk.getColumn());
  }

  protected String buildJoinTableColumnDeclaration(ColumnDeclaration columnDeclaration) {
    return nameTranslator.adjustName(columnDeclaration.getName()) + " "
        + buildColumnDefinition(columnDeclaration.getDatabaseType(), Optional.empty())
        + " not null";
  }

  protected String buildJoinTableColumnDeclaration(JdbcJoinColumnMapping jdbcJoinColumnMapping) {
    return jdbcJoinColumnMapping.getJoinColumns().stream()
        .map(c -> nameTranslator.adjustName(c.getName()) + " "
            + buildColumnDefinition(c.getDatabaseType(), Optional.empty()) + (
            jdbcJoinColumnMapping.unique() ? "" : " not null"))
        .collect(Collectors.joining(", "));
  }

  protected String buildDeclaration(ColumnDeclaration columnDeclaration) {
    return nameTranslator.adjustName(columnDeclaration.getName()) + " "
        + buildColumnDefinition(columnDeclaration.getDatabaseType(), Optional.empty());
  }

  @Override
  public String export(SqlCreateTable sqlCreateTable) {
    LOG.debug("export: SqlCreateTable sqlCreateTable={}", sqlCreateTable);
    StringBuilder sb = new StringBuilder();
    sb.append("create table ");
    sb.append(nameTranslator.adjustName(sqlCreateTable.getTableName()));
    sb.append(" (");
    String cols = buildPkDeclaration(sqlCreateTable.getJdbcPk());
    LOG.debug("export: SqlCreateTable pk cols={}", cols);
    sb.append(cols);

    if (!sqlCreateTable.getColumnDeclarations().isEmpty()) {
      sb.append(", ");
      cols = sqlCreateTable.getColumnDeclarations().stream().map(a -> buildAttributeDeclaration(a))
          .collect(Collectors.joining(", "));
      sb.append(cols);
    }

    LOG.debug("export: SqlCreateTable pk Columns cols={}", cols);
    for (ForeignKeyDeclaration foreignKeyDeclaration : sqlCreateTable.getForeignKeyDeclarations()) {
      sb.append(", ");
      LOG.debug(
          "export: SqlCreateTable pk JoinColumns foreignKeyDeclaration.getJdbcJoinColumnMapping()={}",
          foreignKeyDeclaration.getJdbcJoinColumnMapping());
      LOG.debug(
          "export: SqlCreateTable pk JoinColumns foreignKeyDeclaration.getJdbcJoinColumnMapping().getJoinColumns()={}",
          foreignKeyDeclaration.getJdbcJoinColumnMapping().getJoinColumns());
      cols = foreignKeyDeclaration.getJdbcJoinColumnMapping().getJoinColumns().stream()
          .map(a -> buildDeclaration(a)).collect(Collectors.joining(", "));
      sb.append(cols);
    }

    LOG.debug("export: SqlCreateTable pk JoinColumns cols={}", cols);
    sb.append(", primary key ");
    if (sqlCreateTable.getJdbcPk().isComposite()) {
      sb.append("(");
      cols = sqlCreateTable.getJdbcPk().getColumns().stream()
          .map(a -> nameTranslator.adjustName(a.getName()))
          .collect(Collectors.joining(", "));
      sb.append(cols);
      sb.append(")");
    } else {
      sb.append("(");
      sb.append(nameTranslator.adjustName(sqlCreateTable.getJdbcPk().getColumn().getName()));
      sb.append(")");
    }

    // foreign keys
    for (ForeignKeyDeclaration foreignKeyDeclaration : sqlCreateTable.getForeignKeyDeclarations()) {
      sb.append(", foreign key (");
      cols = foreignKeyDeclaration.getJdbcJoinColumnMapping().getJoinColumns().stream()
          .map(a -> nameTranslator.adjustName(a.getName())).collect(Collectors.joining(", "));
      sb.append(cols);
      sb.append(") references ");
      sb.append(foreignKeyDeclaration.getReferenceTable());
    }

    sb.append(")");
    return sb.toString();
  }

  @Override
  public String export(SqlCreateJoinTable sqlCreateJoinTable) {
    StringBuilder sb = new StringBuilder();
    sb.append("create table ");
    sb.append(nameTranslator.adjustName(sqlCreateJoinTable.getTableName()));
    sb.append(" (");
    String cols = sqlCreateJoinTable.getForeignKeyDeclarations()
        .stream()
        .map(ForeignKeyDeclaration::getJdbcJoinColumnMapping).map(
            this::buildJoinTableColumnDeclaration)
        .collect(Collectors.joining(", "));

    sb.append(cols);

    // foreign keys
    for (ForeignKeyDeclaration foreignKeyDeclaration : sqlCreateJoinTable.getForeignKeyDeclarations()) {
      sb.append(", foreign key (");
      cols = foreignKeyDeclaration.getJdbcJoinColumnMapping().getJoinColumns().stream()
          .map(a -> nameTranslator.adjustName(a.getName())).collect(Collectors.joining(", "));
      sb.append(cols);
      sb.append(") references ");
      sb.append(foreignKeyDeclaration.getReferenceTable());
    }

    // possible table constraints
    for (ForeignKeyDeclaration foreignKeyDeclaration : sqlCreateJoinTable.getForeignKeyDeclarations()) {
      if (foreignKeyDeclaration.getJdbcJoinColumnMapping().unique()) {
        sb.append(", unique (");
        sb.append(foreignKeyDeclaration.getJdbcJoinColumnMapping()
            .getJoinColumns().stream().map(ColumnDeclaration::getName).collect(Collectors.joining(", ")));
        sb.append(")");
      }
    }

    sb.append(")");
    return sb.toString();
  }

  @Override
  public String export(SqlCreateSequence sqlCreateSequence) {
    StringBuilder sb = new StringBuilder();
    sb.append("create sequence ");
    sb.append(nameTranslator.adjustName(sqlCreateSequence.getSequenceName()));
    if (sqlCreateSequence.getInitialValue() != null) {
      sb.append(" start with ");
      sb.append(sqlCreateSequence.getInitialValue());
    }

    if (sqlCreateSequence.getAllocationSize() != null) {
      sb.append(" increment by ");
      sb.append(sqlCreateSequence.getAllocationSize());
    }

    return sb.toString();
  }

  @Override
  public List<String> export(List<SqlDDLStatement> sqlDDLStatement) {
    List<String> result = new ArrayList<>();
    List<SqlCreateTable> createTables = sqlDDLStatement.stream()
        .filter(c -> c instanceof SqlCreateTable)
        .map(c -> (SqlCreateTable) c).collect(Collectors.toList());

    List<String> createTableStrs = createTables.stream().map(c -> export(c))
        .collect(Collectors.toList());
    result.addAll(createTableStrs);

    List<String> createSequenceStrs = sqlDDLStatement.stream()
        .filter(c -> c instanceof SqlCreateSequence)
        .map(c -> export((SqlCreateSequence) c)).collect(Collectors.toList());
    result.addAll(createSequenceStrs);

    List<SqlCreateJoinTable> createJoinTables = sqlDDLStatement.stream()
        .filter(c -> c instanceof SqlCreateJoinTable).map(c -> (SqlCreateJoinTable) c)
        .collect(Collectors.toList());

    List<String> createJoinTableStrs = createJoinTables.stream().map(c -> export(c))
        .collect(Collectors.toList());
    result.addAll(createJoinTableStrs);

    return result;
  }

}
