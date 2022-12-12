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
package org.minijpa.jpa.criteria;

import java.time.LocalDate;

public class CriteriaUtils {
    public static final String QM = "?";

    public static String buildValue(Object value) {
        StringBuilder sb = new StringBuilder();
        if (value instanceof String) {
            sb.append("'");
            sb.append((String) value);
            sb.append("'");
        } else
            sb.append(value.toString());

        return sb.toString();
    }

    public static boolean requireQM(Object value) {
        if (value instanceof LocalDate)
            return true;

        return false;
    }

}
