package org.tinyjpa.jpa;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

public class PersistenceProviderHelperTest {
	@Test
	public void mutiplePU() throws URISyntaxException, ParserConfigurationException, SAXException, IOException {
		PersistenceUnitInfo persistenceUnitInfo = new PersistenceProviderHelper()
				.parseXml("/org/tinyjpa/jpa/embedded/persistence.xml", "emb_booking");
		List<String> classNames = persistenceUnitInfo.getManagedClassNames();
		Assertions.assertFalse(classNames.isEmpty());
		Assertions.assertEquals(1, classNames.size());
	}
}
