package org.tinyjpa.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jpa.PersistenceUnitInfoImpl;
import org.tinyjpa.metadata.PersistenceMetaData;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PersistenceParser extends DefaultHandler {
	private Logger LOG = LoggerFactory.getLogger(PersistenceParser.class);
	private PersistenceMetaData persistenceMetaData;
	private Map<String, PersistenceUnitInfoImpl> persistenceUnitMetaDatas = new HashMap<>();
	private Map<String, String> properties;
	private String persistentUnitName;
	private PersistenceUnitInfoImpl persistenceUnitMetaData;
	private List<String> managedClassNames = new ArrayList<>();
	private boolean startClass;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("persistence-unit")) {
			properties = new HashMap<>();
			Map<String, String> map = new HashMap<>();
			readPersistentUnitName(attributes, map);
			persistentUnitName = map.get("name");
		}

		if (qName.equals("property")) {
			readPropertyAttrs(attributes, properties);
		}

		if (qName.equals("class")) {
			startClass = true;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("persistence-unit")) {
			if (persistentUnitName == null || persistentUnitName.equals(""))
				LOG.warn("Persistent unit name not set");
			else {
				if (persistentUnitName != null && persistentUnitName.trim().length() > 0) {
					persistenceUnitMetaData = new PersistenceUnitInfoImpl(persistentUnitName,
							Collections.unmodifiableList(new ArrayList<>(managedClassNames)));
				}

				for (Map.Entry<String, String> entry : properties.entrySet()) {
					persistenceUnitMetaData.getProperties().setProperty(entry.getKey(), entry.getValue());
				}

				persistenceUnitMetaDatas.put(persistentUnitName, persistenceUnitMetaData);
			}

			properties = new HashMap<>();
			persistentUnitName = null;
			persistenceUnitMetaData = null;
			managedClassNames.clear();
		}

		if (qName.equals("persistence")) {
			persistenceMetaData = new PersistenceMetaData(Collections.unmodifiableMap(persistenceUnitMetaDatas));
		}

		if (qName.equals("class")) {
			startClass = false;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (startClass)
			managedClassNames.add(new String(ch, start, length));
	}

	private void readPropertyAttrs(Attributes attributes, Map<String, String> props) {
		int length = attributes.getLength();
		String name = null;
		String value = "";
		for (int i = 0; i < length; ++i) {
			String localName = attributes.getLocalName(i);
			if (localName.equals("name")) {
				name = attributes.getValue(i);
			} else if (localName.equals("value")) {
				value = attributes.getValue(i);
			}
		}

		if (name != null && name.length() > 0) {
			props.put(name, value);
		}
	}

	private void readPersistentUnitName(Attributes attributes, Map<String, String> props) {
		int length = attributes.getLength();
		String value = null;
		for (int i = 0; i < length; ++i) {
			String localName = attributes.getLocalName(i);
			if (localName.equals("name"))
				value = attributes.getValue(i);
		}

		if (value != null && value.length() > 0)
			props.put("name", value);
	}

	public PersistenceMetaData getPersistenceMetaData() {
		return persistenceMetaData;
	}

}
