package org.tinyjpa.jpa;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.metadata.PersistenceMetaData;
import org.tinyjpa.xml.PersistenceParser;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class PersistenceProviderHelper {
	private Logger LOG = LoggerFactory.getLogger(PersistenceProviderHelper.class);

	public PersistenceUnitInfo parseXml(String filePath, String persistenceUnitName)
			throws URISyntaxException, ParserConfigurationException, SAXException, IOException {
		File file = Paths.get(getClass().getResource(filePath).toURI()).toFile();
		SAXParserFactory spf = SAXParserFactory.newInstance();
//		spf.setNamespaceAware(true);
		SAXParser saxParser = spf.newSAXParser();

		XMLReader xmlReader = saxParser.getXMLReader();
		PersistenceParser persistenceParser = new PersistenceParser();
		xmlReader.setContentHandler(persistenceParser);
		xmlReader.parse(file.getAbsolutePath());
		PersistenceMetaData persistenceMetaData = persistenceParser.getPersistenceMetaData();
		if (persistenceMetaData == null) {
			LOG.error("'persistence' element not found, file path: " + filePath);
		}

		PersistenceUnitInfoImpl persistenceUnitMetaData = persistenceMetaData
				.getPersistenceUnitMetaData(persistenceUnitName);
		return persistenceUnitMetaData;
	}

}
