package org.minijpa.jdbc.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.minijpa.jdbc.DDLData;
import org.minijpa.jdbc.JoinColumnAttribute;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.Pk;
import org.minijpa.jdbc.PkSequenceGenerator;
import org.minijpa.jdbc.PkStrategy;

import org.minijpa.jdbc.db.DbJdbc;
import org.minijpa.jdbc.model.aggregate.AggregateFunction;
import org.minijpa.jdbc.model.aggregate.BasicAggregateFunction;
import org.minijpa.jdbc.model.aggregate.GroupBy;
import org.minijpa.jdbc.model.condition.BetweenCondition;
import org.minijpa.jdbc.model.condition.BinaryCondition;
import org.minijpa.jdbc.model.condition.BinaryLogicCondition;
import org.minijpa.jdbc.model.condition.Condition;
import org.minijpa.jdbc.model.condition.ConditionType;
import org.minijpa.jdbc.model.condition.InCondition;
import org.minijpa.jdbc.model.condition.UnaryCondition;
import org.minijpa.jdbc.model.condition.UnaryLogicCondition;
import org.minijpa.jdbc.model.expression.SqlBinaryExpression;
import org.minijpa.jdbc.model.expression.SqlExpression;
import org.minijpa.jdbc.model.expression.SqlExpressionOperator;
import org.minijpa.jdbc.model.join.FromJoin;
import org.minijpa.jdbc.model.join.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultSqlStatementGenerator implements SqlStatementGenerator {

    private final Logger LOG = LoggerFactory.getLogger(DefaultSqlStatementGenerator.class);

    protected final DbJdbc dbJdbc;

    public DefaultSqlStatementGenerator(DbJdbc dbJdbc) {
	super();
	this.dbJdbc = dbJdbc;
    }

    @Override
    public String export(SqlInsert sqlInsert) {
	StringBuilder sb = new StringBuilder();
	sb.append("insert into ");
	sb.append(sqlInsert.getFromTable().getName());
	sb.append(" (");
	String cols = sqlInsert.getColumns().stream().map(a -> a.getName()).collect(Collectors.joining(","));
	sb.append(cols);
	sb.append(") values (");

	for (int i = 0; i < sqlInsert.getColumns().size(); ++i) {
	    if (i > 0)
		sb.append(",");

	    sb.append("?");
	}

	sb.append(")");
	return sb.toString();
    }

    @Override
    public String export(SqlUpdate sqlUpdate) {
	StringBuilder sb = new StringBuilder();
	sb.append("update ");
	sb.append(dbJdbc.getNameTranslator().toTableName(sqlUpdate.getFromTable().getAlias(),
		sqlUpdate.getFromTable().getName()));
	sb.append(" set ");

	String sv = sqlUpdate.getTableColumns().stream().map(c -> {
	    return exportTableColumn(c) + " = ?";
	}).collect(Collectors.joining(", "));
	sb.append(sv);

	if (sqlUpdate.getCondition().isPresent()) {
	    sb.append(" where ");
	    sb.append(exportCondition(sqlUpdate.getCondition().get()));
	}

	return sb.toString();
    }

    @Override
    public String export(SqlDelete sqlDelete) {
	StringBuilder sb = new StringBuilder();
	sb.append("delete from ");
	sb.append(dbJdbc.getNameTranslator().toTableName(sqlDelete.getFromTable().getAlias(),
		sqlDelete.getFromTable().getName()));

	if (sqlDelete.getCondition().isPresent()) {
	    sb.append(" where ");
	    sb.append(exportCondition(sqlDelete.getCondition().get()));
	}

	return sb.toString();
    }

    private String exportColumn(Column column) {
	if (column.getAlias().isPresent())
	    return column.getName() + " AS " + column.getAlias().get();

	return column.getName();
    }

    private String exportColumnAlias(String columnName, Optional<String> alias) {
	if (alias.isPresent())
	    return columnName + " AS " + alias.get();

	return columnName;
    }

    private String exportTableColumn(TableColumn tableColumn) {
	Optional<FromTable> optionalFromTable = tableColumn.getTable();
	Column column = tableColumn.getColumn();
	if (optionalFromTable.isPresent()) {
	    String tc = dbJdbc.getNameTranslator().toColumnName(optionalFromTable.get().getAlias(), column.getName());
	    return exportColumnAlias(tc, column.getAlias());
	}

	if (tableColumn.getSubQuery().isPresent() && tableColumn.getSubQuery().get().getAlias().isPresent())
	    return tableColumn.getSubQuery().get().getAlias().get() + "." + exportColumn(column);

	String c = dbJdbc.getNameTranslator().toColumnName(Optional.empty(), column.getName());
	return exportColumnAlias(c, column.getAlias());
    }

    private String exportAggregateFunction(AggregateFunction aggregateFunction) {
	BasicAggregateFunction basicAggregateFunction = (BasicAggregateFunction) aggregateFunction;
	switch (aggregateFunction.getType()) {
	    case AVG:
		return "avg(" + exportTableColumn(basicAggregateFunction.getTableColumn().get()) + ")";
	    case SUM:
		return "sum(" + exportTableColumn(basicAggregateFunction.getTableColumn().get()) + ")";
	    case MIN:
		return "min(" + exportTableColumn(basicAggregateFunction.getTableColumn().get()) + ")";
	    case MAX:
		return "max(" + exportTableColumn(basicAggregateFunction.getTableColumn().get()) + ")";
	    case COUNT:
		StringBuilder sb = new StringBuilder("count(");
		if (basicAggregateFunction.isDistinct())
		    sb.append("distinct ");

		if (basicAggregateFunction.getExpression().isPresent())
		    sb.append(basicAggregateFunction.getExpression().get());

		if (basicAggregateFunction.getTableColumn().isPresent())
		    sb.append(exportTableColumn(basicAggregateFunction.getTableColumn().get()));

		sb.append(")");
		return sb.toString();
	    default:
		break;
	}

	throw new IllegalArgumentException("Aggregate function '" + aggregateFunction + "' not supported");
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

    private String exportBinaryExpression(SqlBinaryExpression sqlBinaryExpression) {
	StringBuilder sb = new StringBuilder();
	if (sqlBinaryExpression.getLeftTableColumn().isPresent())
	    sb.append(exportTableColumn(sqlBinaryExpression.getLeftTableColumn().get()));

	if (sqlBinaryExpression.getLeftExpression().isPresent())
	    sb.append(sqlBinaryExpression.getLeftExpression().get());

	sb.append(getSqlOperator(sqlBinaryExpression.getOperator()));
	if (sqlBinaryExpression.getRightTableColumn().isPresent())
	    sb.append(exportTableColumn(sqlBinaryExpression.getRightTableColumn().get()));

	if (sqlBinaryExpression.getRightExpression().isPresent())
	    sb.append(sqlBinaryExpression.getRightExpression().get());

	return sb.toString();
    }

    private String exportExpression(SqlExpression sqlExpression) {
	if (sqlExpression instanceof SqlBinaryExpression)
	    return exportBinaryExpression((SqlBinaryExpression) sqlExpression);

	throw new IllegalArgumentException("Expression '" + sqlExpression + "' not supported");
    }

    private String exportCondition(Condition condition) {
//	LOG.debug("exportCondition: condition=" + condition);
	if (condition instanceof BinaryLogicCondition) {
	    BinaryLogicCondition binaryLogicCondition = (BinaryLogicCondition) condition;
	    StringBuilder sb = new StringBuilder();
	    if (binaryLogicCondition.nested())
		sb.append("(");

//	    LOG.debug("exportCondition: binaryLogicCondition.getConditions().size="
//		    + binaryLogicCondition.getConditions().size());
	    String operator = " " + getOperator(condition.getConditionType()) + " ";
	    String cc = binaryLogicCondition.getConditions().stream().map(c -> {
		return exportCondition(c);
	    }).collect(Collectors.joining(operator));
	    sb.append(cc);

	    if (binaryLogicCondition.nested())
		sb.append(")");

	    return sb.toString();
	}

	if (condition instanceof UnaryLogicCondition) {
	    UnaryLogicCondition unaryLogicCondition = (UnaryLogicCondition) condition;
	    StringBuilder sb = new StringBuilder();
	    if (unaryLogicCondition.getConditionType() == ConditionType.NOT) {
		sb.append(getOperator(condition.getConditionType()));
		sb.append(" (");
		sb.append(exportCondition(unaryLogicCondition.getCondition()));
		sb.append(" )");
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
	    if (unaryCondition.getConditionType() == ConditionType.IS_TRUE
		    || unaryCondition.getConditionType() == ConditionType.IS_FALSE
		    || unaryCondition.getConditionType() == ConditionType.IS_NULL
		    || unaryCondition.getConditionType() == ConditionType.IS_NOT_NULL) {
		if (unaryCondition.getTableColumn().isPresent())
		    sb.append(exportTableColumn(unaryCondition.getTableColumn().get()));

		if (unaryCondition.getExpression().isPresent())
		    sb.append(unaryCondition.getExpression().get());

		sb.append(" ");
		sb.append(getOperator(condition.getConditionType()));
	    }

	    return sb.toString();
	}

//		if (condition instanceof LikeCondition) {
//			LikeCondition likeCondition = (LikeCondition) condition;
//			return exportColumn(likeCondition.getColumn()) + getOperator(condition.getConditionType()) + " '"
//					+ likeCondition.getExpression() + "'";
//		}
	if (condition instanceof BinaryCondition) {
	    BinaryCondition binaryCondition = (BinaryCondition) condition;

	    StringBuilder sb = new StringBuilder();
	    if (binaryCondition.isNot())
		sb.append("not ");

	    if (binaryCondition.getLeftColumn().isPresent())
		sb.append(exportTableColumn(binaryCondition.getLeftColumn().get()));

	    if (binaryCondition.getLeftExpression().isPresent())
		sb.append(binaryCondition.getLeftExpression().get());

	    sb.append(" ");
	    sb.append(getOperator(condition.getConditionType()));
	    sb.append(" ");
	    if (binaryCondition.getRightColumn().isPresent())
		sb.append(exportTableColumn(binaryCondition.getRightColumn().get()));

	    if (binaryCondition.getRightExpression().isPresent())
		sb.append(binaryCondition.getRightExpression().get());

	    return sb.toString();
	}

	if (condition instanceof BetweenCondition) {
	    BetweenCondition betweenCondition = (BetweenCondition) condition;
	    StringBuilder sb = new StringBuilder();
	    sb.append(exportTableColumn(betweenCondition.getTableColumn()));
	    sb.append(" ");
	    sb.append(getOperator(condition.getConditionType()));
	    sb.append(" ");

	    if (betweenCondition.getLeftColumn().isPresent())
		sb.append(exportTableColumn(betweenCondition.getLeftColumn().get()));

	    if (betweenCondition.getLeftExpression().isPresent())
		sb.append(betweenCondition.getLeftExpression().get());

	    sb.append(" AND ");
	    if (betweenCondition.getRightColumn().isPresent())
		sb.append(exportTableColumn(betweenCondition.getRightColumn().get()));

	    if (betweenCondition.getRightExpression().isPresent())
		sb.append(betweenCondition.getRightExpression().get());

	    return sb.toString();
	}

	if (condition instanceof InCondition) {
	    InCondition inCondition = (InCondition) condition;
	    StringBuilder sb = new StringBuilder();
	    if (inCondition.isNot())
		sb.append("not ");

	    sb.append(exportTableColumn(inCondition.getLeftColumn()));
	    sb.append(" ");
	    sb.append(getOperator(condition.getConditionType()));
	    sb.append(" (");

	    String s = inCondition.getRightExpressions().stream().collect(Collectors.joining(", "));
	    sb.append(s);
	    sb.append(")");
	    return sb.toString();
	}

	throw new IllegalArgumentException("Condition '" + condition + "'not supported");
    }

    private String exportJoins(FromTable fromTable) {
	StringBuilder sb = new StringBuilder();
	if (!fromTable.getJoins().isPresent())
	    return sb.toString();

	List<FromJoin> fromJoins = fromTable.getJoins().get();
	for (FromJoin fromJoin : fromJoins) {
	    if (fromJoin.getType() == JoinType.InnerJoin) {
		sb.append(" INNER JOIN ");
		FromTable toTable = fromJoin.getToTable();
		sb.append(dbJdbc.getNameTranslator().toTableName(toTable.getAlias(), toTable.getName()));
		sb.append(" ON ");
		List<Column> fromColumns = fromJoin.getFromColumns();
		List<Column> toColumns = fromJoin.getToColumns();
		for (int i = 0; i < fromColumns.size(); ++i) {
		    if (i > 0)
			sb.append(" AND ");

		    if (fromTable.getAlias().isPresent()) {
			sb.append(fromTable.getAlias().get());
			sb.append(".");
		    }

		    sb.append(fromColumns.get(i).getName());
		    sb.append(" = ");
		    if (toTable.getAlias().isPresent()) {
			sb.append(toTable.getAlias().get());
			sb.append(".");
		    }

		    sb.append(toColumns.get(i).getName());
		}
	    }
	}

	return sb.toString();
    }

    private String exportFromTable(FromTable fromTable) {
	StringBuilder sb = new StringBuilder(
		dbJdbc.getNameTranslator().toTableName(fromTable.getAlias(), fromTable.getName()));

	sb.append(exportJoins(fromTable));
	return sb.toString();
    }

    private String exportGroupBy(GroupBy groupBy) {
	return "group by "
		+ groupBy.getColumns().stream().map(c -> exportTableColumn(c)).collect(Collectors.joining(", "));
    }

    private String exportOrderBy(OrderBy orderBy) {
	String ad = orderBy.isAscending() ? " ASC" : " DESC";
	return exportTableColumn(orderBy.getTableColumn()) + ad;
    }

    @Override
    public String export(SqlSelect sqlSelect) {
	StringBuilder sb = new StringBuilder("select ");
	if (sqlSelect.isDistinct())
	    sb.append("distinct ");

	LOG.debug("export: sqlSelect.getValues()=" + sqlSelect.getValues());

	String cc = sqlSelect.getValues().stream().map(c -> {
	    if (c instanceof TableColumn)
		return exportTableColumn((TableColumn) c);
	    if (c instanceof AggregateFunction)
		return exportAggregateFunction((AggregateFunction) c);
	    if (c instanceof SqlExpression)
		return exportExpression((SqlExpression) c);

	    throw new IllegalArgumentException("Value type '" + c + "'not supported");
	}).collect(Collectors.joining(", "));

	sb.append(cc);
	sb.append(" from ");
	sb.append(exportFromTable(sqlSelect.getFromTable()));

	if (sqlSelect.getConditions().isPresent()) {
	    sb.append(" where ");
	    String ccs = sqlSelect.getConditions().get().stream().map(c -> exportCondition(c))
		    .collect(Collectors.joining(" "));
	    sb.append(ccs);
	    LOG.debug("export: ccs=" + ccs);
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

	String forUpdate = dbJdbc.forUpdate(sqlSelect.getLockType());
	if (forUpdate != null && !forUpdate.isEmpty()) {
	    sb.append(" ");
	    sb.append(forUpdate);
	}

	return sb.toString();
    }

    private String getOperator(ConditionType conditionType) {
	switch (conditionType) {
	    case EQUAL:
		return dbJdbc.equalOperator();
	    case NOT_EQUAL:
		return dbJdbc.notEqualOperator();
	    case AND:
		return dbJdbc.andOperator();
	    case IS_FALSE:
		return dbJdbc.falseOperator();
	    case IS_NOT_NULL:
		return dbJdbc.notNullOperator();
	    case IS_NULL:
		return dbJdbc.isNullOperator();
	    case IS_TRUE:
		return dbJdbc.trueOperator();
	    case NOT:
		return dbJdbc.notOperator();
	    case OR:
		return dbJdbc.orOperator();
	    case EMPTY_CONJUNCTION:
		return dbJdbc.emptyConjunctionOperator();
	    case EMPTY_DISJUNCTION:
		return dbJdbc.emptyDisjunctionOperator();
	    case GREATER_THAN:
		return dbJdbc.greaterThanOperator();
	    case GREATER_THAN_OR_EQUAL_TO:
		return dbJdbc.greaterThanOrEqualToOperator();
	    case LESS_THAN:
		return dbJdbc.lessThanOperator();
	    case LESS_THAN_OR_EQUAL_TO:
		return dbJdbc.lessThanOrEqualToOperator();
	    case BETWEEN:
		return dbJdbc.betweenOperator();
	    case LIKE:
		return dbJdbc.likeOperator();
	    case IN:
		return dbJdbc.inOperator();
	    default:
		break;
	}

	throw new IllegalArgumentException("Unknown operator for condition type: " + conditionType);
    }

    private String buildColumnDefinition(MetaAttribute attribute) {
	Optional<DDLData> ddlData = attribute.getDdlData();
	if (ddlData.isPresent()) {
	    if (ddlData.get().getColumnDefinition().isPresent())
		return ddlData.get().getColumnDefinition().get();
	}

	String s = dbJdbc.buildColumnDefinition(attribute);
	if (ddlData.isPresent() && ddlData.get().getNullable().isPresent() && ddlData.get().getNullable().get() == false) {
	    return s + " not null";
	}

	return s;
    }

    private String buildJoinColumnDefinition(JoinColumnAttribute joinColumnAttribute) {
	return dbJdbc.buildColumnDefinition(joinColumnAttribute);
    }

    protected String buildAttributeDeclaration(MetaAttribute attribute) {
	return dbJdbc.getNameTranslator().adjustName(attribute.getColumnName())
		+ " " + buildColumnDefinition(attribute);
    }

    private String buildPkDeclaration(Pk pk) {
	if (pk.getPkGeneration().getPkStrategy() == PkStrategy.IDENTITY) {
	    return dbJdbc.getNameTranslator().adjustName(pk.getAttribute().getColumnName())
		    + " " + dbJdbc.buildIdentityColumnDefinition(pk.getAttribute());
	}

	String cols = pk.getAttributes().stream()
		.map(a -> buildAttributeDeclaration(a))
		.collect(Collectors.joining(", "));

	return cols;
    }

    protected String buildDeclaration(JoinColumnAttribute joinColumnAttribute) {
	return dbJdbc.getNameTranslator().adjustName(joinColumnAttribute.getColumnName())
		+ " " + buildJoinColumnDefinition(joinColumnAttribute);
    }

    protected String buildJoinTableColumnDeclaration(JoinColumnAttribute joinColumnAttribute) {
	return dbJdbc.getNameTranslator().adjustName(joinColumnAttribute.getColumnName())
		+ " " + buildJoinColumnDefinition(joinColumnAttribute) + " not null";
    }

    @Override
    public String export(SqlCreateTable sqlCreateTable) {
	StringBuilder sb = new StringBuilder();
	sb.append("create table ");
	sb.append(dbJdbc.getNameTranslator().adjustName(sqlCreateTable.getTableName()));
	sb.append(" (");
	String cols = buildPkDeclaration(sqlCreateTable.getPk());
	sb.append(cols);

	if (!sqlCreateTable.getAttributes().isEmpty()) {
	    sb.append(", ");
	    cols = sqlCreateTable.getAttributes().stream()
		    .map(a -> buildAttributeDeclaration(a))
		    .collect(Collectors.joining(", "));
	    sb.append(cols);
	}

	for (ForeignKeyDeclaration foreignKeyDeclaration : sqlCreateTable.getForeignKeyDeclarations()) {
	    sb.append(", ");
	    cols = foreignKeyDeclaration.getJoinColumnMapping().getJoinColumnAttributes().stream()
		    .map(a -> buildDeclaration(a))
		    .collect(Collectors.joining(", "));
	    sb.append(cols);
	}

	sb.append(", primary key ");
	if (sqlCreateTable.getPk().isComposite()) {
	    sb.append("(");
	    cols = sqlCreateTable.getPk().getAttributes().stream()
		    .map(a -> dbJdbc.getNameTranslator().adjustName(a.getColumnName()))
		    .collect(Collectors.joining(", "));
	    sb.append(cols);
	    sb.append(")");
	} else {
	    sb.append("(");
	    sb.append(dbJdbc.getNameTranslator().adjustName(sqlCreateTable.getPk().getAttribute().getColumnName()));
	    sb.append(")");
	}

	// foreign keys
	for (ForeignKeyDeclaration foreignKeyDeclaration : sqlCreateTable.getForeignKeyDeclarations()) {
	    sb.append(", foreign key (");
	    cols = foreignKeyDeclaration.getJoinColumnMapping().getJoinColumnAttributes().stream()
		    .map(a -> dbJdbc.getNameTranslator().adjustName(a.getColumnName()))
		    .collect(Collectors.joining(", "));
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
	sb.append(dbJdbc.getNameTranslator().adjustName(sqlCreateJoinTable.getTableName()));
	sb.append(" (");
	List<JoinColumnAttribute> joinColumnAttributes = sqlCreateJoinTable.getForeignKeyDeclarations().stream()
		.map(d -> d.getJoinColumnMapping().getJoinColumnAttributes())
		.flatMap(List::stream).collect(Collectors.toList());
	String cols = joinColumnAttributes.stream()
		.map(a -> buildJoinTableColumnDeclaration(a))
		.collect(Collectors.joining(", "));
	sb.append(cols);

	// foreign keys
	for (ForeignKeyDeclaration foreignKeyDeclaration : sqlCreateJoinTable.getForeignKeyDeclarations()) {
	    sb.append(", foreign key (");
	    cols = foreignKeyDeclaration.getJoinColumnMapping().getJoinColumnAttributes().stream()
		    .map(a -> dbJdbc.getNameTranslator().adjustName(a.getColumnName()))
		    .collect(Collectors.joining(", "));
	    sb.append(cols);
	    sb.append(") references ");
	    sb.append(foreignKeyDeclaration.getReferenceTable());
	}

	sb.append(")");
	return sb.toString();
    }

    @Override
    public String export(SqlCreateSequence sqlCreateSequence) {
	StringBuilder sb = new StringBuilder();
	sb.append("create sequence ");
	LOG.debug("export: sqlCreateSequence.getPkSequenceGenerator().getSequenceName()=" + sqlCreateSequence.getPkSequenceGenerator().getSequenceName());
	sb.append(dbJdbc.getNameTranslator().adjustName(sqlCreateSequence.getPkSequenceGenerator().getSequenceName()));
	sb.append(" start with ");
	sb.append(sqlCreateSequence.getPkSequenceGenerator().getInitialValue());
	sb.append(" increment by ");
	sb.append(sqlCreateSequence.getPkSequenceGenerator().getAllocationSize());
	return sb.toString();
    }

    @Override
    public List<String> export(List<SqlDDLStatement> sqlDDLStatement) {
	List<String> result = new ArrayList<>();
	List<SqlCreateTable> createTables = sqlDDLStatement.stream()
		.filter(c -> c instanceof SqlCreateTable)
		.map(c -> (SqlCreateTable) c)
		.collect(Collectors.toList());

	List<String> createTableStrs = createTables.stream().map(c -> export(c)).collect(Collectors.toList());
	result.addAll(createTableStrs);

	List<PkSequenceGenerator> pkSequenceGenerators = createTables.stream()
		.filter(c -> c.getPk().getPkGeneration().getPkStrategy() == PkStrategy.SEQUENCE)
		.map(c -> c.getPk().getPkGeneration().getPkSequenceGenerator()).distinct()
		.collect(Collectors.toList());
	List<String> createSequenceStrs = pkSequenceGenerators.stream()
		.map(c -> new SqlCreateSequence(c))
		.map(c -> export(c))
		.collect(Collectors.toList());
	result.addAll(createSequenceStrs);

	if (sqlDDLStatement instanceof SqlCreateTable) {
	    String s = export((SqlCreateTable) sqlDDLStatement);

	    SqlCreateTable sqlCreateTable = (SqlCreateTable) sqlDDLStatement;
	    if (sqlCreateTable.getPk().getPkGeneration().getPkStrategy() == PkStrategy.SEQUENCE) {
		SqlCreateSequence sqlCreateSequence = new SqlCreateSequence(sqlCreateTable.getPk().getPkGeneration().getPkSequenceGenerator());
		String sc = export(sqlCreateSequence);
		return Arrays.asList(s, sc);
	    }

	    return Arrays.asList(s);
	}

//	if (sqlDDLStatement instanceof SqlCreateJoinTable)
//	    return Arrays.asList(export((SqlCreateJoinTable) sqlDDLStatement));
	List<SqlCreateJoinTable> createJoinTables = sqlDDLStatement.stream()
		.filter(c -> c instanceof SqlCreateJoinTable)
		.map(c -> (SqlCreateJoinTable) c)
		.collect(Collectors.toList());

	List<String> createJoinTableStrs = createJoinTables.stream().map(c -> export(c)).collect(Collectors.toList());
	result.addAll(createJoinTableStrs);

	return result;
    }

}
