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
