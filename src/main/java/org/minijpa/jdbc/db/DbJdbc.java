/*
 * Copyright (C) 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.minijpa.jdbc.db;

import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.NameTranslator;
import org.minijpa.jdbc.PkGenerationType;
import org.minijpa.jdbc.PkStrategy;

public interface DbJdbc {

    public PkStrategy findPkStrategy(PkGenerationType pkGenerationType);

    /**
     * Returns the statement to generate the next sequence value.
     *
     * @param entity the entity metamodel
     * @return the statement to generate the next sequence value
     */
    public String sequenceNextValueStatement(MetaEntity entity);

    public NameTranslator getNameTranslator();

    public default String notEqualOperator() {
	return "<>";
    }

    public default String equalOperator() {
	return "=";
    }

    public default String orOperator() {
	return "OR";
    }

    public default String andOperator() {
	return "AND";
    }

    public default String notOperator() {
	return "NOT";
    }

    public default String isNullOperator() {
	return "IS NULL";
    }

    public default String notNullOperator() {
	return "IS NOT NULL";
    }

    public default String trueOperator() {
	return "= TRUE";
    }

    public default String falseOperator() {
	return "= FALSE";
    }

    public default String emptyConjunctionOperator() {
	return "1=1";
    }

    public default String emptyDisjunctionOperator() {
	return "1=2";
    }

    public default String greaterThanOperator() {
	return ">";
    }

    public default String greaterThanOrEqualToOperator() {
	return ">=";
    }

    public default String lessThanOperator() {
	return "<";
    }

    public default String lessThanOrEqualToOperator() {
	return "<=";
    }

    public default String betweenOperator() {
	return "BETWEEN";
    }

    public default String likeOperator() {
	return "LIKE";
    }

    public default String inOperator() {
	return "in";
    }

}
