package org.minijpa.metadata;

import java.util.ArrayList;
import java.util.List;

import org.minijpa.jdbc.AttributeValue;
import org.minijpa.jdbc.AttributeValueConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

}
