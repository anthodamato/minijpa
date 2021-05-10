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

import java.util.Date;
import java.util.Optional;
import org.minijpa.jdbc.DDLData;
import org.minijpa.jdbc.DefaultNameTranslator;
import org.minijpa.jdbc.JoinColumnAttribute;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.NameTranslator;
import org.minijpa.jdbc.PkGenerationType;
import org.minijpa.jdbc.PkStrategy;

public abstract class BasicDbJdbc implements DbJdbc {

    private final NameTranslator nameTranslator = new DefaultNameTranslator();

    @Override
    public NameTranslator getNameTranslator() {
	return nameTranslator;
    }

    @Override
    public PkStrategy findPkStrategy(PkGenerationType pkGenerationType) {
	if (pkGenerationType == null)
	    return PkStrategy.PLAIN;

	if (pkGenerationType == PkGenerationType.IDENTITY)
	    return PkStrategy.IDENTITY;

	if (pkGenerationType == PkGenerationType.SEQUENCE
		|| pkGenerationType == PkGenerationType.AUTO)
	    return PkStrategy.SEQUENCE;

	return PkStrategy.PLAIN;
    }

    public String buildColumnDefinition(Class<?> type, Optional<DDLData> ddlData) {
	if (type == Integer.class || (type.isPrimitive() && type.getName().equals("int")))
	    return "integer";

	if (type == Long.class || (type.isPrimitive() && type.getName().equals("long")))
	    return "bigint";

	if (type == Date.class || type == java.sql.Date.class)
	    return "date";

	return "";
    }

    @Override
    public String buildColumnDefinition(MetaAttribute metaAttribute) {
	return buildColumnDefinition(metaAttribute.getType(), metaAttribute.getDdlData());
    }

    @Override
    public String buildColumnDefinition(JoinColumnAttribute joinColumnAttribute) {
	return buildColumnDefinition(joinColumnAttribute.getType(), Optional.empty());
    }

}
