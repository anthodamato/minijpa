package org.minijpa.jdbc.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.minijpa.jdbc.db.DbJdbc;
import org.minijpa.jdbc.model.aggregate.AggregateFunction;
import org.minijpa.jdbc.model.aggregate.Count;
import org.minijpa.jdbc.model.aggregate.GroupBy;
import org.minijpa.jdbc.model.aggregate.Max;
import org.minijpa.jdbc.model.aggregate.Min;
import org.minijpa.jdbc.model.aggregate.Sum;
import org.minijpa.jdbc.model.condition.BetweenCondition;
import org.minijpa.jdbc.model.condition.BinaryCondition;
import org.minijpa.jdbc.model.condition.BinaryLogicCondition;
import org.minijpa.jdbc.model.condition.Condition;
import org.minijpa.jdbc.model.condition.ConditionType;
import org.minijpa.jdbc.model.condition.UnaryCondition;
import org.minijpa.jdbc.model.condition.UnaryLogicCondition;
import org.minijpa.jdbc.model.join.FromJoin;
import org.minijpa.jdbc.model.join.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlStatementGenerator {
	private Logger LOG = LoggerFactory.getLogger(SqlStatementGenerator.class);

	private DbJdbc dbJdbc;

	public SqlStatementGenerator(DbJdbc dbJdbc) {
		super();
		this.dbJdbc = dbJdbc;
	}

	public String export(SqlInsert sqlInsert) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(sqlInsert.getFromTable().getName());
		sb.append(" (");
		String cols = sqlInsert.getColumns().stream().map(a -> a.getName()).collect(Collectors.joining(","));
		sb.append(cols);
		sb.append(") values (");

		for (int i = 0; i < sqlInsert.getParameters().size(); ++i) {
			if (i > 0)
				sb.append(",");

			sb.append("?");
		}

		sb.append(")");
		return sb.toString();
	}

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
		if (aggregateFunction instanceof Sum)
			return "sum(" + exportTableColumn(((Sum) aggregateFunction).getTableColumn()) + ")";

		if (aggregateFunction instanceof Min)
			return "min(" + exportTableColumn(((Min) aggregateFunction).getTableColumn()) + ")";

		if (aggregateFunction instanceof Max)
			return "max(" + exportTableColumn(((Max) aggregateFunction).getTableColumn()) + ")";

//		if (aggregateFunction instanceof Distinct)
//			return "distinct " + exportTableColumn(((Distinct) aggregateFunction).getTableColumn());

		if (aggregateFunction instanceof Count) {
			Count count = (Count) aggregateFunction;
			if (count.getExpression().isPresent())
				return "count(" + count.getExpression().get() + ")";

			if (count.getAggregateFunction().isPresent())
				return exportAggregateFunction(count.getAggregateFunction().get());
		}

		throw new IllegalArgumentException("Aggregate function '" + aggregateFunction + "'not supported");
	}

	private String exportCondition(Condition condition) {
		LOG.info("exportCondition: condition=" + condition);
		if (condition instanceof BinaryLogicCondition) {
			BinaryLogicCondition binaryLogicCondition = (BinaryLogicCondition) condition;
			StringBuilder sb = new StringBuilder();
			if (binaryLogicCondition.nested())
				sb.append("(");

			LOG.info("exportCondition: binaryLogicCondition.getConditions().size="
					+ binaryLogicCondition.getConditions().size());
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
//			if (binaryCondition.getConditionType() == ConditionType.LIKE) {
//				sb.append(exportTableColumn(binaryCondition.getLeftColumn().get()));
//				sb.append(" ");
//				sb.append(getOperator(condition.getConditionType()));
//				sb.append(" ");
//				sb.append(binaryCondition.getRightExpression().get());
//			} else {

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
//			}

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
					if (i > 0) {
						sb.append(" AND ");
					}

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

	public String export(SqlSelect sqlSelect) {
		StringBuilder sb = new StringBuilder("select ");
		if (sqlSelect.isDistinct())
			sb.append("distinct ");

		LOG.info("export: sqlSelect.getValues()=" + sqlSelect.getValues());

		String cc = sqlSelect.getValues().stream().map(c -> {
			if (c instanceof TableColumn)
				return exportTableColumn((TableColumn) c);
			if (c instanceof AggregateFunction)
				return exportAggregateFunction((AggregateFunction) c);

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
			LOG.info("export: ccs=" + ccs);
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
		case LESS_THAN:
			return dbJdbc.lessThanOperator();
		case BETWEEN:
			return dbJdbc.betweenOperator();
		case LIKE:
			return dbJdbc.likeOperator();
		default:
			break;
		}

		throw new IllegalArgumentException("Unknown operator for condition type: " + conditionType);
	}

}
