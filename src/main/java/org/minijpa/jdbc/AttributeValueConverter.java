package org.minijpa.jdbc;

import java.util.List;

public interface AttributeValueConverter {
	public List<AttributeValue> convert(AttributeValue attrValue) throws Exception;

	public List<AttributeValue> convert(List<AttributeValue> attrValue) throws Exception;
}
