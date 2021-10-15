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

import org.minijpa.jdbc.model.TableColumn;

public class SqlBinaryExpressionBuilder {

	private final SqlExpressionOperator operator;
//	private TableColumn leftTableColumn;
	private Object leftExpression;
//	private TableColumn rightTableColumn;
	private Object rightExpression;

	public SqlBinaryExpressionBuilder(SqlExpressionOperator operator) {
		this.operator = operator;
	}

//	public SqlBinaryExpressionBuilder setLeftTableColumn(TableColumn leftTableColumn) {
//		this.leftTableColumn = leftTableColumn;
//		return this;
//	}
	public SqlBinaryExpressionBuilder setLeftExpression(Object leftExpression) {
		this.leftExpression = leftExpression;
		return this;
	}

//	public SqlBinaryExpressionBuilder setRightTableColumn(TableColumn rightTableColumn) {
//		this.rightTableColumn = rightTableColumn;
//		return this;
//	}
	public SqlBinaryExpressionBuilder setRightExpression(Object rightExpression) {
		this.rightExpression = rightExpression;
		return this;
	}

	public SqlBinaryExpression build() {
		return new SqlBinaryExpressionImpl(operator, leftExpression, rightExpression);
	}

}
