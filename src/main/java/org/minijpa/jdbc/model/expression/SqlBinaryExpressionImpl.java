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
package org.minijpa.jdbc.model.expression;

import org.minijpa.jdbc.model.Value;

/**
 *
 * @author adamato
 */
public class SqlBinaryExpressionImpl implements SqlBinaryExpression, Value {

	private final SqlExpressionOperator operator;
//	private Optional<TableColumn> leftTableColumn = Optional.empty();
	private final Object leftExpression;
//	private Optional<TableColumn> rightTableColumn = Optional.empty();
	private final Object rightExpression;

	public SqlBinaryExpressionImpl(SqlExpressionOperator operator, Object leftExpression, Object rightExpression) {
		this.operator = operator;
		this.leftExpression = leftExpression;
		this.rightExpression = rightExpression;
	}

//	public SqlBinaryExpressionImpl(SqlExpressionOperator operator, TableColumn leftTableColumn, TableColumn rightTableColumn) {
//		this.operator = operator;
//		this.leftTableColumn = Optional.of(leftTableColumn);
//		this.rightTableColumn = Optional.of(rightTableColumn);
//	}
//
//	public SqlBinaryExpressionImpl(SqlExpressionOperator operator, TableColumn leftTableColumn, String rightExpression) {
//		this.operator = operator;
//		this.leftTableColumn = Optional.of(leftTableColumn);
//		this.rightExpression = Optional.of(rightExpression);
//	}
//
//	public SqlBinaryExpressionImpl(SqlExpressionOperator operator, String leftExpression, TableColumn rightTableColumn) {
//		this.operator = operator;
//		this.leftExpression = Optional.of(leftExpression);
//		this.rightTableColumn = Optional.of(rightTableColumn);
//	}
//
//	public SqlBinaryExpressionImpl(SqlExpressionOperator operator, String leftExpression, String rightExpression) {
//		this.operator = operator;
//		this.leftExpression = Optional.of(leftExpression);
//		this.rightExpression = Optional.of(rightExpression);
//	}
//
//	public SqlBinaryExpressionImpl(SqlExpressionOperator operator, TableColumn leftTableColumn, String leftExpression, TableColumn rightTableColumn, String rightExpression) {
//		this.operator = operator;
//		this.leftTableColumn = Optional.ofNullable(leftTableColumn);
//		this.leftExpression = Optional.ofNullable(leftExpression);
//		this.rightTableColumn = Optional.ofNullable(rightTableColumn);
//		this.rightExpression = Optional.ofNullable(rightExpression);
//	}
	@Override
	public SqlExpressionOperator getOperator() {
		return operator;
	}

//	public Optional<TableColumn> getLeftTableColumn() {
//		return leftTableColumn;
//	}
	@Override
	public Object getLeftExpression() {
		return leftExpression;
	}

//	public Optional<TableColumn> getRightTableColumn() {
//		return rightTableColumn;
//	}
	@Override
	public Object getRightExpression() {
		return rightExpression;
	}

}
