package org.tinyjpa.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jpa.model.Citizen;

public class ParserTest {
	private Parser parser = new Parser();

	@Test
	public void parse() throws Exception {
		List<String> classNames = new ArrayList<>();
		classNames.add("org.tinyjpa.jpa.model.Citizen");
		Map<String, MetaEntity> entities = parser.createMetaEntities(classNames);
		MetaEntity entity = entities.get("org.tinyjpa.jpa.model.Citizen");
		Assertions.assertNotNull(entity);
		Assertions.assertNotNull(entity.getClazz());
		Assertions.assertEquals(Citizen.class, entity.getClazz());
		Assertions.assertEquals("citizen", entity.getTableName());
		Assertions.assertEquals(2, entity.getAttributes().size());
		MetaAttribute attribute = entity.getAttribute("name");
		Assertions.assertEquals("first_name", attribute.getColumnName());
		Assertions.assertEquals(String.class, attribute.getType());
		attribute = entity.getAttribute("lastName");
		Assertions.assertEquals("last_name", attribute.getColumnName());
		Assertions.assertEquals(String.class, attribute.getType());
	}

}
