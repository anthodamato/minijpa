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

public class BinaryCondition implements Condition {

    private ConditionType conditionType;
    private Object left;
    private Object right;
    private boolean not = false;

    public BinaryCondition(ConditionType conditionType, Object left, Object right) {
        super();
        this.conditionType = conditionType;
        this.left = left;
        this.right = right;
    }

    public BinaryCondition(ConditionType conditionType, Object left, Object right, boolean not) {
        super();
        this.conditionType = conditionType;
        this.left = left;
        this.right = right;
        this.not = not;
    }

    @Override
    public ConditionType getConditionType() {
        return conditionType;
    }

    public Object getLeft() {
        return left;
    }

    public Object getRight() {
        return right;
    }

    public boolean isNot() {
        return not;
    }

    public static class Builder {
        private final ConditionType conditionType;
        private Object left;
        private Object right;
        private boolean not = false;

        public Builder(ConditionType conditionType) {
            super();
            this.conditionType = conditionType;
        }

        public Builder withLeft(Object left) {
            this.left = left;
            return this;
        }

        public Builder withRight(Object right) {
            this.right = right;
            return this;
        }

        public Builder not() {
            this.not = true;
            return this;
        }

        public BinaryCondition build() {
            BinaryCondition condition = new BinaryCondition(conditionType, left, right, not);
            condition.conditionType = conditionType;
            condition.left = left;
            condition.right = right;
            condition.not = not;
            return condition;
        }
    }

}
