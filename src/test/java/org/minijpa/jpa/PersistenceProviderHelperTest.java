package org.minijpa.jpa;

import java.util.List;

import javax.persistence.spi.PersistenceUnitInfo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.PersistenceProviderHelper;

public class PersistenceProviderHelperTest {

    @Test
    public void multiplePU() throws Exception {
	PersistenceUnitInfo persistenceUnitInfo = new PersistenceProviderHelper()
		.parseXml("/META-INF/persistence.xml", "emb_booking", null);
	List<String> classNames = persistenceUnitInfo.getManagedClassNames();
	Assertions.assertFalse(classNames.isEmpty());
	Assertions.assertEquals(1, classNames.size());
    }
}
