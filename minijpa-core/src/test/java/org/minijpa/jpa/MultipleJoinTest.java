package org.minijpa.jpa;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MultipleJoinTest {
    private final Logger log = LoggerFactory.getLogger(MultipleJoinTest.class);
    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() throws Exception {
        emf = Persistence.createEntityManagerFactory("multiple_joins", PersistenceUnitProperties.getProperties());
    }

    @AfterAll
    public static void afterAll() {
        emf.close();
    }

    @Test
    public void multipleJoins() throws Exception {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Artist artist = buildArtist();
        persistArtist(em, artist);
        tx.commit();

        tx.begin();
        em.detach(artist);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Artist> criteriaQuery = cb.createQuery(Artist.class);
        Root<Artist> root = criteriaQuery.from(Artist.class);
        root.fetch("movies");
        root.fetch("songs");
        criteriaQuery.distinct(true);
        TypedQuery<Artist> q = em.createQuery(criteriaQuery);
        List<Artist> artists = q.getResultList();

        assertNotNull(artists);
        assertEquals(1, artists.size());
        assertEquals(3, artists.get(0).getMovies().size());
        assertEquals(3, artists.get(0).getSongs().size());

        removeArtist(em, artists.get(0));
        tx.commit();

        em.close();
    }

    @Test
    public void multipleFetchJoinsSplitQuery() throws Exception {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Artist artist = buildArtist();
        persistArtist(em, artist);
        tx.commit();

        tx.begin();
        em.detach(artist);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Artist> criteriaQuery = cb.createQuery(Artist.class);
        Root<Artist> root = criteriaQuery.from(Artist.class);
        root.fetch("movies");
        Fetch<Artist, Song> song = root.fetch("songs");

        ParameterExpression<String> pName = cb.parameter(String.class);
        criteriaQuery.where(cb.notLike(((Path) song).get("name"), pName));

        criteriaQuery.distinct(true);
        TypedQuery<Artist> q = em.createQuery(criteriaQuery);
        q.setHint(QueryHints.SPLIT_MULTIPLE_JOINS, true);
        q.setParameter(pName, "%Falling%");
        List<Artist> artists = q.getResultList();
        assertNotNull(artists);
        assertEquals(1, artists.size());
        assertNotNull(artists.get(0).getSongs());
        assertNotNull(artists.get(0).getMovies());

        assertEquals(3, artists.get(0).getMovies().size());
        assertEquals(3, artists.get(0).getSongs().size());

        removeArtist(em, artists.get(0));
        tx.commit();

        em.close();
    }

    @Test
    public void jpqlMultipleJoins() throws Exception {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Artist artist = buildArtist();
        persistArtist(em, artist);
        tx.commit();

        tx.begin();
        em.detach(artist);
        Query q = em.createQuery("select distinct a from Artist a inner join fetch a.movies m inner join fetch a.songs s where s.name not like :name");
        q.setParameter("name", "%Falling%");
        List<Artist> artists = q.getResultList();
        assertNotNull(artists);
        assertEquals(1, artists.size());
        assertNotNull(artists.get(0).getSongs());
        assertNotNull(artists.get(0).getMovies());

        assertEquals(3, artists.get(0).getMovies().size());
        assertEquals(3, artists.get(0).getSongs().size());

        removeArtist(em, artists.get(0));
        tx.commit();

        em.close();
    }

    @Test
    public void jpqlMultipleJoinsSplitQuery() throws Exception {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Artist artist = buildArtist();
        persistArtist(em, artist);
        tx.commit();

        tx.begin();
        em.detach(artist);
        Query q = em.createQuery("select distinct a from Artist a inner join fetch a.movies m inner join fetch a.songs s where s.name not like :name");
        q.setParameter("name", "%Falling%");
        q.setHint(QueryHints.SPLIT_MULTIPLE_JOINS, true);
        List<Artist> artists = q.getResultList();
        assertNotNull(artists);
        assertEquals(1, artists.size());
        assertNotNull(artists.get(0).getSongs());
        assertNotNull(artists.get(0).getMovies());

        assertEquals(3, artists.get(0).getMovies().size());
        assertEquals(3, artists.get(0).getSongs().size());

        removeArtist(em, artists.get(0));
        tx.commit();

        em.close();
    }

    private Artist buildArtist() {
        Song song1 = new Song();
        song1.setName("That's All Right");

        Song song2 = new Song();
        song2.setName("Loving You");

        Song song3 = new Song();
        song3.setName("Blue Hawaii");

        Movie movie1 = new Movie();
        movie1.setName("Love Me Tender");

        Movie movie2 = new Movie();
        movie2.setName("Flaming Star");

        Movie movie3 = new Movie();
        movie3.setName("It Happened at the World's Fair");

        Artist artist = new Artist();
        artist.setName("Elvis Presley");
        artist.setMovies(List.of(movie1, movie2, movie3));
        artist.setSongs(List.of(song1, song2, song3));
        return artist;
    }

    private void removeArtist(EntityManager em, Artist artist) {
        em.remove(artist);
        artist.getMovies().forEach(em::remove);
        artist.getSongs().forEach(em::remove);
    }

    private void persistArtist(EntityManager em, Artist artist) {
        artist.getMovies().forEach(em::persist);
        artist.getSongs().forEach(em::persist);
        em.persist(artist);
    }
}
