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
package org.minijpa.metadata;

import org.minijpa.jdbc.DDLData;
import org.minijpa.jdbc.JdbcTypes;
import org.minijpa.jdbc.mapper.ObjectConverter;
import org.minijpa.jdbc.relationship.JoinColumnDataList;
import org.minijpa.jpa.db.*;
import org.minijpa.jpa.db.namedquery.MiniNamedNativeQueryMapping;
import org.minijpa.jpa.db.namedquery.MiniNamedQueryMapping;
import org.minijpa.jpa.db.querymapping.QueryResultMapping;
import org.minijpa.jpa.model.*;
import org.minijpa.jpa.model.relationship.*;
import org.minijpa.metadata.enhancer.EnhAttribute;
import org.minijpa.metadata.enhancer.EnhEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class JpaParser {

    private final Logger log = LoggerFactory.getLogger(JpaParser.class);
    private final DbConfiguration dbConfiguration;
    private final OneToOneHelper oneToOneHelper = new OneToOneHelper();
    private final ManyToOneHelper manyToOneHelper = new ManyToOneHelper();
    private final OneToManyHelper oneToManyHelper = new OneToManyHelper();
    private final ManyToManyHelper manyToManyHelper = new ManyToManyHelper();
    private final SqlResultSetMappingParser sqlResultSetMappingParser = new SqlResultSetMappingParser();
    private final NamedQueryParser namedQueryParser = new NamedQueryParser();
    private final NamedNativeQueryParser namedNativeQueryParser = new NamedNativeQueryParser();
    private final PkFactory pkFactory = new PkFactory();

    public JpaParser(DbConfiguration dbConfiguration) {
        super();
        this.dbConfiguration = dbConfiguration;
    }

    public void fillRelationships(Map<String, MetaEntity> entities) throws Exception {
        entities.forEach((k, v) -> {
            v.getBasicAttributes()
                    .forEach(a -> log.trace("Building Relationships -> Basic Attribute {}", a.getName()));
        });

        finalizeRelationships(entities);
    }

    public Optional<Map<String, QueryResultMapping>> parseSqlResultSetMappings(
            Map<String, MetaEntity> entities) {
        Map<String, QueryResultMapping> map = new HashMap<>();
        for (Map.Entry<String, MetaEntity> entry : entities.entrySet()) {
            MetaEntity metaEntity = entry.getValue();
            Optional<Map<String, QueryResultMapping>> optional = sqlResultSetMappingParser
                    .parse(metaEntity.getEntityClass(), entities);
            if (optional.isPresent()) {
                // checks for uniqueness
                for (Map.Entry<String, QueryResultMapping> e : optional.get().entrySet()) {
                    if (map.containsKey(e.getKey())) {
                        throw new IllegalStateException(
                                "@SqlResultSetMapping '" + e.getKey() + "' already declared");
                    }
                }

                map.putAll(optional.get());
            }
        }

        if (map.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(map);
    }


    public Optional<Map<String, MiniNamedQueryMapping>> parseNamedQueries(
            Map<String, MetaEntity> entities) {
        Map<String, MiniNamedQueryMapping> map = new HashMap<>();
        for (Map.Entry<String, MetaEntity> entry : entities.entrySet()) {
            MetaEntity metaEntity = entry.getValue();
            Optional<Map<String, MiniNamedQueryMapping>> optional = namedQueryParser
                    .parse(metaEntity.getEntityClass());
            // checks for uniqueness
            optional.ifPresent(map::putAll);
        }

        if (map.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(map);
    }


    public Optional<Map<String, MiniNamedNativeQueryMapping>> parseNamedNativeQueries(
            Map<String, MetaEntity> entities) {
        Map<String, MiniNamedNativeQueryMapping> map = new HashMap<>();
        for (Map.Entry<String, MetaEntity> entry : entities.entrySet()) {
            MetaEntity metaEntity = entry.getValue();
            Optional<Map<String, MiniNamedNativeQueryMapping>> optional = namedNativeQueryParser
                    .parse(metaEntity.getEntityClass());
            // checks for uniqueness
            optional.ifPresent(map::putAll);
        }

        if (map.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(map);
    }


    public MetaEntity parse(
            EnhEntity enhEntity,
            Collection<MetaEntity> parsedEntities) throws Exception {
        Optional<MetaEntity> optional = parsedEntities.stream()
                .filter(e -> e.getEntityClass().getName().equals(enhEntity.getClassName())).findFirst();
        if (optional.isPresent())
            return optional.get();

        Class<?> c = Class.forName(enhEntity.getClassName());
        Entity ec = c.getAnnotation(Entity.class);
        if (ec == null)
            throw new Exception("@Entity annotation not found: '" + c.getName() + "'");

        String name = c.getSimpleName();
        if (ec.name() != null && !ec.name().trim().isEmpty()) {
            name = ec.name();
        }

        String tableName = c.getSimpleName();
        Table table = c.getAnnotation(Table.class);
        if (table != null && table.name() != null && !table.name().trim().isEmpty()) {
            tableName = table.name();
        }

        log.trace("Reading '{}' attributes...", enhEntity.getClassName());
        EntityAttributes entityAttributes = parseAttributes(enhEntity, null);
        List<MetaEntity> embeddables = parseEmbeddables(enhEntity, parsedEntities, null);
        MetaEntity mappedSuperclassEntity = null;
        log.trace("Meta Entity Mapped Superclass {}", enhEntity.getMappedSuperclass());
        if (enhEntity.getMappedSuperclass() != null) {
            log.trace("Meta Entity Mapped Superclass Class {}", enhEntity.getMappedSuperclass().getClassName());
            Optional<MetaEntity> optionalEntity = parsedEntities.stream().filter(e -> e.getMappedSuperclassEntity() != null)
                    .filter(e -> e.getMappedSuperclassEntity().getEntityClass().getName()
                            .equals(enhEntity.getMappedSuperclass().getClassName()))
                    .findFirst();

            if (optionalEntity.isPresent()) {
                mappedSuperclassEntity = optionalEntity.get().getMappedSuperclassEntity();
            } else {
                mappedSuperclassEntity = parseMappedSuperclass(enhEntity.getMappedSuperclass(),
                        parsedEntities);
            }

            List<AbstractMetaAttribute> msAttributes = mappedSuperclassEntity.getAttributes();
            entityAttributes.metaAttributes.addAll(msAttributes);
        }

        log.trace("Getting '{}' Id...", c.getName());
        log.trace("Meta Attributes -> {}", entityAttributes.metaAttributes.size());
        Pk pk = mappedSuperclassEntity != null ?
                mappedSuperclassEntity.getId() :
                pkFactory.buildPk(
                        dbConfiguration,
                        entityAttributes.metaAttributes,
                        entityAttributes.enhEntity.getIdClassPropertyData(),
                        embeddables,
                        tableName,
                        c);

        Method modificationAttributeReadMethod;
        try {
            modificationAttributeReadMethod = c.getMethod(enhEntity.getModificationAttributeGetMethod());
        } catch (NoSuchMethodException e1) {
            log.error("Meta Entity parsing. Special method '{}' not found", enhEntity.getModificationAttributeGetMethod());
            throw e1;
        } catch (Exception e1) {
            log.error(e1.getMessage());
            throw e1;
        }

        Method lazyLoadedAttributeReadMethod = null;
        if (enhEntity.getLazyLoadedAttributeGetMethod() != null) {
            lazyLoadedAttributeReadMethod =
                    c.getMethod(enhEntity.getLazyLoadedAttributeGetMethod());
        }

        Method joinColumnPostponedUpdateAttributeReadMethod = null;
        if (enhEntity.getJoinColumnPostponedUpdateAttributeGetMethod() != null) {
            joinColumnPostponedUpdateAttributeReadMethod = c.getMethod(enhEntity.getJoinColumnPostponedUpdateAttributeGetMethod());
        }

        Method lockTypeAttributeReadMethod = c.getMethod(
                enhEntity.getLockTypeAttributeGetMethod());
        Method lockTypeAttributeWriteMethod = c.getMethod(
                enhEntity.getLockTypeAttributeSetMethod(),
                LockType.class);
        Method entityStatusAttributeReadMethod = c.getMethod(
                enhEntity.getEntityStatusAttributeGetMethod());
        Method entityStatusAttributeWriteMethod = c.getMethod(
                enhEntity.getEntityStatusAttributeSetMethod(),
                EntityStatus.class);

        List<MetaEntity> embeddablesNoId = embeddables.stream().filter(me -> !me.isEmbeddedId()).
                collect(Collectors.toList());
        List<AbstractMetaAttribute> entityAttrsNoIds = entityAttributes.metaAttributes.stream()
                .filter(ea -> {
                    if (!(ea instanceof MetaAttribute))
                        return true;

                    if (((MetaAttribute) ea).isId())
                        return false;

                    return true;
                })
                .collect(Collectors.toList());

        List<MetaAttribute> basicAttributes = entityAttrsNoIds.stream()
                .filter(a -> a instanceof MetaAttribute)
                .map(a -> (MetaAttribute) a)
                .filter(MetaAttribute::isBasic)
                .collect(Collectors.toList());
        return new MetaEntity.Builder()
                .withEntityClass(c)
                .withName(name)
                .withTableName(tableName)
                .withId(pk)
                .withAttributes(entityAttrsNoIds)
                .withBasicAttributes(Collections.unmodifiableList(basicAttributes))
                .withRelationshipAttributes(entityAttributes.relationshipMetaAttributes)
                .withEmbeddables(embeddablesNoId)
                .withMappedSuperclassEntity(mappedSuperclassEntity)
                .withVersionMetaAttribute(entityAttributes.versionMetaAttribute)
                .withModificationAttributeReadMethod(modificationAttributeReadMethod)
                .withLazyLoadedAttributeReadMethod(lazyLoadedAttributeReadMethod)
                .withJoinColumnPostponedUpdateAttributeReadMethod(
                        joinColumnPostponedUpdateAttributeReadMethod)
                .withLockTypeAttributeReadMethod(lockTypeAttributeReadMethod)
                .withLockTypeAttributeWriteMethod(lockTypeAttributeWriteMethod)
                .withEntityStatusAttributeReadMethod(entityStatusAttributeReadMethod)
                .withEntityStatusAttributeWriteMethod(entityStatusAttributeWriteMethod)
                .build();
    }


    private void checkIdClassAttributes(Pk pk) throws Exception {
        if (!pk.isIdClass())
            return;

        for (AbstractMetaAttribute abstractMetaAttribute : pk.getAttributes()) {
            if (abstractMetaAttribute instanceof MetaAttribute) {
                String getMethodName = BeanUtil.getGetterMethodName(abstractMetaAttribute.getName());
                Arrays.stream(pk.getType().getMethods()).forEach(m -> log.debug("checkIdClassAttributes: m.getName()={}", m.getName()));
                Method getMethod = pk.getType().getMethod(getMethodName);

                if (getMethod.getReturnType() != abstractMetaAttribute.getType())
                    throw new Exception("IdClass attributes don't match Entity @id attributes");

                String setMethodName = BeanUtil.getSetterMethodName(abstractMetaAttribute.getName());
                Method setMethod = pk.getType().getMethod(setMethodName, abstractMetaAttribute.getType());
            }
        }

        // TODO to review
        IdClassPk idClassPk = (IdClassPk) pk;
        if (idClassPk.getRelationshipMetaAttribute() != null) {
            RelationshipMetaAttribute relationshipMetaAttribute = idClassPk.getRelationshipMetaAttribute();
            MetaEntity entity = relationshipMetaAttribute.getRelationship().getAttributeType();
            // get method check
            String getMethodName = BeanUtil.getGetterMethodName(relationshipMetaAttribute.getName());
            Method idClassGetMethod = pk.getType().getMethod(getMethodName);
        }
    }


    private MetaEntity parseEmbeddable(
            String parentClassName,
            EnhAttribute enhAttribute,
            EnhEntity enhEntity,
            Collection<MetaEntity> parsedEntities,
            String parentPath) throws Exception {
        Optional<MetaEntity> optional = parsedEntities.stream()
                .filter(e -> e.getEntityClass().getName().equals(enhEntity.getClassName())).findFirst();
        if (optional.isPresent()) {
            return optional.get();
        }

        Class<?> c = Class.forName(enhEntity.getClassName());
        Embeddable ec = c.getAnnotation(Embeddable.class);
        if (ec == null) {
            throw new Exception("@Embeddable annotation not found: '" + c.getName() + "'");
        }

        log.trace("Reading Embeddable '{}' attributes...", enhEntity.getClassName());
        String path = parentPath == null ? enhAttribute.getName()
                : parentPath + "." + enhAttribute.getName();
        EntityAttributes entityAttributes = parseAttributes(enhEntity, path);
        List<MetaEntity> embeddables = parseEmbeddables(enhEntity, parsedEntities, path);

        Method modificationAttributeReadMethod = null;
        if (enhEntity.getModificationAttributeGetMethod() != null) {
            modificationAttributeReadMethod = c.getMethod(enhEntity.getModificationAttributeGetMethod());
        }

        Method lazyLoadedAttributeReadMethod = null;
        if (enhEntity.getLazyLoadedAttributeGetMethod() != null) {
            lazyLoadedAttributeReadMethod =
                    c.getMethod(enhEntity.getLazyLoadedAttributeGetMethod());
        }

        Method joinColumnPostponedUpdateAttributeReadMethod = null;
        if (enhEntity.getJoinColumnPostponedUpdateAttributeGetMethod() != null) {
            joinColumnPostponedUpdateAttributeReadMethod = c.getMethod(enhEntity.getJoinColumnPostponedUpdateAttributeGetMethod());
        }

        Class<?> attributeClass = Class.forName(enhAttribute.getClassName());
        Class<?> parentClass = Class.forName(parentClassName);
        Method readMethod = parentClass.getMethod(enhAttribute.getGetMethod());
        Method writeMethod = parentClass.getMethod(enhAttribute.getSetMethod(), attributeClass);

        Field field = parentClass.getDeclaredField(enhAttribute.getName());
        boolean id = field.getAnnotation(EmbeddedId.class) != null;

        List<MetaAttribute> basicAttributes = entityAttributes.metaAttributes.stream()
                .filter(a -> a instanceof MetaAttribute).map(a -> (MetaAttribute) a)
                .filter(MetaAttribute::isBasic)
                .collect(Collectors.toList());
        return new MetaEntity.Builder()
                .withEntityClass(c)
                .withName(enhAttribute.getName())
                .isEmbeddedId(id)
                .withAttributes(entityAttributes.metaAttributes)
                .withBasicAttributes(Collections.unmodifiableList(basicAttributes))
                .withRelationshipAttributes(entityAttributes.relationshipMetaAttributes)
                .withEmbeddables(embeddables)
                .withReadMethod(readMethod).withWriteMethod(writeMethod).withPath(path)
                .withModificationAttributeReadMethod(modificationAttributeReadMethod)
                .withLazyLoadedAttributeReadMethod(lazyLoadedAttributeReadMethod)
                .withJoinColumnPostponedUpdateAttributeReadMethod(
                        joinColumnPostponedUpdateAttributeReadMethod)
                .withVersionMetaAttribute(entityAttributes.versionMetaAttribute)
                .build();
    }

    private MetaEntity parseMappedSuperclass(EnhEntity enhEntity,
                                             Collection<MetaEntity> parsedEntities)
            throws Exception {
        Class<?> c = Class.forName(enhEntity.getClassName());
        MappedSuperclass ec = c.getAnnotation(MappedSuperclass.class);
        if (ec == null) {
            throw new Exception("@MappedSuperclass annotation not found: '" + c.getName() + "'");
        }

        log.trace("Reading mapped superclass '{}' attributes...", enhEntity.getClassName());
        EntityAttributes entityAttributes = parseAttributes(enhEntity, null);
        List<MetaEntity> embeddables = parseEmbeddables(enhEntity, parsedEntities, null);

        Pk pk = pkFactory.buildPk(
                dbConfiguration,
                entityAttributes.metaAttributes,
                entityAttributes.enhEntity.getIdClassPropertyData(),
                embeddables,
                c.getSimpleName().toUpperCase(),
                c);

        Method modificationAttributeReadMethod = c.getMethod(
                enhEntity.getModificationAttributeGetMethod());
        Method lazyLoadedAttributeReadMethod = null;
        if (enhEntity.getLazyLoadedAttributeGetMethod() != null) {
            lazyLoadedAttributeReadMethod =
                    c.getMethod(enhEntity.getLazyLoadedAttributeGetMethod());
        }

        Method joinColumnPostponedUpdateAttributeReadMethod = null;
        if (enhEntity.getJoinColumnPostponedUpdateAttributeGetMethod() != null) {
            joinColumnPostponedUpdateAttributeReadMethod =
                    c.getMethod(enhEntity.getJoinColumnPostponedUpdateAttributeGetMethod());
        }

        List<MetaEntity> embeddablesNoId = embeddables.stream().filter(me -> !me.isEmbeddedId()).
                collect(Collectors.toList());
        List<AbstractMetaAttribute> entityAttrsNoIds = entityAttributes.metaAttributes.stream()
                .filter(ea -> {
                    if (!(ea instanceof MetaAttribute))
                        return true;

                    if (((MetaAttribute) ea).isId())
                        return false;

                    return true;
                })
                .collect(Collectors.toList());

        List<MetaAttribute> basicAttributes = entityAttrsNoIds.stream()
                .filter(a -> a instanceof MetaAttribute).map(a -> (MetaAttribute) a)
                .filter(MetaAttribute::isBasic)
                .collect(Collectors.toList());
        return new MetaEntity.Builder()
                .withEntityClass(c)
                .withId(pk)
                .withAttributes(entityAttrsNoIds)
                .withBasicAttributes(Collections.unmodifiableList(basicAttributes))
                .withRelationshipAttributes(entityAttributes.relationshipMetaAttributes)
                .withEmbeddables(embeddablesNoId)
                .withModificationAttributeReadMethod(modificationAttributeReadMethod)
                .withLazyLoadedAttributeReadMethod(lazyLoadedAttributeReadMethod)
                .withJoinColumnPostponedUpdateAttributeReadMethod(
                        joinColumnPostponedUpdateAttributeReadMethod)
                .withVersionMetaAttribute(entityAttributes.versionMetaAttribute)
                .build();
    }

    private static class EntityAttributes {
        EnhEntity enhEntity;
        List<AbstractMetaAttribute> metaAttributes = new ArrayList<>();
        List<RelationshipMetaAttribute> relationshipMetaAttributes = new ArrayList<>();
        VersionMetaAttribute versionMetaAttribute;
    }

    private EntityAttributes parseAttributes(
            EnhEntity enhEntity,
            String parentPath) throws Exception {
        EntityAttributes entityAttributes = new EntityAttributes();
        log.trace("Parsing Attributes {}", enhEntity.getEnhAttributes().size());
        for (EnhAttribute enhAttribute : enhEntity.getEnhAttributes()) {
            log.trace("Parsing Attributes -> Attribute '{}'", enhAttribute.getName());
            log.trace("Parsing Attributes -> Is Embedded {}", enhAttribute.isEmbedded());
            if (enhAttribute.isEmbedded())
                continue;

            Class<?> c = Class.forName(enhEntity.getClassName());
            Field field = c.getDeclaredField(enhAttribute.getName());
            Optional<Relationship> relationship = buildRelationship(field);
            log.trace("Parsing Attributes-> Relationship {}", relationship);

            log.trace("Parsing Attributes -> Class Name '{}'", enhAttribute.getClassName());
            Class<?> attributeClass;
            if (enhAttribute.isPrimitiveType()) {
                attributeClass = JavaTypes.getClass(enhAttribute.getClassName());
            } else {
                attributeClass = Class.forName(enhAttribute.getClassName());
            }

            log.trace("Parsing Attributes -> Attribute Class '{}'", attributeClass);
            Method readMethod = c.getMethod(enhAttribute.getGetMethod());
            Method writeMethod = c.getMethod(enhAttribute.getSetMethod(), attributeClass);
            if (relationship.isPresent()) {
                RelationshipMetaAttribute relationshipMetaAttribute = buildRelationshipMetaAttribute(c,
                        enhAttribute, attributeClass, readMethod, writeMethod,
                        relationship.get(),
                        field);
                entityAttributes.relationshipMetaAttributes.add(relationshipMetaAttribute);
                entityAttributes.metaAttributes.add(relationshipMetaAttribute);
            } else {
                MetaAttribute attribute = parseAttribute(
                        field,
                        attributeClass,
                        readMethod,
                        writeMethod,
                        enhEntity.getClassName(),
                        enhAttribute,
                        parentPath);
                entityAttributes.metaAttributes.add(attribute);
                if (attribute.isVersion())
                    entityAttributes.versionMetaAttribute = (VersionMetaAttribute) attribute;
            }
        }

        entityAttributes.enhEntity = enhEntity;
        return entityAttributes;
    }


    private RelationshipMetaAttribute buildRelationshipMetaAttribute(
            Class<?> parentClass,
            EnhAttribute enhAttribute,
            Class<?> attributeClass,
            Method readMethod,
            Method writeMethod,
            Relationship relationship,
            Field field) throws Exception {
        boolean isCollection = CollectionUtils.isCollectionClass(attributeClass);
        Class<?> collectionImplementationClass = null;
        if (isCollection) {
            collectionImplementationClass = CollectionUtils.findCollectionImplementationClass(
                    attributeClass);
        }

        Method joinColumnReadMethod = enhAttribute.getJoinColumnGetMethod() != null
                ? parentClass.getMethod(enhAttribute.getJoinColumnGetMethod())
                : null;
        Method joinColumnWriteMethod = enhAttribute.getJoinColumnSetMethod() != null
                ? parentClass.getMethod(enhAttribute.getJoinColumnSetMethod(), Object.class)
                : null;

        RelationshipMetaAttribute.Builder builder = new RelationshipMetaAttribute.Builder()
                .withName(enhAttribute.getName())
                .withType(attributeClass)
                .withReadMethod(readMethod)
                .withWriteMethod(writeMethod)
                .isCollection(isCollection)
                .withCollectionImplementationClass(collectionImplementationClass)
                .withRelationship(relationship)
                .withJoinColumnReadMethod(joinColumnReadMethod)
                .withJoinColumnWriteMethod(joinColumnWriteMethod)
                .isId(relationship.isId())
                .withJavaMember(field);
        return builder.build();
    }

    private List<MetaEntity> parseEmbeddables(
            EnhEntity enhEntity,
            Collection<MetaEntity> parsedEntities,
            String parentPath) throws Exception {
        List<MetaEntity> metaEntities = new ArrayList<>();
        for (EnhAttribute enhAttribute : enhEntity.getEnhAttributes()) {
            if (!enhAttribute.isEmbedded()) {
                continue;
            }

            MetaEntity metaEntity = parseEmbeddable(enhEntity.getClassName(), enhAttribute,
                    enhAttribute.getEmbeddedEnhEntity(), parsedEntities, parentPath);
            metaEntities.add(metaEntity);
        }

        return metaEntities;
    }


    private Class<?> enumerationType(Class<?> attributeClass, Enumerated enumerated) {
        if (attributeClass.isEnum() && enumerated == null) {
            return Integer.class;
        }

        if (attributeClass.isEnum()) {
            if (enumerated.value() == null) {
                return Integer.class;
            }

            if (enumerated.value() == EnumType.STRING) {
                return String.class;
            }

            if (enumerated.value() == EnumType.ORDINAL) {
                return Integer.class;
            }
        }

        return null;
    }

    private MetaAttribute parseAttribute(
            Field field,
            Class<?> attributeClass,
            Method readMethod,
            Method writeMethod,
            String parentClassName,
            EnhAttribute enhAttribute,
            String parentPath) {
        String columnName = enhAttribute.getName();
        Column column = field.getAnnotation(Column.class);
        log.trace("Parsing Attribute {}", columnName);
        Id idAnnotation = field.getAnnotation(Id.class);
        boolean nullableColumn = !attributeClass.isPrimitive() && !enhAttribute.isParentEmbeddedId()
                && (idAnnotation == null);
        DDLData ddlData;
        if (column != null) {
            String cn = column.name();
            if (cn != null && !cn.trim().isEmpty()) {
                columnName = cn;
            }

            ddlData = buildDDLData(column, nullableColumn);
        } else {
            ddlData = new DDLData(null, 255, 0, 0,
                    nullableColumn, false);
        }

        Enumerated enumerated = field.getAnnotation(Enumerated.class);
        log.trace("Is Attribute Enumerated: {}", enumerated != null);
        Class<?> enumerationType = enumerationType(attributeClass, enumerated);
        Class<?> readWriteType = findDatabaseType(field, attributeClass, enumerationType);
        log.trace("Attribute Database Type: {}", readWriteType);
        Integer sqlType = JdbcTypes.sqlTypeFromClass(readWriteType);
        log.trace("Attribute Sql Type: {}", sqlType);
        String path = parentPath != null ? parentPath + "." + enhAttribute.getName() : enhAttribute.getName();
        log.trace("Attribute Path: {}", path);
        log.trace("Is Attribute Id: {}", idAnnotation != null);
        if (idAnnotation != null) {
            ObjectConverter<?, ?> objectConverter = dbConfiguration.getDbTypeMapper()
                    .attributeMapper(attributeClass,
                            readWriteType);
            log.trace("Attribute Object Converter '{}'", objectConverter);
            MetaAttribute.Builder builder = new MetaAttribute.Builder(
                    enhAttribute.getName())
                    .withColumnName(columnName)
                    .withType(attributeClass)
                    .withReadWriteDbType(readWriteType)
                    .withReadMethod(readMethod)
                    .withWriteMethod(writeMethod)
                    .isId(true)
                    .withSqlType(sqlType)
                    .withJavaMember(field)
                    .isBasic(true)
                    .withPath(path)
                    .withDDLData(ddlData)
                    .withAttributeMapper(objectConverter);
            return builder.build();
        }

        boolean version = field.getAnnotation(Version.class) != null;
        if (version) {
            VersionMetaAttribute.VersionMetaAttributeBuilder builder =
                    (VersionMetaAttribute.VersionMetaAttributeBuilder) new VersionMetaAttribute.VersionMetaAttributeBuilder(
                            enhAttribute.getName())
                            .withColumnName(columnName)
                            .withType(attributeClass)
                            .withReadWriteDbType(readWriteType)
                            .withReadMethod(readMethod)
                            .withWriteMethod(writeMethod)
                            .withSqlType(sqlType)
                            .withJavaMember(field)
                            .isVersion(true)
                            .isBasic(AttributeUtil.isBasicAttribute(attributeClass))
                            .withPath(path)
                            .withDDLData(ddlData);
            return builder.build();
        }

        MetaAttribute.Builder builder = new MetaAttribute.Builder(
                enhAttribute.getName())
                .withColumnName(columnName)
                .withType(attributeClass)
                .withReadWriteDbType(readWriteType)
                .withReadMethod(readMethod)
                .withWriteMethod(writeMethod)
                .withSqlType(sqlType)
                .withJavaMember(field)
                .isVersion(version)
                .isBasic(AttributeUtil.isBasicAttribute(attributeClass))
                .withPath(path)
                .withDDLData(ddlData);

        ObjectConverter<?, ?> objectConverter = dbConfiguration.getDbTypeMapper()
                .attributeMapper(attributeClass, readWriteType);
        builder.withAttributeMapper(objectConverter);

        // Basic annotation
        Basic basic = field.getAnnotation(Basic.class);
        if (basic != null) {
            if (!basic.optional()) {
                builder.isNullable(false);
            }
        }

        MetaAttribute attribute = builder.build();
        return attribute;
    }

    private Class<?> findDatabaseType(
            Field field,
            Class<?> attributeClass,
            Class<?> enumerationType) {
        Optional<Class<?>> optional = temporalType(field, attributeClass);
        if (optional.isPresent()) {
            return dbConfiguration.getDbTypeMapper().databaseType(optional.get(), enumerationType);
        }

        return dbConfiguration.getDbTypeMapper().databaseType(attributeClass, enumerationType);
    }

    private Optional<Class<?>> temporalType(Field field, Class<?> attributeClass) {
        if (attributeClass != Date.class && attributeClass != Calendar.class) {
            return Optional.empty();
        }

        Temporal temporal = field.getAnnotation(Temporal.class);
        if (temporal == null) {
            return Optional.empty();
        }

        TemporalType temporalType = temporal.value();
        if (temporalType == null) {
            return Optional.empty();
        }

        if (temporalType == TemporalType.DATE) {
            return Optional.of(java.sql.Date.class);
        }

        if (temporalType == TemporalType.TIME) {
            return Optional.of(Time.class);
        }

        return Optional.of(Timestamp.class);
    }

    private DDLData buildDDLData(Column column, boolean nullableColumn) {
        String columnDefinition = null;
        String cd = column.columnDefinition();
        if (cd != null && !cd.trim().isEmpty()) {
            columnDefinition = cd.trim();
        }

        Integer length = column.length();

        Integer precision = null;
        int p = column.precision();
        if (p != 0) {
            precision = p;
        }

        Integer scale = null;
        int s = column.scale();
        if (s != 0) {
            scale = s;
        }

//    if (columnDefinition.isEmpty() && length.isEmpty() && precision.isEmpty() && scale.isEmpty()) {
//      return Optional.empty();
//    }

        Boolean nullable =
                !nullableColumn ? Boolean.FALSE : column.nullable();
        Boolean unique = column.unique();

        return new DDLData(columnDefinition, length, precision, scale, nullable, unique);
    }


    private Optional<Relationship> buildRelationship(Field field) {
        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
        ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
        Id idAnnotation = field.getAnnotation(Id.class);
        int counter = 0;
        if (oneToOne != null) {
            ++counter;
        }
        if (manyToOne != null) {
            ++counter;
        }
        if (oneToMany != null) {
            ++counter;
        }
        if (manyToMany != null) {
            ++counter;
        }

        if (counter > 1) {
            throw new IllegalArgumentException(
                    "More than one relationship annotations at '" + field.getName() + "'");
        }

        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        JoinColumns joinColumns = field.getAnnotation(JoinColumns.class);
        JoinColumnDataList joinColumnDataList = RelationshipUtils.buildJoinColumnDataList(
                joinColumn,
                joinColumns);
        if (oneToOne != null) {
            return Optional.of(oneToOneHelper.createOneToOne(oneToOne, joinColumnDataList, idAnnotation != null));
        }

        if (manyToOne != null) {
            return Optional.of(manyToOneHelper.createManyToOne(manyToOne, joinColumnDataList, idAnnotation != null));
        }

        if (oneToMany != null) {
            Class<?> collectionClass = field.getType();
            log.trace("Building Relationship -> Field Type '{}'", field.getType());
            Class<?> targetEntity = oneToMany.targetEntity();
            if (targetEntity == null || targetEntity == Void.TYPE) {
                targetEntity = ReflectionUtil.findTargetEntity(field);
            }

            JoinTable joinTable = field.getAnnotation(JoinTable.class);
            return Optional.of(
                    oneToManyHelper.createOneToMany(oneToMany, collectionClass, targetEntity, joinTable,
                            joinColumnDataList, idAnnotation != null));
        }

        if (manyToMany != null) {
            Class<?> collectionClass = field.getType();
            Class<?> targetEntity = manyToMany.targetEntity();
            if (targetEntity == null || targetEntity == Void.TYPE) {
                targetEntity = ReflectionUtil.findTargetEntity(field);
            }

            JoinTable joinTable = field.getAnnotation(JoinTable.class);
            return Optional.of(
                    manyToManyHelper.createManyToMany(manyToMany, collectionClass, targetEntity, joinTable,
                            joinColumnDataList, idAnnotation != null));
        }

        return Optional.empty();
    }


    private void finalizeRelationships(
            MetaEntity entity,
            Map<String, MetaEntity> entities,
            List<RelationshipMetaAttribute> attributes) {
        entity.getEmbeddables().forEach(embeddable -> {
            finalizeRelationships(embeddable, entities, embeddable.getRelationshipAttributes());
        });

        for (RelationshipMetaAttribute a : attributes) {
            log.trace("Relationship Attribute: {}", a);
            Relationship relationship = a.getRelationship();
            if (relationship instanceof OneToOneRelationship) {
                MetaEntity toEntity = entities.get(a.getType().getName());
                log.trace("Relationship OneToOne To Entity {}", toEntity);
                if (toEntity == null) {
                    throw new IllegalArgumentException(
                            "One to One entity not found (" + a.getType().getName() + ")");
                }

                OneToOneRelationship oneToOne = (OneToOneRelationship) relationship;
                a.setRelationship(
                        oneToOneHelper.finalizeRelationship(oneToOne, a, entity, toEntity, dbConfiguration));
            } else if (relationship instanceof ManyToOneRelationship) {
                MetaEntity toEntity = entities.get(a.getType().getName());
                log.trace("Relationship ManyToOne To Entity {}", toEntity);
                if (toEntity == null) {
                    throw new IllegalArgumentException(
                            "Many to One entity not found (" + a.getType().getName() + ")");
                }

                ManyToOneRelationship manyToOne = (ManyToOneRelationship) relationship;
                a.setRelationship(
                        manyToOneHelper.finalizeRelationship(manyToOne, a, entity, toEntity, dbConfiguration));
            } else if (relationship instanceof OneToManyRelationship) {
                MetaEntity toEntity = entities.get(relationship.getTargetEntityClass().getName());
                log.trace("Relationship OneToMany To Entity {}", toEntity);
                if (toEntity == null) {
                    throw new IllegalArgumentException("One to Many target entity not found ("
                            + relationship.getTargetEntityClass().getName() + ")");
                }

                OneToManyRelationship oneToMany = (OneToManyRelationship) relationship;
                OneToManyRelationship otm = oneToManyHelper.finalizeRelationship(oneToMany, a, entity,
                        toEntity,
                        dbConfiguration);
                a.setRelationship(otm);
            } else if (relationship instanceof ManyToManyRelationship) {
                MetaEntity toEntity = entities.get(relationship.getTargetEntityClass().getName());
                log.trace("Relationship ManyToMany To Entity {}", toEntity);
                if (toEntity == null) {
                    throw new IllegalArgumentException("Many to Many target entity not found ("
                            + relationship.getTargetEntityClass().getName() + ")");
                }

                ManyToManyRelationship manyToMany = (ManyToManyRelationship) relationship;
                ManyToManyRelationship otm = manyToManyHelper.finalizeRelationship(manyToMany, a, entity,
                        toEntity,
                        dbConfiguration, entities);
                a.setRelationship(otm);
            }
        }
    }

    /**
     * It's a post-processing step needed to complete entity data. For example, filling out the 'join
     * column' one to one relationships if missing.
     *
     * @param entities the list of all entities
     */
    private void finalizeRelationships(Map<String, MetaEntity> entities) throws Exception {
        for (Map.Entry<String, MetaEntity> entry : entities.entrySet()) {
            MetaEntity entity = entry.getValue();
            finalizeRelationships(entity, entities, entity.getRelationshipAttributes());
        }

        // checks Pk IdClass
        for (Map.Entry<String, MetaEntity> entry : entities.entrySet()) {
            MetaEntity entity = entry.getValue();
            try {
                checkIdClassAttributes(entity.getId());
            } catch (Exception e) {
                throw new Exception("IdClass attributes don't match Entity @id attributes: " + entity.getName());
            }
        }
    }


}
