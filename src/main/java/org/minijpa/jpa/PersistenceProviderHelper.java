package org.minijpa.jpa;

import java.io.File;
import java.nio.file.Paths;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.minijpa.metadata.PersistenceMetaData;
import org.minijpa.xml.PersistenceParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.XMLReader;

public class PersistenceProviderHelper {
	private Logger LOG = LoggerFactory.getLogger(PersistenceProviderHelper.class);

	public PersistenceUnitInfo parseXml(String filePath, String persistenceUnitName) throws Exception {
		File file = Paths.get(getClass().getResource(filePath).toURI()).toFile();
		if (file == null)
			throw new Exception("Persistence file '" + filePath + "' not found");

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

		PersistenceUnitInfo persistenceUnitInfoImpl = persistenceMetaData
				.getPersistenceUnitMetaData(persistenceUnitName);
		return persistenceUnitInfoImpl;
	}

}
