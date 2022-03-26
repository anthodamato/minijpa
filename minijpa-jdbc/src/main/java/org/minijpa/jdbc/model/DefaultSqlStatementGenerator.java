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
import org.minijpa.jdbc.model.function.Abs;
import org.minijpa.jdbc.model.function.Avg;
import org.minijpa.jdbc.model.function.Concat;
import org.minijpa.jdbc.model.function.Count;
import org.minijpa.jdbc.model.function.CurrentDate;
import org.minijpa.jdbc.model.function.CurrentTime;
import org.minijpa.jdbc.model.function.CurrentTimestamp;
import org.minijpa.jdbc.model.function.Function;
import org.minijpa.jdbc.model.function.Length;
import org.minijpa.jdbc.model.function.Locate;
import org.minijpa.jdbc.model.function.Lower;
import org.minijpa.jdbc.model.function.Max;
import org.minijpa.jdbc.model.function.Min;
import org.minijpa.jdbc.model.function.Mod;
import org.minijpa.jdbc.model.function.Sqrt;
import org.minijpa.jdbc.model.function.Substring;
import org.minijpa.jdbc.model.function.Sum;
import org.minijpa.jdbc.model.function.Trim;
import org.minijpa.jdbc.model.function.Upper;
import org.minijpa.jdbc.model.join.FromJoin;
import org.minijpa.jdbc.model.join.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultSqlStatementGenerator implements SqlStatementGenerator {

	private final Logger LOG = LoggerFactory.getLogger(DefaultSqlStatementGenerator.class);

	protected final DbJdbc dbJdbc;
	protected SqlStatementExporter sqlStatementExporter = new DefaultSqlStatementExporter();

	public DefaultSqlStatementGenerator(DbJdbc dbJdbc) {
		super();
		this.dbJdbc = dbJdbc;
	}

//	protected final SqlStatementExporter getSqlStatementExporter() {
//		if (sqlStatementExporter != null)
//			return sqlStatementExporter;
//
//		sqlStatementExporter = createSqlStatementExporter();
//		return sqlStatementExporter;
//	}
//
//	@Override
//	public SqlStatementExporter createSqlStatementExporter() {
//		return new DefaultSqlStatementExporter();
//	}

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
		return export(sqlUpdate, sqlStatementExporter);
	}

	protected String export(SqlUpdate sqlUpdate, SqlStatementExporter sqlStatementExporter) {
		StringBuilder sb = new StringBuilder();
		sb.append("update ");
		sb.append(dbJdbc.getNameTranslator().toTableName(sqlUpdate.getFromTable().getAlias(),
				sqlUpdate.getFromTable().getName()));
		sb.append(" set ");

		String sv = sqlUpdate.getTableColumns().stream().map(c -> {
			return sqlStatementExporter.exportTableColumn(c, dbJdbc) + " = ?";
		}).collect(Collectors.joining(", "));
		sb.append(sv);

		if (sqlUpdate.getCondition().isPresent()) {
			sb.append(" where ");
			sb.append(exportCondition(sqlUpdate.getCondition().get(), sqlStatementExporter));
		}

		return sb.toString();
	}

	@Override
	public String export(SqlDelete sqlDelete) {
		return export(sqlDelete, sqlStatementExporter);
	}

	protected String export(SqlDelete sqlDelete, SqlStatementExporter sqlStatementExporter) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ");
		sb.append(dbJdbc.getNameTranslator().toTableName(sqlDelete.getFromTable().getAlias(),
				sqlDelete.getFromTable().getName()));

		if (sqlDelete.getCondition().isPresent()) {
			sb.append(" where ");
			sb.append(exportCondition(sqlDelete.getCondition().get(), sqlStatementExporter));
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

//	private String exportOperand(Object operand) {
//		if (operand instanceof TableColumn)
//			return sqlStatementExporter.exportTableColumn((TableColumn) operand, dbJdbc);
//
//		if (operand instanceof String)
//			return (String) operand;
//
//		if (operand instanceof Boolean)
//			return dbJdbc.booleanValue((Boolean) operand);
//
//		return "";
//	}
	protected String exportFunction(Abs abs) {
		return "ABS(" + exportExpression(abs.getArgument(), sqlStatementExporter) + ")";
	}

	protected String exportFunction(Avg avg) {
		return "AVG(" + exportExpression(avg.getArgument(), sqlStatementExporter) + ")";
	}

	protected String exportFunction(Concat concat) {
		return Arrays.stream(concat.getParams()).map(p -> exportExpression(p, sqlStatementExporter))
				.collect(Collectors.joining("||"));
	}

	protected String exportFunction(Count count) {
		StringBuilder sb = new StringBuilder("COUNT(");
		if (count.isDistinct())
			sb.append("distinct ");

		sb.append(exportExpression(count.getArgument(), sqlStatementExporter));
		sb.append(")");
		return sb.toString();
	}

	protected String exportFunction(Length length) {
		return "LENGTH(" + exportExpression(length.getArgument(), sqlStatementExporter) + ")";
	}

	protected String exportFunction(Locate locate) {
		StringBuilder sb = new StringBuilder("LOCATE(");

		sb.append(exportExpression(locate.getSearchString(), sqlStatementExporter));
		sb.append(", ");
		sb.append(exportExpression(locate.getInputString(), sqlStatementExporter));
		if (locate.getPosition().isPresent()) {
			sb.append(", ");
			sb.append(exportExpression(locate.getPosition().get(), sqlStatementExporter));
		}

		sb.append(")");
		return sb.toString();
	}

	protected String exportFunction(Lower lower) {
		return "LOWER(" + exportExpression(lower.getArgument(), sqlStatementExporter) + ")";
	}

	protected String exportFunction(Upper upper) {
		return "UPPER(" + exportExpression(upper.getArgument(), sqlStatementExporter) + ")";
	}

	protected String exportFunction(Max max) {
		return "MAX(" + exportExpression(max.getArgument(), sqlStatementExporter) + ")";
	}

	protected String exportFunction(Min min) {
		return "MIN(" + exportExpression(min.getArgument(), sqlStatementExporter) + ")";
	}

	protected String exportFunction(Mod mod) {
		StringBuilder sb = new StringBuilder("MOD(");

		sb.append(exportExpression(mod.getDividend(), sqlStatementExporter));
		sb.append(", ");
		sb.append(exportExpression(mod.getDivider(), sqlStatementExporter));
		sb.append(")");
		return sb.toString();
	}

	protected String exportFunction(Sqrt sqrt) {
		return "SQRT(" + exportExpression(sqrt.getArgument(), sqlStatementExporter) + ")";
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

		sb.append(exportExpression(substring.getArgument(), sqlStatementExporter));
		sb.append(", ");
		sb.append(exportExpression(substring.getStartIndex(), sqlStatementExporter));
		if (substring.getLength().isPresent()) {
			sb.append(", ");
			sb.append(exportExpression(substring.getLength().get(), sqlStatementExporter));
		}

		sb.append(")");
		return sb.toString();
	}

	protected String exportFunction(Sum sum) {
		return "SUM(" + exportExpression(sum.getArgument(), sqlStatementExporter) + ")";
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

			if (trim.getTrimCharacter() != null) {
				sb.append(" '");
				sb.append(trim.getTrimCharacter());
				sb.append("'");
			}

			sb.append(" FROM ");
		}

		sb.append(exportExpression(trim.getArgument(), sqlStatementExporter));
		sb.append(")");
		return sb.toString();
	}

	protected String exportFunction(Function function, SqlStatementExporter sqlStatementExporter) {
		if (function instanceof Abs)
			return exportFunction((Abs) function);
		else if (function instanceof Avg)
			return exportFunction((Avg) function);
		else if (function instanceof Concat)
			return exportFunction((Concat) function);
		else if (function instanceof Count)
			return exportFunction((Count) function);
		else if (function instanceof Length)
			return exportFunction((Length) function);
		else if (function instanceof Locate)
			return exportFunction((Locate) function);
		else if (function instanceof Lower)
			return exportFunction((Lower) function);
		else if (function instanceof Max)
			return exportFunction((Max) function);
		else if (function instanceof Min)
			return exportFunction((Min) function);
		else if (function instanceof Mod)
			return exportFunction((Mod) function);
		else if (function instanceof Sqrt)
			return exportFunction((Sqrt) function);
		else if (function instanceof Substring)
			return exportFunction((Substring) function);
		else if (function instanceof Sum)
			return exportFunction((Sum) function);
		else if (function instanceof Trim)
			return exportFunction((Trim) function);
		else if (function instanceof Upper)
			return exportFunction((Upper) function);
		else if (function instanceof CurrentDate)
			return exportFunction((CurrentDate) function);
		else if (function instanceof CurrentTime)
			return exportFunction((CurrentTime) function);
		else if (function instanceof CurrentTimestamp)
			return exportFunction((CurrentTimestamp) function);

		return "";
	}

	protected String exportExpression(Object expression, SqlStatementExporter sqlStatementExporter) {
		LOG.debug("exportExpression: expression=" + expression);
		LOG.debug("exportExpression: sqlStatementExporter=" + sqlStatementExporter);
		if (expression instanceof TableColumn)
			return sqlStatementExporter.exportTableColumn((TableColumn) expression, dbJdbc);

		if (expression instanceof String)
			return (String) expression;

		if (expression instanceof Boolean)
			return dbJdbc.booleanValue((Boolean) expression);

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
				sb.append(exportExpression(obj, sqlStatementExporter));
			});

			return sb.toString();
		}

		if (expression instanceof Function)
			return exportFunction((Function) expression, sqlStatementExporter);

		return "";
	}

	private String exportSqlBinaryExpression(SqlBinaryExpression sqlBinaryExpression) {
		StringBuilder sb = new StringBuilder();
		Object leftExpression = sqlBinaryExpression.getLeftExpression();
		sb.append(exportExpression(leftExpression, sqlStatementExporter));

		sb.append(getSqlOperator(sqlBinaryExpression.getOperator()));

		sb.append(exportExpression(sqlBinaryExpression.getRightExpression(), sqlStatementExporter));

		return sb.toString();
	}

	private String exportSqlExpression(SqlExpression sqlExpression) {
		return exportExpression(sqlExpression.getExpression(), sqlStatementExporter);
	}

	protected String exportCondition(Condition condition, SqlStatementExporter sqlStatementExporter) {
		LOG.debug("exportCondition: condition=" + condition);
		if (condition instanceof BinaryLogicCondition) {
			BinaryLogicCondition binaryLogicCondition = (BinaryLogicCondition) condition;
			StringBuilder sb = new StringBuilder();
			if (binaryLogicCondition.nested())
				sb.append("(");

//	    LOG.debug("exportCondition: binaryLogicCondition.getConditions().size="
//		    + binaryLogicCondition.getConditions().size());
			String operator = " " + getOperator(condition.getConditionType()) + " ";
			String cc = binaryLogicCondition.getConditions().stream().map(c -> {
				return exportCondition(c, sqlStatementExporter);
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
				sb.append(exportCondition(unaryLogicCondition.getCondition(), sqlStatementExporter));
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
				sb.append(exportExpression(unaryCondition.getTableColumn(), sqlStatementExporter));

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

			Object left = binaryCondition.getLeft();
			LOG.debug("exportCondition: left=" + left);
			sb.append(exportExpression(left, sqlStatementExporter));

			sb.append(" ");
			sb.append(getOperator(condition.getConditionType()));
			sb.append(" ");
			Object right = binaryCondition.getRight();
			LOG.debug("exportCondition: right=" + right);
			sb.append(exportExpression(right, sqlStatementExporter));

			return sb.toString();
		}

		if (condition instanceof BetweenCondition) {
			BetweenCondition betweenCondition = (BetweenCondition) condition;
			StringBuilder sb = new StringBuilder();
			sb.append(exportExpression(betweenCondition.getOperand(), sqlStatementExporter));
			sb.append(" ");
			if (betweenCondition.isNot())
				sb.append("NOT ");

			sb.append(getOperator(condition.getConditionType()));
			sb.append(" ");

			sb.append(exportExpression(betweenCondition.getLeftExpression(), sqlStatementExporter));
			sb.append(" and ");
			sb.append(exportExpression(betweenCondition.getRightExpression(), sqlStatementExporter));

			return sb.toString();
		}

		if (condition instanceof InCondition) {
			InCondition inCondition = (InCondition) condition;
			StringBuilder sb = new StringBuilder();
			if (inCondition.isNot())
				sb.append("not ");

			sb.append(sqlStatementExporter.exportTableColumn(inCondition.getLeftColumn(), dbJdbc));
			sb.append(" ");
			sb.append(getOperator(condition.getConditionType()));
			sb.append(" (");

			String s = inCondition.getRightExpressions().stream().map(v -> exportExpression(v, sqlStatementExporter))
					.collect(Collectors.joining(", "));
			sb.append(s);
			sb.append(")");
			return sb.toString();
		}

		throw new IllegalArgumentException("Condition '" + condition + "' not supported");
	}

	protected String exportJoins(List<FromJoin> fromJoins) {
		StringBuilder sb = new StringBuilder();
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
			}
		}

		return sb.toString();
	}

	protected String exportFromTable(List<FromTable> fromTables) {
		return fromTables.stream().map(t -> {
			return dbJdbc.getNameTranslator().toTableName(t.getAlias(), t.getName());
		}).collect(Collectors.joining(", "));
	}

	private String exportGroupBy(GroupBy groupBy) {
		return "group by " + groupBy.getColumns().stream().map(c -> sqlStatementExporter.exportTableColumn(c, dbJdbc))
				.collect(Collectors.joining(", "));
	}

	private String exportOrderBy(OrderBy orderBy) {
		String ad = "";
		if (orderBy.getOrderByType() != null)
			ad = orderBy.getOrderByType() == OrderByType.ASC ? " ASC" : " DESC";

		return sqlStatementExporter.exportTableColumn(orderBy.getTableColumn(), dbJdbc) + ad;
	}

	@Override
	public String export(SqlSelect sqlSelect) {
		StringBuilder sb = new StringBuilder("select ");
		if (sqlSelect.isDistinct())
			sb.append("distinct ");

		LOG.debug("export: sqlSelect.getValues()=" + sqlSelect.getValues());

		String cc = sqlSelect.getValues().stream().map(c -> {
			if (c instanceof TableColumn)
				return sqlStatementExporter.exportTableColumn((TableColumn) c, dbJdbc);
			if (c instanceof Function)
				return exportFunction((Function) c, sqlStatementExporter);
			if (c instanceof SqlExpression)
				return exportSqlExpression((SqlExpression) c);
			if (c instanceof SqlBinaryExpression)
				return exportSqlBinaryExpression((SqlBinaryExpression) c);

			throw new IllegalArgumentException("Value type '" + c + "'not supported");
		}).collect(Collectors.joining(", "));

		sb.append(cc);
		sb.append(" from ");
		sb.append(exportFromTable(sqlSelect.getFromTables()));
		if (sqlSelect.getJoins().isPresent())
			sb.append(exportJoins(sqlSelect.getJoins().get()));

		if (sqlSelect.getConditions().isPresent()) {
			sb.append(" where ");
			String ccs = sqlSelect.getConditions().get().stream().map(c -> exportCondition(c, sqlStatementExporter))
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
		if (ddlData.isPresent() && ddlData.get().getNullable().isPresent()
				&& ddlData.get().getNullable().get() == false) {
			return s + " not null";
		}

		return s;
	}

	private String buildJoinColumnDefinition(JoinColumnAttribute joinColumnAttribute) {
		return dbJdbc.buildColumnDefinition(joinColumnAttribute);
	}

	protected String buildAttributeDeclaration(MetaAttribute attribute) {
		return dbJdbc.getNameTranslator().adjustName(attribute.getColumnName()) + " "
				+ buildColumnDefinition(attribute);
	}

	private String buildPkDeclaration(Pk pk) {
		if (pk.getPkGeneration().getPkStrategy() == PkStrategy.IDENTITY) {
			return dbJdbc.getNameTranslator().adjustName(pk.getAttribute().getColumnName()) + " "
					+ dbJdbc.buildIdentityColumnDefinition(pk.getAttribute());
		}

		String cols = pk.getAttributes().stream().map(a -> buildAttributeDeclaration(a))
				.collect(Collectors.joining(", "));

		return cols;
	}

	protected String buildDeclaration(JoinColumnAttribute joinColumnAttribute) {
		return dbJdbc.getNameTranslator().adjustName(joinColumnAttribute.getColumnName()) + " "
				+ buildJoinColumnDefinition(joinColumnAttribute);
	}

	protected String buildJoinTableColumnDeclaration(JoinColumnAttribute joinColumnAttribute) {
		return dbJdbc.getNameTranslator().adjustName(joinColumnAttribute.getColumnName()) + " "
				+ buildJoinColumnDefinition(joinColumnAttribute) + " not null";
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
			cols = sqlCreateTable.getAttributes().stream().map(a -> buildAttributeDeclaration(a))
					.collect(Collectors.joining(", "));
			sb.append(cols);
		}

		for (ForeignKeyDeclaration foreignKeyDeclaration : sqlCreateTable.getForeignKeyDeclarations()) {
			sb.append(", ");
			cols = foreignKeyDeclaration.getJoinColumnMapping().getJoinColumnAttributes().stream()
					.map(a -> buildDeclaration(a)).collect(Collectors.joining(", "));
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
				.map(d -> d.getJoinColumnMapping().getJoinColumnAttributes()).flatMap(List::stream)
				.collect(Collectors.toList());
		String cols = joinColumnAttributes.stream().map(a -> buildJoinTableColumnDeclaration(a))
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
		LOG.debug("export: sqlCreateSequence.getPkSequenceGenerator().getSequenceName()="
				+ sqlCreateSequence.getPkSequenceGenerator().getSequenceName());
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
		List<SqlCreateTable> createTables = sqlDDLStatement.stream().filter(c -> c instanceof SqlCreateTable)
				.map(c -> (SqlCreateTable) c).collect(Collectors.toList());

		List<String> createTableStrs = createTables.stream().map(c -> export(c)).collect(Collectors.toList());
		result.addAll(createTableStrs);

		List<PkSequenceGenerator> pkSequenceGenerators = createTables.stream()
				.filter(c -> c.getPk().getPkGeneration().getPkStrategy() == PkStrategy.SEQUENCE)
				.map(c -> c.getPk().getPkGeneration().getPkSequenceGenerator()).distinct().collect(Collectors.toList());
		List<String> createSequenceStrs = pkSequenceGenerators.stream().map(c -> new SqlCreateSequence(c))
				.map(c -> export(c)).collect(Collectors.toList());
		result.addAll(createSequenceStrs);

		if (sqlDDLStatement instanceof SqlCreateTable) {
			String s = export((SqlCreateTable) sqlDDLStatement);

			SqlCreateTable sqlCreateTable = (SqlCreateTable) sqlDDLStatement;
			if (sqlCreateTable.getPk().getPkGeneration().getPkStrategy() == PkStrategy.SEQUENCE) {
				SqlCreateSequence sqlCreateSequence = new SqlCreateSequence(
						sqlCreateTable.getPk().getPkGeneration().getPkSequenceGenerator());
				String sc = export(sqlCreateSequence);
				return Arrays.asList(s, sc);
			}

			return Arrays.asList(s);
		}

//	if (sqlDDLStatement instanceof SqlCreateJoinTable)
//	    return Arrays.asList(export((SqlCreateJoinTable) sqlDDLStatement));
		List<SqlCreateJoinTable> createJoinTables = sqlDDLStatement.stream()
				.filter(c -> c instanceof SqlCreateJoinTable).map(c -> (SqlCreateJoinTable) c)
				.collect(Collectors.toList());

		List<String> createJoinTableStrs = createJoinTables.stream().map(c -> export(c)).collect(Collectors.toList());
		result.addAll(createJoinTableStrs);

		return result;
	}

}
