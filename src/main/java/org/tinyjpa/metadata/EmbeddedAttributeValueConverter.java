package org.tinyjpa.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.AttributeValueConverter;

public class EmbeddedAttributeValueConverter implements AttributeValueConverter {
	private Logger LOG = LoggerFactory.getLogger(EmbeddedAttributeValueConverter.class);

	@Override
	public List<AttributeValue> convert(AttributeValue attrValue) throws Exception {
		List<AttributeValue> attrValues = new ArrayList<>();
		LOG.info("convert: getAttribute().getName()=" + attrValue.getAttribute().getName()
				+ "; getAttribute().isEmbedded()=" + attrValue.getAttribute().isEmbedded());
		if (!attrValue.getAttribute().isEmbedded()) {
			attrValues.add(attrValue);
			return attrValues;
		}

		Optional<List<AttributeValue>> optional = EntityDelegate.getInstance()
				.findEmbeddedAttrValues(attrValue.getValue());
//		LOG.info("expandEmbedded: optional.isPresent()=" + optional.isPresent());
		if (optional.isPresent()) {
			List<AttributeValue> list = optional.get();
			for (AttributeValue av : list) {
//				LOG.info("expandEmbedded: av.getAttribute().getName()=" + av.getAttribute().getName());
				List<AttributeValue> attrValueList = convert(av);
				attrValues.addAll(attrValueList);
			}
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
