/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.metadata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.minijpa.jdbc.MetaAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author adamato
 */
public class EntityModificationRepositoryImpl implements EntityModificationRepository {

    private final Logger LOG = LoggerFactory.getLogger(EntityModificationRepositoryImpl.class);
//    private final Set<Content> entityInstanceModifications = new HashSet<>();
    private final Map<Object, Map<String, Object>> entityInstanceModifications = new HashMap<>();
    private final Set<LazyAttributeContent> lazyAttributeContents = new HashSet<>();

    @Override
    public void save(Object owningEntityInstance, String attributeName, Object value) {
//	Optional<Content> optional = findModified(owningEntityInstance);
	Map<String, Object> attributeChanges = entityInstanceModifications.get(owningEntityInstance);
	if (attributeChanges == null) {
//	    Content content = new Content(owningEntityInstance);
//	    entityInstanceModifications.add(content);
	    attributeChanges = new HashMap<>();
	    entityInstanceModifications.put(owningEntityInstance, attributeChanges);
	    attributeChanges.put(attributeName, value);
	} else {
//	    Content content = optional.get();
//	    Map<String, Object> attributeChanges = optional
	    attributeChanges.put(attributeName, value);
	}
    }

    @Override
    public Optional<Map<String, Object>> get(Object entityInstance) {
	Map<String, Object> attributeChanges = entityInstanceModifications.get(entityInstance);
//	Optional<Content> optional = findModified(entityInstance);
	if (attributeChanges == null)
	    return Optional.empty();

	return Optional.of(attributeChanges);
    }

    @Override
    public void remove(Object entityInstance) {
//	Optional<Content> optional = findModified(entityInstance);
//	if (optional.isEmpty())
//	    return;

	entityInstanceModifications.remove(entityInstance);
    }

//    private Optional<Content> findModified(Object owningEntityInstance) {
//	return entityInstanceModifications.stream().filter(e -> e.owningEntityInstance == owningEntityInstance).findFirst();
//    }
    private Optional<LazyAttributeContent> findLazyContent(Object owningEntityInstance) {
	return lazyAttributeContents.stream().filter(e -> e.owningEntityInstance == owningEntityInstance).findFirst();
    }

    @Override
    public boolean isLazyAttributeLoaded(Object entityInstance, MetaAttribute a) {
	Optional<LazyAttributeContent> optional = findLazyContent(entityInstance);
	if (optional.isEmpty())
	    return false;

	LazyAttributeContent lazyAttributeContent = optional.get();
	return lazyAttributeContent.attributes.contains(a);
    }

    @Override
    public void setLazyAttributeLoaded(Object entityInstance, MetaAttribute a) {
	Optional<LazyAttributeContent> optional = findLazyContent(entityInstance);
	if (optional.isEmpty()) {
	    LazyAttributeContent lazyAttributeContent = new LazyAttributeContent(entityInstance);
	    lazyAttributeContent.attributes.add(a);
	    return;
	}

	LazyAttributeContent lazyAttributeContent = optional.get();
	lazyAttributeContent.attributes.add(a);
    }

    @Override
    public void removeLazyAttributeLoaded(Object entityInstance, MetaAttribute a) {
	Optional<LazyAttributeContent> optional = findLazyContent(entityInstance);
	if (optional.isEmpty())
	    return;

	LazyAttributeContent lazyAttributeContent = optional.get();
	lazyAttributeContent.attributes.remove(a);
	if (lazyAttributeContent.attributes.isEmpty())
	    lazyAttributeContents.remove(lazyAttributeContent);
    }

    @Override
    public void removeEntity(Object entityInstance) {
	Optional<LazyAttributeContent> optional = findLazyContent(entityInstance);
	if (optional.isEmpty())
	    return;

	lazyAttributeContents.remove(optional.get());
	remove(entityInstance);
    }

    private class Content {

	private Object owningEntityInstance;
	private Map<String, Object> attributeChanges = new HashMap<>();

	public Content(Object owningEntityInstance) {
	    this.owningEntityInstance = owningEntityInstance;
	}
    }

    private class LazyAttributeContent {

	private Object owningEntityInstance;
	private Set<MetaAttribute> attributes = new HashSet<>();

	public LazyAttributeContent(Object owningEntityInstance) {
	    this.owningEntityInstance = owningEntityInstance;
	}
    }
}
