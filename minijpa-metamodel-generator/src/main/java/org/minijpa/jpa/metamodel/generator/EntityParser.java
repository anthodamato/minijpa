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
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
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
        Entity ec = c.getAnnotation(Entity.class);
        if (ec == null)
            throw new Exception("@Entity annotation not found: '" + c.getName() + "'");

        String name = c.getSimpleName();
        if (ec.name() != null && !ec.name().trim().isEmpty())
            name = ec.name();

        List<JpaAttribute> attributes = readAttributes(c);
        List<JpaEntity> embeddables = parseEmbeddables(c, parsedEntities);
        JpaEntity entity = new JpaEntity();
        entity.setClassName(className);
        entity.setName(name);
        entity.setType(c);
        attributes.forEach(entity::addAttribute);
        embeddables.forEach(entity::addEmbeddable);
        return entity;
    }

    private List<JpaAttribute> readAttributes(Class<?> entityClass) throws Exception {
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

            JpaAttribute attribute = readAttribute(field);
            attributes.add(attribute);
        }

        return attributes;
    }

    private JpaAttribute readAttribute(Field field) throws Exception {
//        String columnName = enhAttribute.getName();
        Class<?> attributeClass = field.getType();

//        LOG.debug("readAttribute: attributeClass={}", attributeClass);
//        Method readMethod = c.getMethod(enhAttribute.getGetMethod());
//        Method writeMethod = c.getMethod(enhAttribute.getSetMethod(), attributeClass);
//        Column column = field.getAnnotation(Column.class);

        Id idAnnotation = field.getAnnotation(Id.class);
//        boolean nullableColumn = !attributeClass.isPrimitive() && !enhAttribute.isParentEmbeddedId()
//                && (idAnnotation == null);
//        Optional<DDLData> ddlData;
//        if (column != null) {
//            String cn = column.name();
//            if (cn != null && cn.trim().length() > 0)
//                columnName = cn;
//
//            ddlData = buildDDLData(column, nullableColumn);
//        } else
//            ddlData = Optional.of(new DDLData(Optional.empty(), Optional.of(255), Optional.of(0), Optional.of(0),
//                    Optional.of(nullableColumn)));

//        Enumerated enumerated = field.getAnnotation(Enumerated.class);
////        LOG.debug("readAttribute: enumerated={}", enumerated);
//        Optional<Class<?>> enumerationType = enumerationType(attributeClass, enumerated);
////        LOG.debug("readAttribute: dbConfiguration={}", dbConfiguration);
//        Class<?> readWriteType = findDatabaseType(field, attributeClass, enumerationType);
////        LOG.debug("readAttribute: readWriteType={}", readWriteType);
//        Integer sqlType = findSqlType(readWriteType, enumerated);
//        LOG.debug("readAttribute: sqlType={}", sqlType);
//        LOG.debug("readAttribute: parentPath.isEmpty() ={}", parentPath.isEmpty());
//        String path = parentPath.isEmpty() ? enhAttribute.getName() : parentPath.get() + "." + enhAttribute.getName();
//        LOG.debug("readAttribute: idAnnotation={}", idAnnotation);
//        if (idAnnotation != null) {
//            AttributeMapper<?, ?> attributeMapper = dbConfiguration.getDbTypeMapper().attributeMapper(attributeClass,
//                    readWriteType);
//            LOG.debug("readAttribute: id attributeMapper={}", attributeMapper);
//            Optional<AttributeMapper> optionalAM = Optional.empty();
//            if (attributeMapper != null)
//                optionalAM = Optional.of(attributeMapper);
//
//            MetaAttribute.Builder builder = new MetaAttribute.Builder(enhAttribute.getName()).withColumnName(columnName)
//                    .withType(attributeClass).withReadWriteDbType(readWriteType).withReadMethod(readMethod)
//                    .withWriteMethod(writeMethod).isId(true).withSqlType(sqlType).withJavaMember(field).isBasic(true)
//                    .withPath(path).withDDLData(ddlData).withAttributeMapper(optionalAM);
//
//            return builder.build();
//        }

//        boolean isCollection = CollectionUtils.isCollectionClass(attributeClass);
//        Class<?> collectionImplementationClass = null;
//        if (isCollection)
//            collectionImplementationClass = CollectionUtils.findCollectionImplementationClass(attributeClass);

//        boolean version = field.getAnnotation(Version.class) != null;
//
//        MetaAttribute.Builder builder = new MetaAttribute.Builder(enhAttribute.getName()).withColumnName(columnName)
//                .withType(attributeClass).withReadWriteDbType(readWriteType).withReadMethod(readMethod)
//                .withWriteMethod(writeMethod).withSqlType(sqlType).isCollection(isCollection).withJavaMember(field)
//                .withCollectionImplementationClass(collectionImplementationClass).isVersion(version)
//                .isBasic(AttributeUtil.isBasicAttribute(attributeClass)).withPath(path).withDDLData(ddlData);
//
//        AttributeMapper<?, ?> attributeMapper = dbConfiguration.getDbTypeMapper().attributeMapper(attributeClass,
//                readWriteType);
//        if (attributeMapper != null)
//            builder.withAttributeMapper(Optional.of(attributeMapper));

        Optional<JpaRelationship> relationship = buildRelationship(field);

//        // Basic annotation
//        Basic basic = field.getAnnotation(Basic.class);
//        if (basic != null) {
//            if (!basic.optional())
//                builder.isNullable(false);
//        }

//        MetaAttribute attribute = builder.build();
//        LOG.debug("readAttribute: attribute: " + attribute);
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
//        String path = parentPath.isEmpty() ? enhAttribute.getName() : parentPath.get() + "." + enhAttribute.getName();
        List<JpaAttribute> attributes = readAttributes(c);
        List<JpaEntity> embeddables = parseEmbeddables(field.getType(), parsedEntities);

//        Class<?> attributeClass = Class.forName(enhAttribute.getClassName());
//        Class<?> parentClass = Class.forName(parentClassName);
//
//        Field field = parentClass.getDeclaredField(attributeName);
//        boolean id = field.getAnnotation(EmbeddedId.class) != null;

//        List<JpaAttribute> basicAttributes = attributes.stream().filter(a -> a.getRelationship().isEmpty())
//                .collect(Collectors.toList());
//        List<JpaAttribute> relationshipAttributes = attributes.stream().filter(a -> a.getRelationship() != null)
//                .collect(Collectors.toList());
        JpaEntity entity = new JpaEntity();
        entity.setClassName(entityClassName);
        entity.setName(field.getName());
        entity.setType(c);
        attributes.forEach(entity::addAttribute);
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
//            LOG.debug("finalizeRelationships: a={}", a);
            Optional<JpaRelationship> opRelationship = a.getRelationship();
            if (opRelationship.isEmpty())
                continue;

            JpaRelationship relationship = opRelationship.get();
            if (relationship instanceof OneToOneJpaRelationship) {
                JpaEntity toEntity = entities.get(a.getType().getName());
//                LOG.debug("finalizeRelationships: OneToOne toEntity={}", toEntity);
                if (toEntity == null)
                    throw new IllegalArgumentException("One to One entity not found (" + a.getType().getName() + ")");

                OneToOneJpaRelationship oneToOne = (OneToOneJpaRelationship) relationship;
                finalizeRelationship(oneToOne, entity, toEntity);
            } else if (relationship instanceof ManyToOneJpaRelationship) {
                JpaEntity toEntity = entities.get(a.getType().getName());
//                LOG.debug("finalizeRelationships: ManyToOne toEntity={}", toEntity);
                if (toEntity == null)
                    throw new IllegalArgumentException("Many to One entity not found (" + a.getType().getName() + ")");

                ManyToOneJpaRelationship manyToOne = (ManyToOneJpaRelationship) relationship;
                finalizeRelationship(manyToOne, toEntity);
            } else if (relationship instanceof OneToManyJpaRelationship) {
                JpaEntity toEntity = entities.get(relationship.getTargetEntityClass().getName());
//                LOG.debug("finalizeRelationships: OneToMany toEntity={}; a.getType().getName()=", toEntity,
//                        a.getType().getName());
                if (toEntity == null)
                    throw new IllegalArgumentException("One to Many target entity not found ("
                            + relationship.getTargetEntityClass().getName() + ")");

                OneToManyJpaRelationship oneToMany = (OneToManyJpaRelationship) relationship;
                finalizeRelationship(oneToMany, entity, toEntity);
            } else if (relationship instanceof ManyToManyJpaRelationship) {
                JpaEntity toEntity = entities.get(relationship.getTargetEntityClass().getName());
//                LOG.debug("finalizeRelationships: ManyToMany toEntity={}; a.getType().getName()=", toEntity,
//                        a.getType().getName());
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

    private Optional<String> getMappedBy(ManyToOne manyToOne) {
        return Optional.empty();
    }

}
