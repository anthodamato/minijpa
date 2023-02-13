package org.minijpa.jpa.metamodel.generator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityParser {
    private Logger LOG = LoggerFactory.getLogger(EntityParser.class);

    public JpaEntity parse(String className, Collection<JpaEntity> parsedEntities) throws Exception {
        Optional<JpaEntity> optional = parsedEntities.stream().filter(e -> e.getClassName().equals(className))
                .findFirst();
        if (optional.isPresent())
            return optional.get();

        Class<?> c = Class.forName(className);

        LOG.debug("parse: class '{}' loaded", className);
        Entity ec = c.getAnnotation(Entity.class);
        if (ec == null)
            throw new Exception("@Entity annotation not found: '" + c.getName() + "'");

        String name = c.getSimpleName();
        if (ec.name() != null && !ec.name().trim().isEmpty())
            name = ec.name();

        Class<?> superClass = c.getSuperclass();
        Optional<JpaEntity> optionalMappedSuperclass = Optional.empty();
        if (superClass != null) {
            MappedSuperclass mappedSuperclass = superClass.getAnnotation(MappedSuperclass.class);
            if (mappedSuperclass != null) {
                optionalMappedSuperclass = Optional.of(parseMappedSuperclass(superClass.getName(), parsedEntities));
            }
        }

        List<JpaAttribute> attributes = parseAttributes(c);
        List<JpaEntity> embeddables = parseEmbeddables(c, parsedEntities);
        JpaEntity entity = new JpaEntity();
        entity.setClassName(className);
        entity.setName(name);
        entity.setType(c);
        attributes.forEach(entity::addAttribute);
        embeddables.forEach(entity::addEmbeddable);
        entity.setMappedSuperclass(optionalMappedSuperclass);
        return entity;
    }

    private List<JpaAttribute> parseAttributes(Class<?> entityClass) throws Exception {
        List<JpaAttribute> attributes = new ArrayList<>();
        Field[] fields = entityClass.getDeclaredFields();

        for (Field field : fields) {
            LOG.debug("readAttributes: parsing '{}' attribute", field.getName());
            Transient tr = field.getAnnotation(Transient.class);
            if (tr != null)
                continue;

            Embedded embedded = field.getAnnotation(Embedded.class);
            if (embedded != null)
                continue;

            JpaAttribute attribute = parseAttribute(field);
            attributes.add(attribute);
        }

        return attributes;
    }

    private JpaAttribute parseAttribute(Field field) throws Exception {
        Class<?> attributeClass = field.getType();
        Optional<JpaRelationship> relationship = buildRelationship(field);

        JpaAttribute attribute = new JpaAttribute();
        attribute.setName(field.getName());
        attribute.setType(attributeClass);
        attribute.setRelationship(relationship);
        return attribute;
    }

    private List<JpaEntity> parseEmbeddables(Class<?> entityClass, Collection<JpaEntity> parsedEntities)
            throws Exception {
        List<JpaEntity> metaEntities = new ArrayList<>();
        Field[] fields = entityClass.getDeclaredFields();

        for (Field field : fields) {
            LOG.debug("readAttributes: parsing '{}' attribute", field.getName());
            Transient tr = field.getAnnotation(Transient.class);
            if (tr != null)
                continue;

            Embedded embedded = field.getAnnotation(Embedded.class);
            if (embedded == null)
                continue;

            JpaEntity jpaEntity = parseEmbeddable(field, field.getType().getName(), parsedEntities, null);
            metaEntities.add(jpaEntity);
        }

        return metaEntities;
    }

    private JpaEntity parseEmbeddable(Field field, String entityClassName, Collection<JpaEntity> parsedEntities,
            Optional<String> parentPath) throws Exception {
        Optional<JpaEntity> optional = parsedEntities.stream().filter(e -> e.getClassName().equals(entityClassName))
                .findFirst();
        if (optional.isPresent())
            return optional.get();

        Class<?> c = Class.forName(entityClassName);
        Embeddable ec = c.getAnnotation(Embeddable.class);
        if (ec == null)
            throw new Exception("@Embeddable annotation not found: '" + c.getName() + "'");

        LOG.debug("Reading '" + entityClassName + "' attributes...");
        List<JpaAttribute> attributes = parseAttributes(c);
        List<JpaEntity> embeddables = parseEmbeddables(field.getType(), parsedEntities);

        JpaEntity entity = new JpaEntity();
        entity.setClassName(entityClassName);
        entity.setName(field.getName());
        entity.setType(c);
        attributes.forEach(entity::addAttribute);
        embeddables.forEach(entity::addEmbeddable);
        return entity;
    }

    private JpaEntity parseMappedSuperclass(String entityClassName, Collection<JpaEntity> parsedEntities)
            throws Exception {
        Class<?> c = Class.forName(entityClassName);
        MappedSuperclass ec = c.getAnnotation(MappedSuperclass.class);
        if (ec == null)
            throw new Exception("@MappedSuperclass annotation not found: '" + c.getName() + "'");

        LOG.debug("Reading mapped superclass '" + entityClassName + "' attributes...");
        List<JpaAttribute> attributes = parseAttributes(c);
        List<JpaEntity> embeddables = parseEmbeddables(c, parsedEntities);

        JpaEntity entity = new JpaEntity();
        entity.setClassName(entityClassName);
        entity.setType(c);
        attributes.forEach(entity::addAttribute);
        embeddables.forEach(entity::addEmbeddable);
        return entity;
    }

    private Optional<JpaRelationship> buildRelationship(Field field) {
        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
        ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
        int counter = 0;
        if (oneToOne != null)
            ++counter;
        if (manyToOne != null)
            ++counter;
        if (oneToMany != null)
            ++counter;
        if (manyToMany != null)
            ++counter;

        if (counter > 1)
            throw new IllegalArgumentException("More than one relationship annotations at '" + field.getName() + "'");

        if (oneToOne != null)
            return Optional.of(createOneToOne(oneToOne));

        if (manyToOne != null)
            return Optional.of(createManyToOne(manyToOne));

        if (oneToMany != null) {
            Class<?> collectionClass = null;
            Class<?> targetEntity = oneToMany.targetEntity();
            if (targetEntity == null || targetEntity == Void.TYPE)
                targetEntity = ReflectionUtil.findTargetEntity(field);

            return Optional.of(createOneToMany(oneToMany, collectionClass, targetEntity));
        }

        if (manyToMany != null) {
            Class<?> collectionClass = null;
            Class<?> targetEntity = manyToMany.targetEntity();
            if (targetEntity == null || targetEntity == Void.TYPE)
                targetEntity = ReflectionUtil.findTargetEntity(field);

            return Optional.of(createManyToMany(manyToMany, collectionClass, targetEntity));
        }

        return Optional.empty();
    }

    private OneToOneJpaRelationship createOneToOne(OneToOne oneToOne) {
        OneToOneJpaRelationship oneToOneJpaRelationship = new OneToOneJpaRelationship();
        oneToOneJpaRelationship.setMappedBy(getMappedBy(oneToOne));
        return oneToOneJpaRelationship;
    }

    private void finalizeRelationship(OneToOneJpaRelationship oneToOneJpaRelationship, JpaEntity entity,
            JpaEntity toEntity) {
        if (!oneToOneJpaRelationship.isOwner()) {
            oneToOneJpaRelationship.setOwningEntity(toEntity);
        }

        oneToOneJpaRelationship.setAttributeType(toEntity);
    }

    private OneToManyJpaRelationship createOneToMany(OneToMany oneToMany, Class<?> collectionClass,
            Class<?> targetEntity) {
        OneToManyJpaRelationship oneToManyJpaRelationship = new OneToManyJpaRelationship();
        oneToManyJpaRelationship.setMappedBy(getMappedBy(oneToMany));

        oneToManyJpaRelationship.setCollectionClass(collectionClass);
        oneToManyJpaRelationship.setTargetEntityClass(targetEntity);
        return oneToManyJpaRelationship;
    }

    private void finalizeRelationship(OneToManyJpaRelationship oneToManyJpaRelationship, JpaEntity entity,
            JpaEntity toEntity) {
        oneToManyJpaRelationship.setAttributeType(toEntity);
        if (!oneToManyJpaRelationship.isOwner()) {
            oneToManyJpaRelationship.setOwningEntity(toEntity);
        }
    }

    private ManyToOneJpaRelationship createManyToOne(ManyToOne manyToOne) {
        return new ManyToOneJpaRelationship();
    }

    private void finalizeRelationship(ManyToOneJpaRelationship manyToOneJpaRelationship, JpaEntity toEntity) {
        manyToOneJpaRelationship.setAttributeType(toEntity);
    }

    private ManyToManyJpaRelationship createManyToMany(ManyToMany manyToMany, Class<?> collectionClass,
            Class<?> targetEntity) {
        ManyToManyJpaRelationship manyToManyJpaRelationship = new ManyToManyJpaRelationship();
        manyToManyJpaRelationship.setMappedBy(getMappedBy(manyToMany));

        manyToManyJpaRelationship.setCollectionClass(collectionClass);
        manyToManyJpaRelationship.setTargetEntityClass(targetEntity);
        return manyToManyJpaRelationship;
    }

    private void finalizeRelationship(ManyToManyJpaRelationship manyToManyJpaRelationship, JpaEntity toEntity) {
        manyToManyJpaRelationship.setAttributeType(toEntity);
        if (!manyToManyJpaRelationship.isOwner()) {
            manyToManyJpaRelationship.setOwningEntity(toEntity);
        }
    }

    private void finalizeRelationships(JpaEntity entity, Map<String, JpaEntity> entities,
            List<JpaAttribute> attributes) {
        entity.getEmbeddables().forEach(embeddable -> {
            finalizeRelationships(embeddable, entities, embeddable.getAttributes());
        });

        for (JpaAttribute a : attributes) {
            Optional<JpaRelationship> opRelationship = a.getRelationship();
            if (opRelationship.isEmpty())
                continue;

            JpaRelationship relationship = opRelationship.get();
            if (relationship instanceof OneToOneJpaRelationship) {
                JpaEntity toEntity = entities.get(a.getType().getName());
                if (toEntity == null)
                    throw new IllegalArgumentException("One to One entity not found (" + a.getType().getName() + ")");

                OneToOneJpaRelationship oneToOne = (OneToOneJpaRelationship) relationship;
                finalizeRelationship(oneToOne, entity, toEntity);
            } else if (relationship instanceof ManyToOneJpaRelationship) {
                JpaEntity toEntity = entities.get(a.getType().getName());
                if (toEntity == null)
                    throw new IllegalArgumentException("Many to One entity not found (" + a.getType().getName() + ")");

                ManyToOneJpaRelationship manyToOne = (ManyToOneJpaRelationship) relationship;
                finalizeRelationship(manyToOne, toEntity);
            } else if (relationship instanceof OneToManyJpaRelationship) {
                JpaEntity toEntity = entities.get(relationship.getTargetEntityClass().getName());
                if (toEntity == null)
                    throw new IllegalArgumentException("One to Many target entity not found ("
                            + relationship.getTargetEntityClass().getName() + ")");

                OneToManyJpaRelationship oneToMany = (OneToManyJpaRelationship) relationship;
                finalizeRelationship(oneToMany, entity, toEntity);
            } else if (relationship instanceof ManyToManyJpaRelationship) {
                JpaEntity toEntity = entities.get(relationship.getTargetEntityClass().getName());
                if (toEntity == null)
                    throw new IllegalArgumentException("Many to Many target entity not found ("
                            + relationship.getTargetEntityClass().getName() + ")");

                ManyToManyJpaRelationship manyToMany = (ManyToManyJpaRelationship) relationship;
                finalizeRelationship(manyToMany, toEntity);
            }
        }
    }

    public void finalizeRelationships(Map<String, JpaEntity> entities) {
        for (Map.Entry<String, JpaEntity> entry : entities.entrySet()) {
            JpaEntity entity = entry.getValue();
            List<JpaAttribute> attributes = entity.getAttributes();
            finalizeRelationships(entity, entities, attributes);
        }
    }

    private Optional<String> evalMappedBy(String mappedBy) {
        if (mappedBy == null || mappedBy.isEmpty())
            return Optional.empty();

        return Optional.of(mappedBy);
    }

    private Optional<String> getMappedBy(OneToOne oneToOne) {
        return evalMappedBy(oneToOne.mappedBy());
    }

    private Optional<String> getMappedBy(OneToMany oneToMany) {
        return evalMappedBy(oneToMany.mappedBy());
    }

    private Optional<String> getMappedBy(ManyToMany manyToMany) {
        return evalMappedBy(manyToMany.mappedBy());
    }

}
