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

import java.util.Optional;

import org.minijpa.jdbc.model.TableColumn;

public class BinaryCondition implements Condition {

    private ConditionType conditionType;
    private Optional<TableColumn> leftColumn;
    private Optional<TableColumn> rightColumn;
    private Optional<String> leftExpression;
    private Optional<String> rightExpression;
    private boolean not = false;

    @Override
    public ConditionType getConditionType() {
	return conditionType;
    }

    public Optional<TableColumn> getLeftColumn() {
	return leftColumn;
    }

    public Optional<TableColumn> getRightColumn() {
	return rightColumn;
    }

    public Optional<String> getLeftExpression() {
	return leftExpression;
    }

    public Optional<String> getRightExpression() {
	return rightExpression;
    }

    public boolean isNot() {
	return not;
    }

    public static class Builder {

	private ConditionType conditionType;
	private TableColumn leftColumn;
	private TableColumn rightColumn;
	private String leftExpression;
	private String rightExpression;
	private boolean not = false;

	public Builder(ConditionType conditionType) {
	    super();
	    this.conditionType = conditionType;
	}

	public Builder withLeftColumn(TableColumn tableColumn) {
	    this.leftColumn = tableColumn;
	    return this;
	}

	public Builder withRightColumn(TableColumn tableColumn) {
	    this.rightColumn = tableColumn;
	    return this;
	}

	public Builder withLeftExpression(String expr) {
	    this.leftExpression = expr;
	    return this;
	}

	public Builder withRightExpression(String expr) {
	    this.rightExpression = expr;
	    return this;
	}

	public Builder not() {
	    this.not = true;
	    return this;
	}

	public BinaryCondition build() {
	    BinaryCondition condition = new BinaryCondition();
	    condition.conditionType = conditionType;
	    condition.leftColumn = Optional.ofNullable(leftColumn);
	    condition.rightColumn = Optional.ofNullable(rightColumn);
	    condition.leftExpression = Optional.ofNullable(leftExpression);
	    condition.rightExpression = Optional.ofNullable(rightExpression);
	    condition.not = not;
	    return condition;
	}
    }

}
