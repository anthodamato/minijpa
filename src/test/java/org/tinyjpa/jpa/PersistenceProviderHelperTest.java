package org.tinyjpa.jpa;

import java.util.List;

import javax.persistence.spi.PersistenceUnitInfo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PersistenceProviderHelperTest {
	@Test
	public void mutiplePU() throws Exception {
		PersistenceUnitInfo persistenceUnitInfo = new PersistenceProviderHelper()
				.parseXml("/org/tinyjpa/jpa/embedded/persistence.xml", "emb_booking");
		List<String> classNames = persistenceUnitInfo.getManagedClassNames();
		Assertions.assertFalse(classNames.isEmpty());
		Assertions.assertEquals(1, classNames.size());
	}
}
