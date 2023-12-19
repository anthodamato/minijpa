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

public class LikeCondition extends BinaryCondition {

    private String escapeChar;

    public LikeCondition(Object left, Object right, String escapeChar) {
        super(ConditionType.LIKE, left, right);
        this.escapeChar = escapeChar;
    }

    public LikeCondition(Object left, Object right, String escapeChar, boolean not) {
        super(ConditionType.LIKE, left, right, not);
        this.escapeChar = escapeChar;
    }

    public String getEscapeChar() {
        return escapeChar;
    }

    @Override
    public String toString() {
        return super.toString() + "\n" +
                "LikeCondition{" +
                "escapeChar='" + escapeChar + '\'' +
                '}';
    }
}
