package org.minijpa.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import org.minijpa.jpa.PersistenceUnitInfoImpl;
import org.minijpa.metadata.PersistenceMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PersistenceParser extends DefaultHandler {
	private Logger LOG = LoggerFactory.getLogger(PersistenceParser.class);
	private PersistenceMetaData persistenceMetaData;
	private Map<String, PersistenceUnitInfo> persistenceUnitMetaDatas = new HashMap<>();
	private Map<String, String> properties;
	private String persistentUnitName;
	private PersistenceUnitInfo persistenceUnitMetaData;
	private List<String> managedClassNames = new ArrayList<>();
	private boolean startClass;
	private boolean startJtaDataSource;
	private String jtaDataSource;
	private boolean startNonJtaDataSource;
	private String nonJtaDataSource;

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

		if (qName.equals("class"))
			startClass = true;

		if (qName.equals("jta-data-source"))
			startJtaDataSource = true;

		if (qName.equals("non-jta-data-source"))
			startNonJtaDataSource = true;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("persistence-unit")) {
			if (persistentUnitName == null || persistentUnitName.equals(""))
				LOG.warn("Persistent unit name not set");
			else {
				if (persistentUnitName != null && persistentUnitName.trim().length() > 0) {
					DataSource jtaDs = null;
					if (jtaDataSource != null && !jtaDataSource.isEmpty()) {
						jtaDs = findDataSource(jtaDataSource);
					}

					DataSource nonJtaDs = null;
					if (nonJtaDataSource != null && !nonJtaDataSource.isEmpty()) {
						nonJtaDs = findDataSource(nonJtaDataSource);
					}

					persistenceUnitMetaData = new PersistenceUnitInfoImpl.Builder().withName(persistentUnitName)
							.withManagedClassNames(Collections.unmodifiableList(new ArrayList<>(managedClassNames)))
							.withJtaDataSource(jtaDs).withNonJtaDataSource(nonJtaDs).build();
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

		if (qName.equals("class"))
			startClass = false;

		if (qName.equals("jta-data-source"))
			startJtaDataSource = false;

		if (qName.equals("non-jta-data-source"))
			startNonJtaDataSource = false;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (startClass)
			managedClassNames.add(new String(ch, start, length));

		if (startJtaDataSource)
			jtaDataSource = new String(ch, start, length);

		if (startNonJtaDataSource)
			nonJtaDataSource = new String(ch, start, length);
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

	private DataSource findDataSource(String dss) throws SAXException {
		try {
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			DataSource ds = (DataSource) envCtx.lookup(dss);
			initCtx.close();
			envCtx.close();
			return ds;
		} catch (Exception e) {
			throw new SAXException(e);
		}
	}
}
