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
package org.minijpa.jpa.db;

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.relationship.JoinColumnAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AttributeUtil {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(AttributeUtil.class);


    public static int indexOfAttribute(
            ModelValueArray<FetchParameter> modelValueArray,
            MetaAttribute attribute) {
        LOG.debug("indexOfAttribute: attribute={}", attribute);
        for (int i = 0; i < modelValueArray.size(); ++i) {
            LOG.debug("indexOfAttribute: ((AttributeFetchParameter) modelValueArray.getModel(i)).getAttribute()={}", ((AttributeFetchParameter) modelValueArray.getModel(i)).getAttribute());
            if (((AttributeFetchParameter) modelValueArray.getModel(i)).getAttribute() == attribute) {
                return i;
            }
        }

        return -1;
    }


    public static int indexOfJoinColumnAttribute(
            List<JoinColumnAttribute> joinColumnAttributes,
            MetaAttribute a) {
        for (int i = 0; i < joinColumnAttributes.size(); ++i) {
            if (joinColumnAttributes.get(i).getForeignKeyAttribute() == a) {
                return i;
            }
        }

        return -1;
    }


    public static boolean isBasicAttribute(Class<?> c) {
        if (c == String.class) {
            return true;
        }

        if (c == Long.class) {
            return true;
        }

        if (c == BigInteger.class) {
            return true;
        }

        if (c == Boolean.class) {
            return true;
        }

        if (c == Character.class) {
            return true;
        }

        if (c == BigDecimal.class) {
            return true;
        }

        if (c == Double.class) {
            return true;
        }

        if (c == Float.class) {
            return true;
        }

        if (c == Integer.class) {
            return true;
        }

        if (c == Date.class) {
            return true;
        }

        if (c == LocalDate.class) {
            return true;
        }

        if (c == LocalDateTime.class) {
            return true;
        }

        if (c == OffsetDateTime.class) {
            return true;
        }

        if (c == OffsetTime.class) {
            return true;
        }

        if (c == ZonedDateTime.class) {
            return true;
        }

        if (c == Duration.class) {
            return true;
        }

        if (c == Instant.class) {
            return true;
        }

        if (c == LocalTime.class) {
            return true;
        }

        if (c == Calendar.class) {
            return true;
        }

        if (c == java.sql.Date.class) {
            return true;
        }

        if (c == Timestamp.class) {
            return true;
        }

        if (c == Time.class) {
            return true;
        }

        if (c.isEnum()) {
            return true;
        }

        if (c.isPrimitive()) {
            if (c.getName().equals("byte")) {
                return true;
            }

            if (c.getName().equals("short")) {
                return true;
            }

            if (c.getName().equals("int")) {
                return true;
            }

            if (c.getName().equals("long")) {
                return true;
            }

            if (c.getName().equals("float")) {
                return true;
            }

            if (c.getName().equals("double")) {
                return true;
            }

            if (c.getName().equals("boolean")) {
                return true;
            }

            if (c.getName().equals("char")) {
                return true;
            }
        }

        return false;
    }

}
