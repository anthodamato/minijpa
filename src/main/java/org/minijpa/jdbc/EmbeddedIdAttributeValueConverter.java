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

public class EmbeddedIdAttributeValueConverter implements AttributeValueConverter {

    @Override
    public List<AttributeValue> convert(AttributeValue attrValue) throws Exception {
	List<AttributeValue> attrValues = new ArrayList<>();
	if (!attrValue.getAttribute().isEmbedded()) {
	    if (attrValue.getAttribute().getRelationship() != null)
		return attrValues;

	    attrValues.add(attrValue);
	    return attrValues;
	}

	List<MetaAttribute> attributes = attrValue.getAttribute().getEmbeddableMetaEntity().getAttributes();
	List<AttributeValue> attributeValues = new ArrayList<>();
	for (MetaAttribute a : attributes) {
	    Object value = a.getReadMethod().invoke(attrValue.getValue());
	    attributeValues.add(new AttributeValue(a, value));
	}

	for (AttributeValue av : attributeValues) {
	    List<AttributeValue> attrValueList = convert(av);
	    attrValues.addAll(attrValueList);
	}

	return attrValues;
    }

    @Override
    public List<AttributeValue> convert(List<AttributeValue> attrValues) throws Exception {
	List<AttributeValue> values = new ArrayList<>();
	for (AttributeValue attrValue : attrValues) {
	    values.addAll(convert(attrValue));
	}

	return values;
    }

    @Override
    public void convert(MetaAttribute attribute, Object value, AttributeValueArray attributeValueArray) throws Exception {
	if (!attribute.isEmbedded()) {
	    if (attribute.getRelationship() != null)
		return;

	    attributeValueArray.add(attribute, value);
	    return;
	}

	List<MetaAttribute> attributes = attribute.getEmbeddableMetaEntity().getAttributes();
	for (MetaAttribute a : attributes) {
	    Object v = a.getReadMethod().invoke(value);
	    convert(a, v, attributeValueArray);
	}
    }

}
