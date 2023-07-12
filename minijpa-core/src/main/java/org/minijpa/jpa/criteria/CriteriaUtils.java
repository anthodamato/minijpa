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

import org.minijpa.jpa.MiniParameter;
import org.minijpa.jpa.jpql.SemanticException;

import javax.persistence.Parameter;
import javax.persistence.PersistenceException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

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
        return value instanceof LocalDate;
    }

    public static Optional<Object> findParameterValue(
            Map<Parameter<?>, Object> map,
            String inputParameter) {
        if (inputParameter == null || inputParameter.trim().length() < 2)
            return Optional.empty();

        char c1 = inputParameter.charAt(0);
        if (c1 == '?') {
            int index = Integer.parseInt(inputParameter.substring(1));
            for (Map.Entry<Parameter<?>, Object> entry : map.entrySet()) {
                MiniParameter<?> miniParameter = (MiniParameter<?>) entry.getKey();
                if (miniParameter.getPosition() != null && miniParameter.getPosition() == index)
                    return Optional.of(entry.getValue());
            }

            return Optional.empty();
        }

        if (c1 == ':') {
            String paramName = inputParameter.substring(1);
            for (Map.Entry<Parameter<?>, Object> entry : map.entrySet()) {
                MiniParameter<?> miniParameter = (MiniParameter<?>) entry.getKey();
                if (miniParameter.getName() != null && miniParameter.getName().equals(paramName))
                    return Optional.of(entry.getValue());
            }

            return Optional.empty();
        }

        return Optional.empty();
    }
}
