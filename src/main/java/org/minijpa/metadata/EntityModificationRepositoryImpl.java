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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author adamato
 */
public class EntityModificationRepositoryImpl implements EntityModificationRepository {

    private final Logger LOG = LoggerFactory.getLogger(EntityModificationRepositoryImpl.class);
//    private final List<Content> entityInstanceSet = new LinkedList<>();
    private final Set<Content> entityInstanceSet = new HashSet<>();

    @Override
    public void save(Object owningEntityInstance, String attributeName, Object value) {
//	int index = indexOf(owningEntityInstance);
//	if (index == -1) {
//	    Content content = new Content(owningEntityInstance);
//	    entityInstanceSet.add(content);
//	    content.attributeChanges.put(attributeName, value);
//	}

	Optional<Content> optional = find(owningEntityInstance);
	LOG.info("save: optional.isPresent()=" + optional.isPresent());
	if (optional.isEmpty()) {
	    Content content = new Content(owningEntityInstance);
	    entityInstanceSet.add(content);
	    content.attributeChanges.put(attributeName, value);
	} else {
	    Content content = optional.get();
	    content.attributeChanges.put(attributeName, value);
	}
    }

    @Override
    public Optional<Map<String, Object>> get(Object entityInstance) {
	Optional<Content> optional = find(entityInstance);
	if (optional.isEmpty())
//	int index = indexOf(entityInstance);
//	if (index == -1)
	    return Optional.empty();

	return Optional.of(optional.get().attributeChanges);
    }

    @Override
    public void remove(Object entityInstance) {
	Optional<Content> optional = find(entityInstance);
	if (optional.isEmpty())
	    return;

//	int index = indexOf(entityInstance);
//	if (index == -1)
//	    return;
	entityInstanceSet.remove(optional.get());
    }

//    private int indexOf(Object owningEntityInstance) {
//	for (int i = 0; i < entityInstanceSet.size(); ++i) {
//	    if (entityInstanceSet.get(i).owningEntityInstance == owningEntityInstance)
//		return i;
//	}
//
//	return -1;
//    }
    private Optional<Content> find(Object owningEntityInstance) {
	return entityInstanceSet.stream().filter(e -> e.owningEntityInstance == owningEntityInstance).findFirst();
    }

    private class Content {

	private Object owningEntityInstance;
	private Map<String, Object> attributeChanges = new HashMap<>();

	public Content(Object owningEntityInstance) {
	    this.owningEntityInstance = owningEntityInstance;
	}

    }
}
