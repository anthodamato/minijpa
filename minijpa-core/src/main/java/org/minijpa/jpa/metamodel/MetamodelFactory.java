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
package org.minijpa.jpa.metamodel;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.Bindable.BindableType;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MappedSuperclassType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type.PersistenceType;

import org.minijpa.jpa.model.AbstractMetaAttribute;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.Pk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetamodelFactory {

    private Logger LOG = LoggerFactory.getLogger(MetamodelFactory.class);

    private final Map<String, MetaEntity> entities;

    public MetamodelFactory(Map<String, MetaEntity> entities) {
        super();
        this.entities = entities;
    }

    public Metamodel build() throws Exception {
        Set<MappedSuperclassType<?>> mappedSuperclassTypes = buildMappedSuperclassTypes(entities);
        Set<EntityType<?>> entityTypes = buildEntityTypes(entities);
        LOG.debug("build: entityTypes={}", entityTypes);
        Set<EmbeddableType<?>> embeddableTypes = buildEmbeddableTypes(entities);

        Set<ManagedType<?>> managedTypes = new HashSet<>();
        managedTypes.addAll(mappedSuperclassTypes);
        managedTypes.addAll(embeddableTypes);
        managedTypes.addAll(entityTypes);

        return new MetamodelImpl(managedTypes, entityTypes, embeddableTypes);
    }

    private Set<EntityType<?>> buildEntityTypes(Map<String, MetaEntity> entities) throws Exception {
        Set<EntityType<?>> entityTypes = new HashSet<>();
        for (Map.Entry<String, MetaEntity> entry : entities.entrySet()) {
            entityTypes.add(buildEntityType(entry.getValue()));
        }

        return entityTypes;
    }

    private Field findField(Class<?> c, String fieldName) throws Exception {
        Field field;
        try {
            field = c.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> sc = c.getSuperclass();
            field = sc.getDeclaredField(fieldName);
        }

        return field;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private EntityType<?> buildEntityType(MetaEntity entity) throws Exception {
        Pk id = entity.getId();
        LOG.debug("buildEntityType: id={}", id);
        SingularAttribute<?, ?> idSingularAttribute = null;
        if (!id.isIdClass() && !id.isComposite()) {
            Field fieldId = findField(entity.getEntityClass(), id.getName());
            idSingularAttribute = new IdSingularAttribute(id.getName(), null, id.getType(), fieldId,
                    id.getType(), new MetamodelType(PersistenceType.BASIC, id.getType()));
        }

        Set<SingularAttribute> singularAttributes = buildSingularAttributes(entity);
        LOG.debug("buildEntityType: entity.getName()={}", entity.getName());
        singularAttributes.forEach(a -> LOG.debug("buildEntityType: a.getName()={}", a.getName()));
        entity.getBasicAttributes().forEach(a -> LOG.debug("buildEntityType: ba a.getName()={}", a.getName()));
        LOG.debug("buildEntityType: singularAttributes={}", singularAttributes);
        Set<Attribute> allAttributes = new HashSet<>();
        allAttributes.addAll(singularAttributes);
        if (!entity.getId().isComposite())
            allAttributes.add(idSingularAttribute);

        Set<SingularAttribute> esa = entity.getEmbeddables().stream().map(this::buildSingularAttributeFromEmbeddable)
                .collect(Collectors.toSet());
        allAttributes.addAll(esa);
        Set<Attribute> attributes = Collections.unmodifiableSet(allAttributes);
        LOG.debug("buildEntityType: attributes={}", attributes);

        MetamodelEntityType.Builder builder = new MetamodelEntityType.Builder()
                .withBindableType(BindableType.ENTITY_TYPE)
                .withJavaType(entity.getEntityClass())
                .withBindableJavaType(entity.getEntityClass())
                .withId(idSingularAttribute)
                .withSingularAttributes(singularAttributes)
                .withPersistenceType(PersistenceType.ENTITY)
                .withName(entity.getName())
                .withAttributes(attributes)
                .withSingleIdAttribute(!id.isIdClass());
        if (id.isIdClass()) {
            Set<SingularAttribute> singularIdAttributes = id.getAttributes().stream()
                    .map(this::buildSingularAttribute)
                    .collect(Collectors.toSet());
            LOG.debug("buildEntityType: singularIdAttributes={}", singularIdAttributes);
            builder.withIdClassAttributes(singularIdAttributes);
        }

        return builder.build();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private SingularAttribute buildSingularAttribute(MetaAttribute metaAttribute) {
        MetamodelSingularAttribute.Builder builder = new MetamodelSingularAttribute.Builder();
        MetamodelSingularAttribute metamodelSingularAttribute = builder.withName(metaAttribute.getName())
                .withBindableJavaType(metaAttribute.getType()).withBindableType(BindableType.SINGULAR_ATTRIBUTE)
                .withPersistentAttributeType(PersistentAttributeType.BASIC).withJavaType(metaAttribute.getType())
                .withType(new MetamodelType(PersistenceType.BASIC, metaAttribute.getType()))
                .withJavaMember(metaAttribute.getJavaMember()).build();
        return metamodelSingularAttribute;
    }

    private SingularAttribute buildSingularAttributeFromEmbeddable(MetaEntity metaEntity) {
        MetamodelSingularAttribute.Builder builder = new MetamodelSingularAttribute.Builder();
        MetamodelSingularAttribute metamodelSingularAttribute = builder.withName(metaEntity.getName())
                .withBindableJavaType(metaEntity.getEntityClass()).withBindableType(BindableType.SINGULAR_ATTRIBUTE)
                .withPersistentAttributeType(PersistentAttributeType.EMBEDDED).withJavaType(metaEntity.getEntityClass())
                .withType(new MetamodelType(PersistenceType.EMBEDDABLE, metaEntity.getEntityClass()))
                // .withJavaMember(metaEntity.getJavaMember())
                .build();
        return metamodelSingularAttribute;
    }

    @SuppressWarnings("rawtypes")
    private Set<SingularAttribute> buildSingularAttributes(MetaEntity entity) {
        Set<SingularAttribute> singularAttributes = entity.getBasicAttributes().stream()
                .map(a -> buildSingularAttribute(a)).collect(Collectors.toSet());
        return Collections.unmodifiableSet(singularAttributes);
    }

    private Set<EmbeddableType<?>> buildEmbeddableTypes(Map<String, MetaEntity> entities) throws Exception {
        Set<EmbeddableType<?>> embeddableTypes = new HashSet<>();
        Set<MetaEntity> incEmbeddables = new HashSet<>();
        for (Map.Entry<String, MetaEntity> entry : entities.entrySet()) {
            MetaEntity metaEntity = entry.getValue();
            Set<MetaEntity> embeddables = metaEntity.findEmbeddables();
            for (MetaEntity embeddable : embeddables) {
                if (!incEmbeddables.contains(embeddable)) {
                    embeddableTypes.add(buildEmbeddableType(embeddable));
                    incEmbeddables.add(embeddable);
                }
            }
        }

        return embeddableTypes;
    }

    private Set<EmbeddableType<?>> buildEmbeddableTypes(MetaEntity metaEntity) throws Exception {
        Set<EmbeddableType<?>> embeddableTypes = new HashSet<>();
        Set<MetaEntity> incEmbeddables = new HashSet<>();
        Set<MetaEntity> embeddables = metaEntity.findEmbeddables();
        for (MetaEntity embeddable : embeddables) {
            if (!incEmbeddables.contains(embeddable)) {
                embeddableTypes.add(buildEmbeddableType(embeddable));
                incEmbeddables.add(embeddable);
            }
        }

        return embeddableTypes;
    }

    @SuppressWarnings("rawtypes")
    private EmbeddableType<?> buildEmbeddableType(MetaEntity entity) throws Exception {
        Set<SingularAttribute> singularAttributes = buildSingularAttributes(entity);
        Set<Attribute> allAttributes = new HashSet<>();
        allAttributes.addAll(singularAttributes);
        Set<Attribute> attributes = Collections.unmodifiableSet(allAttributes);

        MetamodelEmbeddableType.Builder builder = new MetamodelEmbeddableType.Builder()
                .withJavaType(entity.getEntityClass()).withSingularAttributes(singularAttributes)
                .withPersistenceType(PersistenceType.EMBEDDABLE).withAttributes(attributes);
        return builder.build();
    }

    private Set<MappedSuperclassType<?>> buildMappedSuperclassTypes(Map<String, MetaEntity> entities) throws Exception {
        Set<MappedSuperclassType<?>> mappedSuperclassTypes = new HashSet<>();
        Set<MetaEntity> incMappedSuperclass = new HashSet<>();
        for (Map.Entry<String, MetaEntity> entry : entities.entrySet()) {
            MetaEntity metaEntity = entry.getValue();
            MetaEntity mappedSuperclass = metaEntity.getMappedSuperclassEntity();
            if (mappedSuperclass == null)
                continue;

            if (!incMappedSuperclass.contains(mappedSuperclass)) {
                mappedSuperclassTypes.add(buildMappedSuperclassType(mappedSuperclass));
                incMappedSuperclass.add(mappedSuperclass);
            }
        }

        return mappedSuperclassTypes;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private MappedSuperclassType<?> buildMappedSuperclassType(MetaEntity entity) throws Exception {
        Pk id = entity.getId();
        SingularAttribute<?, ?> idSingularAttribute = new IdSingularAttribute(id.getName(), null, id.getType(),
                entity.getEntityClass().getDeclaredField(id.getName()), id.getType(),
                new MetamodelType(PersistenceType.BASIC, id.getType()));

        Set<SingularAttribute> singularAttributes = buildSingularAttributes(entity);
        Set<Attribute> allAttributes = new HashSet<>();
        allAttributes.addAll(singularAttributes);
        Set<Attribute> attributes = Collections.unmodifiableSet(allAttributes);

        MetamodelMappedSuperclassType.Builder builder = new MetamodelMappedSuperclassType.Builder()
                .withJavaType(entity.getEntityClass()).withId(idSingularAttribute)
                .withSingularAttributes(singularAttributes).withPersistenceType(PersistenceType.MAPPED_SUPERCLASS)
                .withAttributes(attributes);
        return builder.build();
    }

}
