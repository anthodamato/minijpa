package org.tinyjpa.jpa.embedded;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.Bindable.BindableType;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.Type.PersistenceType;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tinyjpa.jpa.MetamodelUtils;
import org.tinyjpa.jpa.model.Book;
import org.tinyjpa.jpa.model.BookFormat;

/**
 * 
 * @author adamato
 *
 */
public class EmbBookTest {
//	private Logger LOG = LoggerFactory.getLogger(EmbBookTest.class);
	private static EntityManagerFactory emf;

	@BeforeAll
	public static void beforeAll() {
		emf = Persistence.createEntityManagerFactory("emb_books");
	}

	@AfterAll
	public static void afterAll() {
		emf.close();
	}

	@Test
	public void persist() throws Exception {
		final EntityManager em = emf.createEntityManager();
		final EntityTransaction tx = em.getTransaction();
		tx.begin();

		Book book = new Book();
		book.setTitle("The Interpretation of Dreams");
		book.setAuthor("Sigmund Freud");

		BookFormat bookFormat = new BookFormat();
		bookFormat.setFormat("paperback");
		bookFormat.setPages(688);

		book.setBookFormat(bookFormat);

		em.persist(book);

		System.out.println("EmbBookTest.persist: book.getId()=" + book.getId());

		Assertions.assertNotNull(book.getId());
		tx.commit();

		Book b = em.find(Book.class, book.getId());
		Assertions.assertTrue(b == book);
		Assertions.assertNotNull(b);
		BookFormat format = b.getBookFormat();
		Assertions.assertNotNull(format);
		Assertions.assertEquals(688, format.getPages());

		em.detach(book);
		b = em.find(Book.class, book.getId());
		Assertions.assertFalse(b == book);
		Assertions.assertNotNull(b);

		Book b2 = em.find(Book.class, b.getId());
		Assertions.assertTrue(b2 == b);

		em.close();
	}

	@Test
	public void metamodel() {
		final EntityManager em = emf.createEntityManager();
		Metamodel metamodel = em.getMetamodel();
		Assertions.assertNotNull(metamodel);

		Set<EntityType<?>> entityTypes = metamodel.getEntities();
		Assertions.assertEquals(1, entityTypes.size());

		List<String> names = entityTypes.stream().map(e -> e.getName()).collect(Collectors.toList());
		Assertions.assertTrue(CollectionUtils.containsAll(Arrays.asList("Book"), names));

		for (EntityType<?> entityType : entityTypes) {
			if (entityType.getName().equals("Book")) {
				checkBook(entityType);
			}
		}

		Set<ManagedType<?>> managedTypes = metamodel.getManagedTypes();
		Assertions.assertEquals(2, managedTypes.size());

		Set<EmbeddableType<?>> embeddableTypes = metamodel.getEmbeddables();
		Assertions.assertEquals(1, embeddableTypes.size());
		checkBookFormat(embeddableTypes.iterator().next());

		em.close();
	}

	private void checkBook(EntityType<?> entityType) {
		Assertions.assertEquals("Book", entityType.getName());
		MetamodelUtils.checkType(entityType, Book.class, PersistenceType.ENTITY);
		MetamodelUtils.checkType(entityType.getIdType(), Long.class, PersistenceType.BASIC);

		Assertions.assertEquals(BindableType.ENTITY_TYPE, entityType.getBindableType());
		Assertions.assertEquals(Book.class, entityType.getBindableJavaType());

		List<String> names = MetamodelUtils.getAttributeNames(entityType);
		Assertions.assertTrue(CollectionUtils.containsAll(Arrays.asList("id", "title", "author", "bookFormat"), names));

		MetamodelUtils.checkAttribute(entityType.getAttribute("title"), "title", String.class,
				PersistentAttributeType.BASIC, false, false);
		MetamodelUtils.checkAttribute(entityType.getAttribute("author"), "author", String.class,
				PersistentAttributeType.BASIC, false, false);
	}

	private void checkBookFormat(EmbeddableType<?> embeddableType) {
		MetamodelUtils.checkType(embeddableType, BookFormat.class, PersistenceType.EMBEDDABLE);

		List<String> names = MetamodelUtils.getAttributeNames(embeddableType);
		Assertions.assertEquals(2, names.size());
		Assertions.assertTrue(CollectionUtils.containsAll(Arrays.asList("format", "pages"), names));

//		List<Attribute<?, ?>> attributes = MetamodelUtils.getAttributes(embeddableType);

		MetamodelUtils.checkAttribute(embeddableType.getAttribute("format"), "format", String.class,
				PersistentAttributeType.BASIC, false, false);
		MetamodelUtils.checkAttribute(embeddableType.getAttribute("pages"), "pages", Integer.class,
				PersistentAttributeType.BASIC, false, false);
	}

}
