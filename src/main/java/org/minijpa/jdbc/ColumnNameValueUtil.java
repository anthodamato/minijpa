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
