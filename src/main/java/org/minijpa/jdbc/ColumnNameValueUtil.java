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
package org.minijpa.jdbc;

import java.util.ArrayList;
import java.util.List;

public class ColumnNameValueUtil {

    public static List<ColumnNameValue> createRelationshipAttrsList(List<MetaAttribute> relationshipAttributes,
	    List<Object> relationshipValues) {
	List<ColumnNameValue> columnNameValues = new ArrayList<>();
	for (int i = 0; i < relationshipAttributes.size(); ++i) {
	    ColumnNameValue columnNameValue = new ColumnNameValue(relationshipAttributes.get(i).getName(),
		    relationshipValues.get(i), null, null, null, relationshipAttributes.get(i), null);
	    columnNameValues.add(columnNameValue);
	}

	return columnNameValues;
    }

}
