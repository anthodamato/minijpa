package org.minijpa.jpa.embedded;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jpa.MetamodelUtils;
import org.minijpa.jpa.MiniEntityManager;
import org.minijpa.jpa.PersistenceUnitProperties;
import org.minijpa.jpa.model.Book;
import org.minijpa.jpa.model.BookFormat;
import org.minijpa.metadata.PersistenceUnitContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author adamato
 *
 */
public class EmbBookTest {

    private Logger LOG = LoggerFactory.getLogger(EmbBookTest.class);

    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() {
	emf = Persistence.createEntityManagerFactory("emb_books", PersistenceUnitProperties.getProperties());
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

	Book book = create1stFreudBook();
	em.persist(book);

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

	tx.begin();
	em.remove(b2);
	tx.commit();

	em.close();
    }

    @Test
    public void metamodel() {
	final EntityManager em = emf.createEntityManager();

	PersistenceUnitContext persistenceUnitContext = ((MiniEntityManager) em).getPersistenceUnitContext();
	Map<String, MetaEntity> map = persistenceUnitContext.getEntities();
	map.forEach((k, v) -> {
	    LOG.debug("metamodel: v.getName()=" + v.getName());
	    v.getBasicAttributes().forEach(a -> LOG.debug("metamodel: ba a.getName()=" + a.getName()));
	});

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
	names.forEach(n -> LOG.debug("checkBook: n=" + n));
	Assertions.assertEquals(4, names.size());
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

    private Book create1stFreudBook() {
	Book book = new Book();
	book.setTitle("The Interpretation of Dreams");
	book.setAuthor("Sigmund Freud");

	BookFormat bookFormat = new BookFormat();
	bookFormat.setFormat("paperback");
	bookFormat.setPages(688);

	book.setBookFormat(bookFormat);
	return book;
    }

    private Book create2ndFreudBook() {
	Book book = new Book();
	book.setTitle("The Ego and the Id");
	book.setAuthor("Sigmund Freud");

	BookFormat bookFormat = new BookFormat();
	bookFormat.setFormat("electronic");
	bookFormat.setPages(128);

	book.setBookFormat(bookFormat);
	return book;
    }

    private Book create1stJoyceBook() {
	Book book = new Book();
	book.setTitle("Ulysses");
	book.setAuthor("James Joyce");

	BookFormat bookFormat = new BookFormat();
	bookFormat.setFormat("paperback");
	bookFormat.setPages(1010);

	book.setBookFormat(bookFormat);
	return book;
    }

    private Book create1stLondonBook() {
	Book book = new Book();
	book.setTitle("The Call of the Wild");
	book.setAuthor("Jack London");

	BookFormat bookFormat = new BookFormat();
	bookFormat.setFormat("hardcover");
	bookFormat.setPages(58);

	book.setBookFormat(bookFormat);
	return book;
    }

    private Book create2ndLondonBook() {
	Book book = new Book();
	book.setTitle("South Sea Tales");
	book.setAuthor("Jack London");

	BookFormat bookFormat = new BookFormat();
	bookFormat.setFormat("electronic");
	bookFormat.setPages(215);

	book.setBookFormat(bookFormat);
	return book;
    }

    @Test
    public void distinct() throws Exception {
	final EntityManager em = emf.createEntityManager();
	final EntityTransaction tx = em.getTransaction();
	tx.begin();

	Book book1 = create1stFreudBook();
	em.persist(book1);
	Book book2 = create2ndFreudBook();
	em.persist(book2);
	Book book3 = create1stJoyceBook();
	em.persist(book3);
	Book book4 = create1stLondonBook();
	em.persist(book4);
	Book book5 = create2ndLondonBook();
	em.persist(book5);

	CriteriaQuery<String> query = em.getCriteriaBuilder().createQuery(String.class);
	Root<Book> root = query.from(Book.class);
	query.select(root.get("author")).distinct(true);
	TypedQuery<String> tq = em.createQuery(query);
	List<String> resultList = tq.getResultList();
	Assertions.assertEquals(3, resultList.size());
	System.out.println("distinct: resultList=" + resultList);
	Assertions.assertTrue(
		CollectionUtils.containsAll(Arrays.asList("Sigmund Freud", "James Joyce", "Jack London"), resultList));

	tx.commit();

	tx.begin();
	em.remove(book1);
	em.remove(book2);
	em.remove(book3);
	em.remove(book4);
	em.remove(book5);
	tx.commit();

	em.close();
    }

    @Test
    public void count() throws Exception {
	final EntityManager em = emf.createEntityManager();
	final EntityTransaction tx = em.getTransaction();
	tx.begin();

	Book book1 = create1stFreudBook();
	em.persist(book1);
	Book book2 = create2ndFreudBook();
	em.persist(book2);
	Book book3 = create1stJoyceBook();
	em.persist(book3);
	Book book4 = create1stLondonBook();
	em.persist(book4);
	Book book5 = create2ndLondonBook();
	em.persist(book5);
	tx.commit();

	CriteriaBuilder cb = em.getCriteriaBuilder();
	CriteriaQuery query = cb.createQuery();
	Root<Book> root = query.from(Book.class);
	query.select(cb.count(root));
	TypedQuery<?> typedQuery = em.createQuery(query);
	Object result = typedQuery.getSingleResult();
	if (result != null && result instanceof Long)
	    Assertions.assertEquals(5L, result);
	if (result != null && result instanceof Integer)
	    Assertions.assertEquals(5, result);

	query.select(cb.countDistinct(root.get("author")));
	typedQuery = em.createQuery(query);
	result = typedQuery.getSingleResult();
	if (result != null && result instanceof Long)
	    Assertions.assertEquals(3L, result);
	if (result != null && result instanceof Integer)
	    Assertions.assertEquals(3, result);

	tx.begin();
	em.remove(book1);
	em.remove(book2);
	em.remove(book3);
	em.remove(book4);
	em.remove(book5);
	tx.commit();

	em.close();
    }

    @Test
    public void equalInEmbedded() throws Exception {
	final EntityManager em = emf.createEntityManager();
	final EntityTransaction tx = em.getTransaction();
	tx.begin();

	Book book1 = create1stFreudBook();
	em.persist(book1);
	Book book2 = create2ndFreudBook();
	em.persist(book2);
	Book book3 = create1stJoyceBook();
	em.persist(book3);
	Book book4 = create1stLondonBook();
	em.persist(book4);
	Book book5 = create2ndLondonBook();
	em.persist(book5);

	CriteriaBuilder cb = em.getCriteriaBuilder();
	CriteriaQuery query = cb.createQuery();
	Root<Book> root = query.from(Book.class);
	query.select(root);
	query.where(cb.equal(root.get("bookFormat").get("format"), "hardcover"));
	TypedQuery<?> typedQuery = em.createQuery(query);
	List<?> result = typedQuery.getResultList();
	Assertions.assertEquals(1, result.size());

	query.where(cb.in(root.get("bookFormat").get("format")).value("hardcover").value("electronic"));
	typedQuery = em.createQuery(query);
	result = typedQuery.getResultList();
	Assertions.assertEquals(3, result.size());

	em.remove(book1);
	em.remove(book2);
	em.remove(book3);
	em.remove(book4);
	em.remove(book5);
	tx.commit();

	em.close();
    }

}
