package org.minijpa.jpa.metamodel.generator;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EntityMetadataProviderTest {

    @Test
    public void citizen() throws IOException, Exception {
        EntityMetadataProvider entityMetadataProvider = new EntityMetadataProviderImpl();

        List<EntityMetadata> entityMetadataList = entityMetadataProvider
                .build(List.of("org.minijpa.jpa.metamodel.generator.Citizen"));
        EntityMetadata entityMetadata = entityMetadataList.get(0);

        Assertions.assertNotNull(entityMetadata);
        Assertions.assertEquals("Citizen_", entityMetadata.getClassName());
        Assertions.assertEquals("Citizen", entityMetadata.getEntityClassName());
        Assertions.assertEquals("org.minijpa.jpa.metamodel.generator", entityMetadata.getPackagePath());
        Assertions.assertEquals("org/minijpa/jpa/metamodel/generator/Citizen_.java", entityMetadata.getPath());
        Assertions.assertEquals(4, entityMetadata.getAttributeElements().size());

        Optional<AttributeElement> optionalId = entityMetadata.getAttributeElements().stream()
                .filter(a -> a.getName().equals("id")).findFirst();
        Assertions.assertTrue(optionalId.isPresent());
        AttributeElement attributeElement = optionalId.get();
        Assertions.assertEquals(Long.class, attributeElement.getType());
        Assertions.assertEquals(AttributeType.SINGULAR, attributeElement.getAttributeType());

        Optional<AttributeElement> optionalName = entityMetadata.getAttributeElements().stream()
                .filter(a -> a.getName().equals("name")).findFirst();
        Assertions.assertTrue(optionalName.isPresent());
        AttributeElement attributeElementName = optionalName.get();
        Assertions.assertEquals(String.class, attributeElementName.getType());
        Assertions.assertEquals(AttributeType.SINGULAR, attributeElementName.getAttributeType());
    }

    @Test
    public void employee() throws IOException, Exception {
        EntityMetadataProvider entityMetadataProvider = new EntityMetadataProviderImpl();
        List<EntityMetadata> entityMetadataList = entityMetadataProvider.build(List
                .of("org.minijpa.jpa.metamodel.generator.Department", "org.minijpa.jpa.metamodel.generator.Employee"));
        EntityMetadata entityMetadata = entityMetadataList.stream()
                .filter(e -> e.getEntityClassName().equals("Department")).findFirst().get();
        Assertions.assertNotNull(entityMetadata);
        Assertions.assertEquals("Department_", entityMetadata.getClassName());
        Assertions.assertEquals("Department", entityMetadata.getEntityClassName());
        Assertions.assertEquals("org.minijpa.jpa.metamodel.generator", entityMetadata.getPackagePath());
        Assertions.assertEquals("org/minijpa/jpa/metamodel/generator/Department_.java", entityMetadata.getPath());
        Assertions.assertEquals(3, entityMetadata.getAttributeElements().size());

        Optional<AttributeElement> optionalId = entityMetadata.getAttributeElements().stream()
                .filter(a -> a.getName().equals("id")).findFirst();
        Assertions.assertTrue(optionalId.isPresent());
        AttributeElement attributeElement = optionalId.get();
        Assertions.assertEquals(Long.class, attributeElement.getType());
        Assertions.assertEquals(AttributeType.SINGULAR, attributeElement.getAttributeType());

        Optional<AttributeElement> optionalName = entityMetadata.getAttributeElements().stream()
                .filter(a -> a.getName().equals("name")).findFirst();
        Assertions.assertTrue(optionalName.isPresent());
        AttributeElement attributeElementName = optionalName.get();
        Assertions.assertEquals(String.class, attributeElementName.getType());
        Assertions.assertEquals(AttributeType.SINGULAR, attributeElementName.getAttributeType());

        Optional<AttributeElement> optionalEmployees = entityMetadata.getAttributeElements().stream()
                .filter(a -> a.getName().equals("employees")).findFirst();
        Assertions.assertTrue(optionalEmployees.isPresent());
        AttributeElement attributeElementEmployees = optionalEmployees.get();
        Assertions.assertEquals(Employee.class, attributeElementEmployees.getType());
        Assertions.assertEquals(AttributeType.COLLECTION, attributeElementEmployees.getAttributeType());
    }

    @Test
    public void embBooks() throws IOException, Exception {
        EntityMetadataProvider entityMetadataProvider = new EntityMetadataProviderImpl();
        List<EntityMetadata> entityMetadataList = entityMetadataProvider
                .build(List.of("org.minijpa.jpa.metamodel.generator.Book"));
        EntityMetadata entityMetadata = entityMetadataList.stream().filter(e -> e.getEntityClassName().equals("Book"))
                .findFirst().get();
        Assertions.assertNotNull(entityMetadata);
        Assertions.assertEquals("Book_", entityMetadata.getClassName());
        Assertions.assertEquals("Book", entityMetadata.getEntityClassName());
        Assertions.assertEquals("org.minijpa.jpa.metamodel.generator", entityMetadata.getPackagePath());
        Assertions.assertEquals("org/minijpa/jpa/metamodel/generator/Book_.java", entityMetadata.getPath());
        Assertions.assertEquals(4, entityMetadata.getAttributeElements().size());

        Optional<AttributeElement> optionalId = entityMetadata.getAttributeElements().stream()
                .filter(a -> a.getName().equals("id")).findFirst();
        Assertions.assertTrue(optionalId.isPresent());
        AttributeElement attributeElement = optionalId.get();
        Assertions.assertEquals(Long.class, attributeElement.getType());
        Assertions.assertEquals(AttributeType.SINGULAR, attributeElement.getAttributeType());

        Optional<AttributeElement> optionalTitle = entityMetadata.getAttributeElements().stream()
                .filter(a -> a.getName().equals("title")).findFirst();
        Assertions.assertTrue(optionalTitle.isPresent());
        AttributeElement attributeElementTitle = optionalTitle.get();
        Assertions.assertEquals(String.class, attributeElementTitle.getType());
        Assertions.assertEquals(AttributeType.SINGULAR, attributeElementTitle.getAttributeType());

        Optional<AttributeElement> optionalAuthor = entityMetadata.getAttributeElements().stream()
                .filter(a -> a.getName().equals("author")).findFirst();
        Assertions.assertTrue(optionalAuthor.isPresent());
        AttributeElement attributeElementAuthor = optionalAuthor.get();
        Assertions.assertEquals(String.class, attributeElementAuthor.getType());
        Assertions.assertEquals(AttributeType.SINGULAR, attributeElementAuthor.getAttributeType());

        Optional<AttributeElement> optionalBookFormat = entityMetadata.getAttributeElements().stream()
                .filter(a -> a.getName().equals("bookFormat")).findFirst();
        Assertions.assertTrue(optionalBookFormat.isPresent());
        AttributeElement attributeElementBookFormat = optionalBookFormat.get();
        Assertions.assertEquals(BookFormat.class, attributeElementBookFormat.getType());
        Assertions.assertEquals(AttributeType.SINGULAR, attributeElementBookFormat.getAttributeType());
    }

}
