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

import java.util.List;

public class BinaryLogicConditionImpl implements BinaryLogicCondition {

    private ConditionType conditionType;
    private List<Condition> conditions;
    private boolean nested = false;

    public BinaryLogicConditionImpl(ConditionType conditionType, List<Condition> conditions) {
        super();
        this.conditionType = conditionType;
        this.conditions = conditions;
    }

    public BinaryLogicConditionImpl(ConditionType conditionType, List<Condition> conditions, boolean nested) {
        super();
        this.conditionType = conditionType;
        this.conditions = conditions;
        this.nested = nested;
    }

    @Override
    public ConditionType getConditionType() {
        return conditionType;
    }

    @Override
    public List<Condition> getConditions() {
        return conditions;
    }

    @Override
    public boolean nested() {
        return nested;
    }

    @Override
    public String toString() {
        return "BinaryLogicConditionImpl{" +
                "conditionType=" + conditionType +
                ", conditions=" + conditions +
                ", nested=" + nested +
                '}';
    }
}
