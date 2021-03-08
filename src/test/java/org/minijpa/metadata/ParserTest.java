package org.minijpa.metadata;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jpa.db.ApacheDerbyConfiguration;
import org.minijpa.jpa.model.Book;
import org.minijpa.jpa.model.Citizen;
import org.minijpa.metadata.enhancer.BytecodeEnhancerProvider;
import org.minijpa.metadata.enhancer.EnhEntity;

public class ParserTest {

    private Parser parser = new Parser(new ApacheDerbyConfiguration());

    @Test
    public void parse() throws Exception {
	String className = "org.minijpa.jpa.model.Citizen";
	MetaEntity entity = MetaEntityUtils.parse(className);

	Assertions.assertNotNull(entity);
	Assertions.assertNotNull(entity.getEntityClass());
	Assertions.assertEquals(Citizen.class, entity.getEntityClass());
	Assertions.assertEquals("citizen", entity.getTableName());
	Assertions.assertEquals(2, entity.getAttributes().size());
	MetaAttribute attribute = entity.getAttribute("name");
	Assertions.assertEquals("first_name", attribute.getColumnName());
	Assertions.assertEquals(String.class, attribute.getType());
	attribute = entity.getAttribute("lastName");
	Assertions.assertEquals("last_name", attribute.getColumnName());
	Assertions.assertEquals(String.class, attribute.getType());
    }

    @Test
    public void embeddedExample() throws Exception {
	String className = "org.minijpa.jpa.model.Book";
	EnhEntity enhEntity = BytecodeEnhancerProvider.getInstance().getBytecodeEnhancer().enhance(className);

	List<MetaEntity> parsedEntities = new ArrayList<>();
	MetaEntity entity = parser.parse(enhEntity, parsedEntities);
	Assertions.assertNotNull(entity);
	Assertions.assertNotNull(entity.getEntityClass());
	Assertions.assertEquals(Book.class, entity.getEntityClass());
	List<MetaEntity> embeddables = entity.getEmbeddables();
	Assertions.assertNotNull(embeddables);
	Assertions.assertEquals(1, embeddables.size());

	entity = embeddables.get(0);
	Assertions.assertEquals(2, entity.getAttributes().size());
	MetaAttribute attribute = entity.getAttribute("format");
	Assertions.assertEquals("format", attribute.getColumnName());
	Assertions.assertEquals(String.class, attribute.getType());
	attribute = entity.getAttribute("pages");
	Assertions.assertEquals("pages", attribute.getColumnName());
	Assertions.assertEquals(Integer.class, attribute.getType());
    }
}
