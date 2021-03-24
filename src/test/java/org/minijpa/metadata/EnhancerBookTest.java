package org.minijpa.metadata;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.commons.collections4.CollectionUtils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.metadata.enhancer.BytecodeEnhancerProvider;
import org.minijpa.metadata.enhancer.EnhAttribute;
import org.minijpa.metadata.enhancer.EnhEntity;

public class EnhancerBookTest {

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
    public void embeddedExample() throws Exception {
	String className = "org.minijpa.jpa.model.Book";
	EnhEntity enhEntity = BytecodeEnhancerProvider.getInstance().getBytecodeEnhancer().enhance(className);
	Assertions.assertNotNull(enhEntity);
	Assertions.assertNotNull(enhEntity.getClassName());

//	List<EnhEntity> embeddables = enhEntity.getEmbeddables();
	Set<EnhEntity> embeddables = new HashSet<>();
	enhEntity.findEmbeddables(embeddables);
	Assertions.assertNotNull(embeddables);
	Assertions.assertEquals(1, embeddables.size());
	EnhEntity embeddable = embeddables.iterator().next();
	Assertions.assertNotNull(embeddable);
	enhEntity = embeddable;
	List<EnhAttribute> enhAttributes = enhEntity.getEnhAttributes();
	Assertions.assertEquals(2, enhAttributes.size());
	List<String> names = enhAttributes.stream().map(a -> a.getName()).collect(Collectors.toList());
	Assertions.assertTrue(CollectionUtils.containsAll(Arrays.asList("format", "pages"), names));
    }
}
