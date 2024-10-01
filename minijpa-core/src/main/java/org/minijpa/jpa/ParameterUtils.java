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
package org.minijpa.jpa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.persistence.Parameter;
import javax.persistence.Query;

/**
 * @author adamato
 */
public class ParameterUtils {

    public static Parameter<?> findParameterByName(
            String name,
            Map<Parameter<?>, Object> parameterValues) {
        for (Map.Entry<Parameter<?>, Object> entry : parameterValues.entrySet()) {
            MiniParameter<?> miniParameter = (MiniParameter<?>) entry.getKey();
            if (miniParameter.getName() != null && miniParameter.getName().equals(name))
                return miniParameter;
        }

        throw new IllegalArgumentException("Parameter '" + name + "' not found");
    }

    public static Object findParameterValueByName(
            String name,
            Map<Parameter<?>, Object> parameterValues) {
        return parameterValues.get(findParameterByName(name, parameterValues));
    }


    public static Parameter<?> findParameterByPosition(
            int position,
            Map<Parameter<?>, Object> parameterValues) {
        for (Map.Entry<Parameter<?>, Object> entry : parameterValues.entrySet()) {
            MiniParameter<?> miniParameter = (MiniParameter<?>) entry.getKey();
            if (miniParameter.getPosition() != null && miniParameter.getPosition() == position)
                return miniParameter;
        }

        throw new IllegalArgumentException("Parameter at position '" + position + "' not found");
    }


    public static Object findParameterValueByPosition(
            int position,
            Map<Parameter<?>, Object> parameterValues) {
        return parameterValues.get(findParameterByPosition(position, parameterValues));
    }


    private static List<QueryParameterData> findIndexParameters(
            String sqlString,
            String ph,
            Parameter<?> p) {
        int index = 0;
        List<QueryParameterData> indexParameters = new ArrayList<>();
        while (index != -1) {
            index = sqlString.indexOf(ph, index);
            if (index != -1) {
                QueryParameterData indexParameter = new QueryParameterData(index, p, ph);
                indexParameters.add(indexParameter);
                ++index;
            }
        }

        return indexParameters;
    }


    public static List<QueryParameterData> findIndexParameters(
            Set<Parameter<?>> parameters,
            String sqlString) {
        List<QueryParameterData> indexParameters = new ArrayList<>();
        for (Parameter<?> p : parameters) {
            if (p.getName() != null) {
                String s = ":" + p.getName();
                List<QueryParameterData> ips = findIndexParameters(sqlString, s, p);
                if (ips.isEmpty())
                    throw new IllegalArgumentException("Named parameter '" + p.getName() + "' not bound");

                indexParameters.addAll(ips);
            } else if (p.getPosition() != null) {
                String s = "?" + p.getPosition();
                List<QueryParameterData> ips = findIndexParameters(sqlString, s, p);
                if (ips.isEmpty())
                    throw new IllegalArgumentException("Parameter at position '" + p.getPosition() + "' not bound");

                indexParameters.addAll(ips);
            }
        }

        indexParameters.sort(Comparator.comparing(QueryParameterData::getIndex));
        return indexParameters;
    }


    public static String replaceParameterPlaceholders(
            String sqlString,
            List<QueryParameterData> indexParameters) {
        String sql = sqlString;
        for (QueryParameterData ip : indexParameters) {
            sql = sql.replace(ip.getPlaceholder(), "?");
        }

        return sql;
    }


    public static List<Object> sortParameterValues(
            Query query,
            List<QueryParameterData> indexParameters) {
        List<Object> values = new ArrayList<>();
        for (QueryParameterData ip : indexParameters) {
            values.add(query.getParameterValue(ip.getParameter()));
        }

        return values;
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
