package org.minijpa.xml;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minijpa.metadata.PersistenceMetaData;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class PeristenceParserTest {
	@Test
	public void parse() throws ParserConfigurationException, SAXException, URISyntaxException, IOException {
		File file = Paths.get(getClass().getResource("/META-INF/persistence.xml").toURI()).toFile();
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser saxParser = spf.newSAXParser();

		XMLReader xmlReader = saxParser.getXMLReader();
		PersistenceParser persistenceParser = new PersistenceParser();
		xmlReader.setContentHandler(persistenceParser);
		xmlReader.parse(file.getAbsolutePath());
		PersistenceMetaData persistenceMetaData = persistenceParser.getPersistenceMetaData();
		Assertions.assertNotNull(persistenceMetaData);
		PersistenceUnitInfo persistenceUnitMetaData = persistenceMetaData.getPersistenceUnitMetaData("citizens");
		Assertions.assertNotNull(persistenceUnitMetaData);
		Assertions.assertTrue(persistenceUnitMetaData.getManagedClassNames().size() > 0);
	}
}
