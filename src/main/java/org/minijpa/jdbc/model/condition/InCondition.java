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

import java.util.List;

import org.minijpa.jdbc.model.TableColumn;

public class InCondition implements Condition {

    private TableColumn leftColumn;
    private List<String> rightExpressions;
    private boolean not = false;

    public InCondition(TableColumn leftColumn, List<String> rightExpressions) {
	super();
	this.leftColumn = leftColumn;
	this.rightExpressions = rightExpressions;
    }

    public InCondition(TableColumn leftColumn, List<String> rightExpressions, boolean not) {
	super();
	this.leftColumn = leftColumn;
	this.rightExpressions = rightExpressions;
	this.not = not;
    }

    @Override
    public ConditionType getConditionType() {
	return ConditionType.IN;
    }

    public TableColumn getLeftColumn() {
	return leftColumn;
    }

    public List<String> getRightExpressions() {
	return rightExpressions;
    }

    public boolean isNot() {
	return not;
    }

}
