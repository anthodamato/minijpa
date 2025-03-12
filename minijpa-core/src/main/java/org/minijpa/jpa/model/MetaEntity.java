/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jpa.model;

import org.minijpa.jpa.model.relationship.Cascade;
import org.minijpa.jpa.model.relationship.JoinColumnAttribute;
import org.minijpa.jpa.model.relationship.JoinColumnMapping;
import org.minijpa.jpa.model.relationship.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class MetaEntity {
    private final Logger log = LoggerFactory.getLogger(MetaEntity.class);

    private Class<?> entityClass;
    private String name;
    private String tableName;
    private Pk id;
    /**
     * Collection of basic and relationship attributes.
     */
    private List<AbstractMetaAttribute> attributes;
    /**
     * Basic attributes
     */
    private List<MetaAttribute> basicAttributes = List.of();
    private List<RelationshipMetaAttribute> relationshipAttributes = List.of();
    private List<MetaEntity> embeddables = List.of();
    private Method readMethod; // used for embeddables
    private Method writeMethod; // used for embeddables
    private String path; // used for embeddables
    private boolean embeddedId;
    private final List<JoinColumnMapping> joinColumnMappings = new ArrayList<>();
    // used to build the metamodel. The 'attributes' field contains the
    // MappedSuperclass attributes
    private MetaEntity mappedSuperclassEntity;
    private VersionMetaAttribute versionMetaAttribute;
    private Method modificationAttributeReadMethod;
    private Method lazyLoadedAttributeReadMethod;
    private Method lockTypeAttributeReadMethod = null;
    private Method lockTypeAttributeWriteMethod = null;
    private Method entityStatusAttributeReadMethod = null;
    private Method entityStatusAttributeWriteMethod = null;
    private Method joinColumnPostponedUpdateAttributeReadMethod = null;

    private MetaEntity() {
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

    public Pk getId() {
        return id;
    }

    public boolean isEmbeddedId() {
        return embeddedId;
    }

    public List<AbstractMetaAttribute> getAttributes() {
        return attributes;
    }

    public List<MetaAttribute> getBasicAttributes() {
        return basicAttributes;
    }

    public List<RelationshipMetaAttribute> getRelationshipAttributes() {
        return relationshipAttributes;
    }

    public List<MetaEntity> getEmbeddables() {
        return embeddables;
    }

    public Method getReadMethod() {
        return readMethod;
    }

    public Method getWriteMethod() {
        return writeMethod;
    }

    public String getPath() {
        return path;
    }

    public List<JoinColumnMapping> getJoinColumnMappings() {
        return joinColumnMappings;
    }

    public VersionMetaAttribute getVersionMetaAttribute() {
        return versionMetaAttribute;
    }

    public MetaEntity getMappedSuperclassEntity() {
        return mappedSuperclassEntity;
    }

    public Method getModificationAttributeReadMethod() {
        return modificationAttributeReadMethod;
    }

    public Method getLazyLoadedAttributeReadMethod() {
        return lazyLoadedAttributeReadMethod;
    }

    public Method getJoinColumnPostponedUpdateAttributeReadMethod() {
        return joinColumnPostponedUpdateAttributeReadMethod;
    }

    public Method getLockTypeAttributeReadMethod() {
        return lockTypeAttributeReadMethod;
    }

    public Method getLockTypeAttributeWriteMethod() {
        return lockTypeAttributeWriteMethod;
    }

    public Method getEntityStatusAttributeReadMethod() {
        return entityStatusAttributeReadMethod;
    }

    public Method getEntityStatusAttributeWriteMethod() {
        return entityStatusAttributeWriteMethod;
    }

    public AbstractMetaAttribute getAttribute(String name) {
        for (AbstractMetaAttribute attribute : attributes) {
            if (attribute.getName().equals(name)) {
                return attribute;
            }
        }

        return null;
    }

    public RelationshipMetaAttribute getRelationshipAttribute(String name) {
        for (RelationshipMetaAttribute attribute : relationshipAttributes) {
            if (attribute.getName().equals(name)) {
                return attribute;
            }
        }

        return null;
    }

    public Optional<MetaEntity> getEmbeddable(String name) {
        return embeddables.stream().filter(e -> e.name.equals(name)).findFirst();
    }

    public Object buildInstance() throws Exception {
        return getEntityClass().getDeclaredConstructor().newInstance();
    }

    public List<MetaAttribute> expandAllAttributes() {
        List<MetaAttribute> list = new ArrayList<>();
        if (id != null) {
            list.addAll(id.getAttributes());
        }

        list.addAll(basicAttributes);

        list.addAll(expandEmbeddables());

        return list;
    }


    public List<MetaAttribute> expandEmbeddables() {
        List<MetaAttribute> list = new ArrayList<>();
        embeddables.forEach(e -> {
            list.addAll(e.expandAllAttributes());
        });

        return list;
    }


    public List<JoinColumnAttribute> expandJoinColumnAttributes() {
        List<JoinColumnAttribute> jcas = new ArrayList<>();
        joinColumnMappings.forEach(joinColumnMapping -> {
            for (int i = 0; i < joinColumnMapping.size(); ++i) {
                jcas.add(joinColumnMapping.get(i));
            }
        });

        embeddables.forEach(metaEntity -> {
            jcas.addAll(metaEntity.expandJoinColumnAttributes());
        });

        return jcas;
    }

    public Optional<RelationshipMetaAttribute> findJoinColumnMappingAttribute(String attributeName) {
        Optional<JoinColumnMapping> o = joinColumnMappings.stream()
                .filter(j -> j.getAttribute().getName().equals(attributeName)).findFirst();
        if (o.isPresent()) {
            return Optional.of(o.get().getAttribute());
        }

        for (MetaEntity embeddable : embeddables) {
            Optional<RelationshipMetaAttribute> optional = embeddable.findJoinColumnMappingAttribute(
                    attributeName);
            if (optional.isPresent()) {
                return optional;
            }
        }

        return Optional.empty();
    }

    public List<JoinColumnMapping> expandJoinColumnMappings() {
        List<JoinColumnMapping> jcms = new ArrayList<>(joinColumnMappings);

        embeddables.forEach(metaEntity -> {
            jcms.addAll(metaEntity.expandJoinColumnMappings());
        });

        return jcms;
    }

    public List<RelationshipMetaAttribute> expandRelationshipAttributes() {
        List<RelationshipMetaAttribute> list = new ArrayList<>(relationshipAttributes);
        embeddables.forEach(e -> {
            list.addAll(e.expandRelationshipAttributes());
        });

        return list;
    }

    public RelationshipMetaAttribute findAttributeByMappedBy(String mappedBy) {
        for (RelationshipMetaAttribute attribute : relationshipAttributes) {
            if (mappedBy.equals(attribute.getRelationship().getMappedBy())) {
                return attribute;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return super.toString() + " class: " + entityClass.getName() + "; tableName: " + tableName;
    }

    public List<AbstractMetaAttribute> notNullableAttributes() {
        return attributes.stream().filter(a -> !a.isNullable()).collect(Collectors.toList());
    }

    private void findEmbeddables(Set<MetaEntity> embeddableSet) {
        for (MetaEntity metaEntity : embeddables) {
            embeddableSet.add(metaEntity);
            metaEntity.findEmbeddables(embeddableSet);
        }
    }

    public Set<MetaEntity> findEmbeddables() {
        Set<MetaEntity> embeddableSet = new HashSet<>();
        findEmbeddables(embeddableSet);
        return embeddableSet;
    }


    public List<RelationshipMetaAttribute> getCascadeAttributes(Cascade... cascades) {
        List<RelationshipMetaAttribute> attrs = new ArrayList<>();
        getRelationshipAttributes().forEach(attribute -> {
            Relationship r = attribute.getRelationship();
            if (r.isOwner() && r.hasAnyCascades(cascades)) {
                attrs.add(attribute);
            }
        });

        return attrs;
    }


    public AbstractMetaAttribute findAttributeFromPath(String path) {
        String[] ss = path.split("\\.");
        if (ss.length == 0) {
            return null;
        }

        if (ss.length == 1) {
            return getAttribute(path);
        }

        Optional<MetaEntity> optional = getEmbeddable(ss[0]);
        if (optional.isEmpty()) {
            return null;
        }

        // it's an embedded
        MetaEntity embeddable = optional.get();
        for (int i = 1; i < ss.length; ++i) {
            Optional<MetaEntity> opt = embeddable.getEmbeddable(ss[i]);
            if (opt.isPresent()) {
                embeddable = opt.get();
            } else {
                AbstractMetaAttribute attribute = embeddable.getAttribute(ss[i]);
                if (attribute == null) {
                    return null;
                }

                if (i == ss.length - 1) {
                    return attribute;
                }

                return null;
            }
        }

        return null;
    }


    public Object nextVersionValue(Object entityInstance) throws Exception {
        if (versionMetaAttribute == null)
            return null;

        Object currentValue = versionMetaAttribute.getReadMethod().invoke(entityInstance);
        return versionMetaAttribute.nextValue(currentValue);
    }

    public void clearModificationAttributes(Object parent)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = getModificationAttributeReadMethod();
        List list = (List) m.invoke(parent);
        list.clear();
    }


    public void removeModificationAttribute(
            Object parent,
            String attributeName)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = getModificationAttributeReadMethod();
        List list = (List) m.invoke(parent);
        list.remove(attributeName);
    }


    public Object getValue(Object parentInstance)
            throws IllegalAccessException, InvocationTargetException {
        return getReadMethod().invoke(parentInstance);
    }


    public void clearLazyAttributeLoaded(Object entityInstance)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (getLazyLoadedAttributeReadMethod() == null)
            return;

        Method m = getLazyLoadedAttributeReadMethod();
        List list = (List) m.invoke(entityInstance);
        list.clear();
    }


    public Object buildInstance(Object idValue) throws Exception {
        Object entityInstance = getEntityClass().getDeclaredConstructor().newInstance();
        log.debug("Building Entity Instance {}", entityInstance);
        log.debug("Building Entity Instance Id Value {}", idValue);
        log.debug("Building Entity Instance Id Value Class() {}", idValue.getClass());
        getId().writeValue(entityInstance, idValue);
        return entityInstance;
    }


    public static class Builder {

        private Class<?> entityClass;
        private String name;
        private String tableName;
        private Pk id;
        private boolean embeddedId;
        private List<AbstractMetaAttribute> attributes;
        private List<MetaAttribute> basicAttributes;
        private List<RelationshipMetaAttribute> relationshipAttributes;
        private List<MetaEntity> embeddables;
        private Method readMethod; // used for embeddables
        private Method writeMethod; // used for embeddables
        private String path; // used for embeddables
        private MetaEntity mappedSuperclassEntity;
        private VersionMetaAttribute versionMetaAttribute;
        private Method modificationAttributeReadMethod;
        private Method lazyLoadedAttributeReadMethod;
        private Method lockTypeAttributeReadMethod;
        private Method lockTypeAttributeWriteMethod;
        private Method entityStatusAttributeReadMethod;
        private Method entityStatusAttributeWriteMethod;
        private Method joinColumnPostponedUpdateAttributeReadMethod;

        public Builder withEntityClass(Class<?> entityClass) {
            this.entityClass = entityClass;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder withId(Pk id) {
            this.id = id;
            return this;
        }

        public Builder isEmbeddedId(boolean id) {
            this.embeddedId = id;
            return this;
        }

        public Builder withAttributes(List<AbstractMetaAttribute> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder withBasicAttributes(List<MetaAttribute> attributes) {
            this.basicAttributes = attributes;
            return this;
        }

        public Builder withRelationshipAttributes(List<RelationshipMetaAttribute> attributes) {
            this.relationshipAttributes = attributes;
            return this;
        }

        public Builder withEmbeddables(List<MetaEntity> embeddables) {
            this.embeddables = embeddables;
            return this;
        }

        public Builder withReadMethod(Method method) {
            this.readMethod = method;
            return this;
        }

        public Builder withWriteMethod(Method method) {
            this.writeMethod = method;
            return this;
        }

        public Builder withPath(String path) {
            this.path = path;
            return this;
        }

        public Builder withMappedSuperclassEntity(MetaEntity mappedSuperclassEntity) {
            this.mappedSuperclassEntity = mappedSuperclassEntity;
            return this;
        }

        public Builder withVersionMetaAttribute(VersionMetaAttribute versionMetaAttribute) {
            this.versionMetaAttribute = versionMetaAttribute;
            return this;
        }

        public Builder withModificationAttributeReadMethod(Method modificationAttributeReadMethod) {
            this.modificationAttributeReadMethod = modificationAttributeReadMethod;
            return this;
        }

        public Builder withLazyLoadedAttributeReadMethod(
                Method lazyLoadedAttributeReadMethod) {
            this.lazyLoadedAttributeReadMethod = lazyLoadedAttributeReadMethod;
            return this;
        }

        public Builder withJoinColumnPostponedUpdateAttributeReadMethod(
                Method joinColumnPostponedUpdateAttributeReadMethod) {
            this.joinColumnPostponedUpdateAttributeReadMethod = joinColumnPostponedUpdateAttributeReadMethod;
            return this;
        }

        public Builder withLockTypeAttributeReadMethod(Method lockTypeAttributeReadMethod) {
            this.lockTypeAttributeReadMethod = lockTypeAttributeReadMethod;
            return this;
        }

        public Builder withLockTypeAttributeWriteMethod(Method lockTypeAttributeWriteMethod) {
            this.lockTypeAttributeWriteMethod = lockTypeAttributeWriteMethod;
            return this;
        }

        public Builder withEntityStatusAttributeReadMethod(
                Method entityStatusAttributeReadMethod) {
            this.entityStatusAttributeReadMethod = entityStatusAttributeReadMethod;
            return this;
        }

        public Builder withEntityStatusAttributeWriteMethod(
                Method entityStatusAttributeWriteMethod) {
            this.entityStatusAttributeWriteMethod = entityStatusAttributeWriteMethod;
            return this;
        }

        public MetaEntity build() {
            MetaEntity metaEntity = new MetaEntity();
            metaEntity.entityClass = entityClass;
            metaEntity.name = name;
            metaEntity.tableName = tableName;
            metaEntity.id = id;
            metaEntity.embeddedId = embeddedId;
            metaEntity.attributes = attributes;
            metaEntity.basicAttributes = basicAttributes;
            metaEntity.relationshipAttributes = relationshipAttributes;
            metaEntity.embeddables = embeddables;
            metaEntity.readMethod = readMethod;
            metaEntity.writeMethod = writeMethod;
            metaEntity.path = path;
            metaEntity.mappedSuperclassEntity = mappedSuperclassEntity;
            metaEntity.versionMetaAttribute = versionMetaAttribute;
            metaEntity.modificationAttributeReadMethod = modificationAttributeReadMethod;
            metaEntity.lazyLoadedAttributeReadMethod = lazyLoadedAttributeReadMethod;
            metaEntity.joinColumnPostponedUpdateAttributeReadMethod = joinColumnPostponedUpdateAttributeReadMethod;
            metaEntity.lockTypeAttributeReadMethod = lockTypeAttributeReadMethod;
            metaEntity.lockTypeAttributeWriteMethod = lockTypeAttributeWriteMethod;
            metaEntity.entityStatusAttributeReadMethod = entityStatusAttributeReadMethod;
            metaEntity.entityStatusAttributeWriteMethod = entityStatusAttributeWriteMethod;
            return metaEntity;
        }
    }
}
