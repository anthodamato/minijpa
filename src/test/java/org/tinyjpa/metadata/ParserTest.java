package org.tinyjpa.metadata;

import java.beans.IntrospectionException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jpa.model.Citizen;

public class ParserTest {
	private Parser parser = new Parser();

	@Test
	public void parse() throws ClassNotFoundException, IntrospectionException {
		Entity entity = parser.parse("org.tinyjpa.jpa.model.Citizen");
		Assertions.assertEquals(Citizen.class, entity.getClazz());
		Assertions.assertEquals("citizen", entity.getTableName());
		Assertions.assertEquals(2, entity.getAttributes().size());
		Attribute attribute = entity.getAttribute("name");
		Assertions.assertEquals("first_name", attribute.getColumnName());
		Assertions.assertEquals(String.class, attribute.getType());
		attribute = entity.getAttribute("lastName");
		Assertions.assertEquals("last_name", attribute.getColumnName());
		Assertions.assertEquals(String.class, attribute.getType());
	}
}
