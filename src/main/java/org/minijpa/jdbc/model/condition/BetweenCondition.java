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
package org.minijpa.jdbc.model.condition;

public class BetweenCondition implements Condition {

	private final Object operand;
	private Object leftExpression;
	private Object rightExpression;
	private boolean not = false;

	private BetweenCondition(Object operand) {
		super();
		this.operand = operand;
	}

	@Override
	public ConditionType getConditionType() {
		return ConditionType.BETWEEN;
	}

	public Object getOperand() {
		return operand;
	}

	public Object getLeftExpression() {
		return leftExpression;
	}

	public Object getRightExpression() {
		return rightExpression;
	}

	public boolean isNot() {
		return not;
	}

	public static class Builder {

		private final Object operand;
		private Object leftExpression;
		private Object rightExpression;
		private boolean not = false;

		public Builder(Object operand) {
			this.operand = operand;
		}

		public Builder withLeftExpression(Object expr) {
			this.leftExpression = expr;
			return this;
		}

		public Builder withRightExpression(Object expr) {
			this.rightExpression = expr;
			return this;
		}

		public Builder withNot(boolean not) {
			this.not = not;
			return this;
		}

		public BetweenCondition build() {
			BetweenCondition betweenCondition = new BetweenCondition(operand);
			betweenCondition.leftExpression = leftExpression;
			betweenCondition.rightExpression = rightExpression;
			betweenCondition.not = not;
			return betweenCondition;
		}
	}
}
