package org.minijpa.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.db.ApacheDerbyConfiguration;
import org.minijpa.jpa.db.DbConfiguration;
import org.minijpa.jpa.db.DbConfigurationList;
import org.minijpa.jpa.db.PersistenceUnitEnv;
import org.minijpa.jpa.model.AbstractMetaAttribute;
import org.minijpa.jpa.model.Book;
import org.minijpa.jpa.model.Citizen;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserTest {

  private Logger LOG = LoggerFactory.getLogger(ParserTest.class);
  private final Parser parser = new Parser(new ApacheDerbyConfiguration());

  @Test
  public void parse() throws Exception {
    String className = "org.minijpa.jpa.model.Citizen";
    List<MetaEntity> parsedEntities = new ArrayList<>();
    MetaEntity entity = MetaEntityUtils.parse(className, parser, parsedEntities);

    Assertions.assertNotNull(entity);
    Assertions.assertNotNull(entity.getEntityClass());
    Assertions.assertEquals(Citizen.class, entity.getEntityClass());
    Assertions.assertEquals("citizen", entity.getTableName());
    Assertions.assertNotNull(entity.getModificationAttributeReadMethod());
    Assertions.assertEquals(3, entity.getAttributes().size());
    AbstractMetaAttribute attribute = entity.getAttribute("name");
    Assertions.assertEquals("first_name", attribute.getColumnName());
    Assertions.assertEquals(String.class, attribute.getType());
    attribute = entity.getAttribute("lastName");
    Assertions.assertEquals("last_name", attribute.getColumnName());
    Assertions.assertEquals(String.class, attribute.getType());

    MetaAttribute versionAttribute = (MetaAttribute) entity.getAttribute("version");
    Assertions.assertTrue(versionAttribute.isVersion());
  }

  @Test
  public void embeddedExample() throws Exception {
    DbConfiguration dbConfiguration = new ApacheDerbyConfiguration();
    DbConfigurationList.getInstance().setDbConfiguration("emb_books", dbConfiguration);
    PersistenceUnitContext persistenceUnitContext = PersistenceUnitEnv.build("emb_books");

    String className = "org.minijpa.jpa.model.Book";
//	EnhEntity enhEntity = BytecodeEnhancerProvider.getInstance().getBytecodeEnhancer().enhance(className);
//	Assertions.assertNotNull(enhEntity);
//	Assertions.assertNotNull(enhEntity.getModificationAttributeGetMethod());
//	Assertions.assertTrue(enhEntity.getLockTypeAttributeGetMethod().isPresent());
//
//	List<MetaEntity> parsedEntities = new ArrayList<>();
//	MetaEntity entity = parser.parse(enhEntity, parsedEntities);
    MetaEntity entity = persistenceUnitContext.getEntity(className);

    Assertions.assertNotNull(entity);
    Assertions.assertNotNull(entity.getEntityClass());
    Assertions.assertEquals(Book.class, entity.getEntityClass());
    Assertions.assertNotNull(entity.getModificationAttributeReadMethod());

    Assertions.assertEquals(2, entity.getBasicAttributes().size());
    entity.getBasicAttributes()
        .forEach(a -> LOG.debug("embeddedExample: ba a.getName()=" + a.getName()));

    Assertions.assertEquals("id", entity.getId().getAttribute().getPath());
    Assertions.assertEquals("title", ((MetaAttribute) entity.getAttribute("title")).getPath());
    Assertions.assertEquals("bookFormat", entity.getEmbeddable("bookFormat").get().getPath());
    MetaEntity emb = entity.getEmbeddable("bookFormat").get();
    Assertions.assertEquals("bookFormat.format", emb.getAttribute("format").getPath());

    Set<MetaEntity> embeddables = entity.findEmbeddables();
    Assertions.assertNotNull(embeddables);
    Assertions.assertEquals(1, embeddables.size());

    entity = embeddables.iterator().next();
    Assertions.assertNotNull(entity.getModificationAttributeReadMethod());
    Assertions.assertEquals(2, entity.getAttributes().size());
    AbstractMetaAttribute attribute = entity.getAttribute("format");
    Assertions.assertEquals("format", attribute.getColumnName());
    Assertions.assertEquals(String.class, attribute.getType());
    attribute = entity.getAttribute("pages");
    Assertions.assertEquals("pages", attribute.getColumnName());
    Assertions.assertEquals(Integer.class, attribute.getType());
  }
}
