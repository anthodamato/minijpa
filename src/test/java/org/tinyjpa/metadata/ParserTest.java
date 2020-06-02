package org.tinyjpa.metadata;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jpa.model.Citizen;

import javassist.CannotCompileException;
import javassist.NotFoundException;

public class ParserTest {
	private Parser parser = new Parser();

	@Test
	public void parse() throws ClassNotFoundException, IntrospectionException, InstantiationException,
			IllegalAccessException, NotFoundException, CannotCompileException {
		List<String> classNames = new ArrayList<>();
		classNames.add("org.tinyjpa.jpa.model.Citizen");
		EntityEnhancer entityEnhancer = new EntityEnhancer();
		List<EnhEntity> enhEntities = entityEnhancer.enhance(classNames);
		Assertions.assertNotNull(enhEntities);
		Assertions.assertEquals(1, enhEntities.size());
		Assertions.assertNotNull(enhEntities.get(0));
		EnhEntity enhEntity = enhEntities.get(0);
		Assertions.assertNotNull(enhEntity.getClassName());

		Entity entity = parser.parse(enhEntity);
		Assertions.assertNotNull(entity);
		Assertions.assertNotNull(entity.getClazz());
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
