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
package org.minijpa.sql.model.condition;

public class UnaryCondition implements Condition {

    private final ConditionType conditionType;
    private final Object operand;

    public UnaryCondition(ConditionType conditionType, Object operand) {
        super();
        this.conditionType = conditionType;
        this.operand = operand;
    }

    @Override
    public ConditionType getConditionType() {
        return conditionType;
    }

    public Object getOperand() {
        return operand;
    }

}
