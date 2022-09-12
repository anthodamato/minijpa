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
package org.minijpa.sql.model.expression;

import org.minijpa.sql.model.Value;

/**
 *
 * @author adamato
 */
public class SqlBinaryExpressionImpl implements SqlBinaryExpression, Value {

	private final SqlExpressionOperator operator;
	private final Object leftExpression;
	private final Object rightExpression;

	public SqlBinaryExpressionImpl(SqlExpressionOperator operator, Object leftExpression, Object rightExpression) {
		this.operator = operator;
		this.leftExpression = leftExpression;
		this.rightExpression = rightExpression;
	}

	@Override
	public SqlExpressionOperator getOperator() {
		return operator;
	}

	@Override
	public Object getLeftExpression() {
		return leftExpression;
	}

	@Override
	public Object getRightExpression() {
		return rightExpression;
	}

}
