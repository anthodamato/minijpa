package org.tinyjpa.jdbc;

import java.util.ArrayList;
import java.util.List;

public class EmbeddedIdAttributeValueConverter implements AttributeValueConverter {

	@Override
	public List<AttributeValue> convert(AttributeValue attrValue) throws Exception {
		List<AttributeValue> attrValues = new ArrayList<>();
		if (!attrValue.getAttribute().isEmbedded()) {
			attrValues.add(attrValue);
			return attrValues;
		}

		List<Attribute> attributes = attrValue.getAttribute().getEmbeddedAttributes();
		List<AttributeValue> attributeValues = new ArrayList<>();
		for (Attribute a : attributes) {
			Object value = a.getReadMethod().invoke(attrValue.getValue());
			attributeValues.add(new AttributeValue(a, value));
		}

		for (AttributeValue av : attributeValues) {
//				LOG.info("expandEmbedded: av.getAttribute().getName()=" + av.getAttribute().getName());
			List<AttributeValue> attrValueList = convert(av);
			attrValues.addAll(attrValueList);
		}

		return attrValues;
	}

	@Override
	public List<AttributeValue> convert(List<AttributeValue> attrValues) throws Exception {
		List<AttributeValue> values = new ArrayList<>();
		for (AttributeValue attrValue : attrValues) {
//			LOG.info("persist: attrValue.getAttribute().getName()=" + attrValue.getAttribute().getName());
//			LOG.info("persist: attrValue.getAttribute().isEmbedded()=" + attrValue.getAttribute().isEmbedded());
			values.addAll(convert(attrValue));
		}

		return values;
	}

}
