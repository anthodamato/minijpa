/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
public class EntityModificationCacheRepositoryImpl implements EntityModificationRepository {

    private final Logger LOG = LoggerFactory.getLogger(EntityModificationCacheRepositoryImpl.class);
    private final List<Content> entityInstanceModifications = new ArrayList<>();
    private final List<Content> entityModificationCache = new ArrayList<>();
    private final Set<LazyAttributeContent> lazyAttributeContents = new HashSet<>();

    @Override
    public void save(Object entityInstance, String attributeName, Object value) {
	Optional<Content> optionalCache = findContentInCache(entityInstance);
	if (optionalCache.isPresent()) {
	    Content c = optionalCache.get();
	    c.attributeValues.put(attributeName, value);
	    return;
	}

	Optional<Content> optional = findContent(entityInstance);
	if (optional.isEmpty()) {
	    Content c = new Content(entityInstance);
	    entityModificationCache.add(0, c);
	    c.attributeValues.put(attributeName, value);
	} else {
	    Content c = optional.get();
	    c.attributeValues.put(attributeName, value);
	    if (entityModificationCache.size() > 4) {
		entityInstanceModifications.addAll(entityModificationCache);
		entityModificationCache.clear();
	    }

	    entityModificationCache.add(0, c);
	    entityInstanceModifications.remove(c);
	}
    }

    @Override
    public Optional<Map<String, Object>> get(Object entityInstance) {
	Optional<Content> optionalCache = findContentInCache(entityInstance);
	if (optionalCache.isPresent())
	    return Optional.of(optionalCache.get().attributeValues);

	Optional<Content> optional = findContent(entityInstance);
	if (optional.isEmpty())
	    return Optional.empty();

	return Optional.of(optional.get().attributeValues);
    }

    @Override
    public void remove(Object entityInstance) {
	Optional<Content> optionalCache = findContentInCache(entityInstance);
	if (optionalCache.isPresent()) {
	    entityModificationCache.remove(optionalCache.get());
	    return;
	}

	Optional<Content> optional = findContent(entityInstance);
	if (optional.isEmpty())
	    return;

	entityInstanceModifications.remove(optional.get());
    }

    private Optional<LazyAttributeContent> findLazyContent(Object owningEntityInstance) {
	return lazyAttributeContents.stream().filter(e -> e.owningEntityInstance == owningEntityInstance).findFirst();
    }

    private Optional<Content> findContent(Object entityInstance) {
	return entityInstanceModifications.stream().filter(c -> c.entityInstance == entityInstance).findFirst();
    }

    private Optional<Content> findContentInCache(Object entityInstance) {
	return entityModificationCache.stream().filter(c -> c.entityInstance == entityInstance).findFirst();
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

	private Object entityInstance;
	private Map<String, Object> attributeValues = new HashMap<>();

	public Content(Object entityInstance) {
	    this.entityInstance = entityInstance;
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
