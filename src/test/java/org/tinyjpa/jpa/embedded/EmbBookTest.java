package org.tinyjpa.jpa.embedded;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tinyjpa.jpa.PersistenceProviderImpl;
import org.tinyjpa.jpa.model.embedded.Book;
import org.tinyjpa.jpa.model.embedded.BookFormat;

/**
 * java -jar $DERBY_HOME/lib/derbyrun.jar server start
 * 
 * connect 'jdbc:derby://localhost:1527/test';
 * 
 * @author adamato
 *
 */
public class EmbBookTest {

	@Test
	public void persist() throws Exception {
		EntityManagerFactory emf = new PersistenceProviderImpl()
				.createEntityManagerFactory("/org/tinyjpa/jpa/embedded/persistence.xml", "emb_books", null);
		final EntityManager em = emf.createEntityManager();
		try {
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

		} finally {
			em.close();
			emf.close();
		}
	}

}
