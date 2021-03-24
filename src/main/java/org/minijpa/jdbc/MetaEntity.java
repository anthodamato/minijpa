package org.minijpa.jdbc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MetaEntity {

    private Class<?> entityClass;
    private String name;
    private String tableName;
    private String alias;
    private MetaAttribute id;
    /**
     * Collection of simple, relationship and embeddable attributes.
     */
    private List<MetaAttribute> attributes;
    private final List<JoinColumnAttribute> joinColumnAttributes = new ArrayList<>();
    // used to build the metamodel. The 'attributes' field contains the
    // MappedSuperclass attributes
    private MetaEntity mappedSuperclassEntity;
    private Method modificationAttributeReadMethod;

    public MetaEntity(Class<?> entityClass, String name, String tableName, String alias, MetaAttribute id,
	    List<MetaAttribute> attributes, MetaEntity mappedSuperclassEntity, Method modificationAttributeReadMethod) {
	super();
	this.entityClass = entityClass;
	this.name = name;
	this.tableName = tableName;
	this.alias = alias;
	this.id = id;
	this.attributes = attributes;
	this.mappedSuperclassEntity = mappedSuperclassEntity;
	this.modificationAttributeReadMethod = modificationAttributeReadMethod;
    }

    public Class<?> getEntityClass() {
	return entityClass;
    }

    public String getName() {
	return name;
    }

    public String getTableName() {
	return tableName;
    }

    public String getAlias() {
	return alias;
    }

    public List<MetaAttribute> getAttributes() {
	return attributes;
    }

    public List<JoinColumnAttribute> getJoinColumnAttributes() {
	return joinColumnAttributes;
    }

    public MetaEntity getMappedSuperclassEntity() {
	return mappedSuperclassEntity;
    }

    public Method getModificationAttributeReadMethod() {
	return modificationAttributeReadMethod;
    }

    public MetaAttribute getAttribute(String name) {
	for (MetaAttribute attribute : attributes) {
	    if (attribute.getName().equals(name))
		return attribute;
	}

	if (id.getName().equals(name))
	    return id;

	return null;
    }

    public MetaAttribute getId() {
	return id;
    }

    public List<MetaAttribute> expandAttributes() {
	List<MetaAttribute> list = new ArrayList<>();
	for (MetaAttribute a : attributes) {
	    list.addAll(a.expand());
	}

	return list;
    }

    public List<MetaAttribute> expandAllAttributes() {
	List<MetaAttribute> list = new ArrayList<>();
	list.addAll(id.expand());
	for (MetaAttribute a : attributes) {
	    list.addAll(a.expand());
	}

	return list;
    }

    public MetaAttribute findAttributeByMappedBy(String mappedBy) {
	for (MetaAttribute attribute : attributes) {
	    if (attribute.getRelationship() != null && mappedBy.equals(attribute.getRelationship().getMappedBy()))
		return attribute;
	}

	return null;
    }

    public List<MetaAttribute> getRelationshipAttributes() {
	return attributes.stream().filter(a -> a.getRelationship() != null).collect(Collectors.toList());
    }

    public boolean isEmbeddedAttribute(String name) {
	Optional<MetaAttribute> optional = attributes.stream().filter(a -> a.getName().equals(name) && a.isEmbedded())
		.findFirst();
	return optional.isPresent();
    }

    @Override
    public String toString() {
	return super.toString() + " class: " + entityClass.getName() + "; tableName: " + tableName;
    }

    public List<MetaAttribute> notNullableAttributes() {
	return attributes.stream().filter(a -> !a.isNullable()).collect(Collectors.toList());
    }

    public void findEmbeddables(Set<MetaEntity> embeddables) {
	for (MetaAttribute enhAttribute : attributes) {
	    if (enhAttribute.isEmbedded()) {
		MetaEntity metaEntity = enhAttribute.getEmbeddableMetaEntity();
		embeddables.add(metaEntity);

		metaEntity.findEmbeddables(embeddables);
	    }
	}
    }

}
