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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.*;

import org.minijpa.jdbc.DDLData;
import org.minijpa.jdbc.JdbcTypes;
import org.minijpa.jdbc.PkSequenceGenerator;
import org.minijpa.jdbc.mapper.AttributeMapper;
import org.minijpa.jdbc.relationship.JoinColumnDataList;
import org.minijpa.jpa.db.AttributeUtil;
import org.minijpa.jpa.db.CollectionUtils;
import org.minijpa.jpa.db.DbConfiguration;
import org.minijpa.jpa.db.EntityStatus;
import org.minijpa.jpa.db.LockType;
import org.minijpa.jpa.db.PkGeneration;
import org.minijpa.jpa.db.PkGenerationType;
import org.minijpa.jpa.db.PkStrategy;
import org.minijpa.jpa.db.namedquery.MiniNamedNativeQueryMapping;
import org.minijpa.jpa.db.namedquery.MiniNamedQueryMapping;
import org.minijpa.jpa.db.querymapping.QueryResultMapping;
import org.minijpa.jpa.model.*;
import org.minijpa.jpa.model.relationship.ManyToManyRelationship;
import org.minijpa.jpa.model.relationship.ManyToOneRelationship;
import org.minijpa.jpa.model.relationship.OneToManyRelationship;
import org.minijpa.jpa.model.relationship.OneToOneRelationship;
import org.minijpa.jpa.model.relationship.Relationship;
import org.minijpa.metadata.enhancer.EnhAttribute;
import org.minijpa.metadata.enhancer.EnhEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            log.debug("fillRelationships: v.getName()={}", v.getName());
            v.getBasicAttributes()
                    .forEach(a -> log.debug("fillRelationships: ba a.getName()={}", a.getName()));
        });

        finalizeRelationships(entities);
        entities.forEach((k, v) -> {
            log.debug("fillRelationships: 2 v.getName()={}", v.getName());
            v.getBasicAttributes()
                    .forEach(a -> log.debug("fillRelationships: 2 ba a.getName()={}", a.getName()));
        });

        printAttributes(entities);
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

        log.info("Reading '{}' attributes...", enhEntity.getClassName());
        EntityAttributes entityAttributes = parseAttributes(enhEntity, Optional.empty());
        List<MetaEntity> embeddables = parseEmbeddables(enhEntity, parsedEntities, Optional.empty());
        MetaEntity mappedSuperclassEntity = null;
        log.debug("parse: enhEntity.getMappedSuperclass()={}", enhEntity.getMappedSuperclass());
        if (enhEntity.getMappedSuperclass() != null) {
            log.debug("parse: enhEntity.getMappedSuperclass().getClassName()={}",
                    enhEntity.getMappedSuperclass().getClassName());
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

        log.debug("Getting '{}' Id...", c.getName());
        log.debug("parse: entityAttributes.metaAttributes.size()={}",
                entityAttributes.metaAttributes.size());
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
            log.error("parse: special method '{}' not found", enhEntity.getModificationAttributeGetMethod());
            throw e1;
        } catch (Exception e1) {
            log.error(e1.getMessage());
            throw e1;
        }

        Optional<Method> lazyLoadedAttributeReadMethod = Optional.empty();
        if (enhEntity.getLazyLoadedAttributeGetMethod().isPresent()) {
            lazyLoadedAttributeReadMethod = Optional.of(
                    c.getMethod(enhEntity.getLazyLoadedAttributeGetMethod().get()));
        }

        Optional<Method> joinColumnPostponedUpdateAttributeReadMethod = Optional.empty();
        if (enhEntity.getJoinColumnPostponedUpdateAttributeGetMethod().isPresent()) {
            joinColumnPostponedUpdateAttributeReadMethod = Optional
                    .of(c.getMethod(enhEntity.getJoinColumnPostponedUpdateAttributeGetMethod().get()));
        }

        Method lockTypeAttributeReadMethod = c.getMethod(
                enhEntity.getLockTypeAttributeGetMethod().get());
        Method lockTypeAttributeWriteMethod = c.getMethod(
                enhEntity.getLockTypeAttributeSetMethod().get(),
                LockType.class);
        Method entityStatusAttributeReadMethod = c.getMethod(
                enhEntity.getEntityStatusAttributeGetMethod().get());
        Method entityStatusAttributeWriteMethod = c.getMethod(
                enhEntity.getEntityStatusAttributeSetMethod().get(),
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
                .withLockTypeAttributeReadMethod(Optional.of(lockTypeAttributeReadMethod))
                .withLockTypeAttributeWriteMethod(Optional.of(lockTypeAttributeWriteMethod))
                .withEntityStatusAttributeReadMethod(Optional.of(entityStatusAttributeReadMethod))
                .withEntityStatusAttributeWriteMethod(Optional.of(entityStatusAttributeWriteMethod))
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
            Optional<String> parentPath) throws Exception {
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

        log.debug("Reading '{}' attributes...", enhEntity.getClassName());
        String path = parentPath.isEmpty() ? enhAttribute.getName()
                : parentPath.get() + "." + enhAttribute.getName();
        EntityAttributes entityAttributes = parseAttributes(enhEntity, Optional.of(path));
        List<MetaEntity> embeddables = parseEmbeddables(enhEntity, parsedEntities, Optional.of(path));

        Method modificationAttributeReadMethod = null;
        if (enhEntity.getModificationAttributeGetMethod() != null) {
            modificationAttributeReadMethod = c.getMethod(enhEntity.getModificationAttributeGetMethod());
        }

        Optional<Method> lazyLoadedAttributeReadMethod = Optional.empty();
        if (enhEntity.getLazyLoadedAttributeGetMethod().isPresent()) {
            lazyLoadedAttributeReadMethod = Optional.of(
                    c.getMethod(enhEntity.getLazyLoadedAttributeGetMethod().get()));
        }

        Optional<Method> joinColumnPostponedUpdateAttributeReadMethod = Optional.empty();
        if (enhEntity.getJoinColumnPostponedUpdateAttributeGetMethod().isPresent()) {
            joinColumnPostponedUpdateAttributeReadMethod = Optional
                    .of(c.getMethod(enhEntity.getJoinColumnPostponedUpdateAttributeGetMethod().get()));
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

        log.debug("Reading mapped superclass '{}' attributes...", enhEntity.getClassName());
        EntityAttributes entityAttributes = parseAttributes(enhEntity, Optional.empty());
        List<MetaEntity> embeddables = parseEmbeddables(enhEntity, parsedEntities, Optional.empty());

        Pk pk = pkFactory.buildPk(
                dbConfiguration,
                entityAttributes.metaAttributes,
                entityAttributes.enhEntity.getIdClassPropertyData(),
                embeddables,
                c.getSimpleName().toUpperCase(),
                c);

        Method modificationAttributeReadMethod = c.getMethod(
                enhEntity.getModificationAttributeGetMethod());
        Optional<Method> lazyLoadedAttributeReadMethod = Optional.empty();
        if (enhEntity.getLazyLoadedAttributeGetMethod().isPresent()) {
            lazyLoadedAttributeReadMethod = Optional.of(
                    c.getMethod(enhEntity.getLazyLoadedAttributeGetMethod().get()));
        }

        Optional<Method> joinColumnPostponedUpdateAttributeReadMethod = Optional.empty();
        if (enhEntity.getJoinColumnPostponedUpdateAttributeGetMethod().isPresent()) {
            joinColumnPostponedUpdateAttributeReadMethod = Optional
                    .of(c.getMethod(enhEntity.getJoinColumnPostponedUpdateAttributeGetMethod().get()));
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
            Optional<String> parentPath) throws Exception {
        EntityAttributes entityAttributes = new EntityAttributes();
        log.debug("parseAttributes: enhEntity.getEnhAttributes().size()={}", enhEntity.getEnhAttributes().size());
        for (EnhAttribute enhAttribute : enhEntity.getEnhAttributes()) {
            log.debug("parseAttributes: enhAttribute.getName()={}", enhAttribute.getName());
            if (enhAttribute.isEmbedded())
                continue;

            String columnName = enhAttribute.getName();
            Class<?> c = Class.forName(enhEntity.getClassName());
            log.debug("Reading attribute '{}'", columnName);
            Field field = c.getDeclaredField(enhAttribute.getName());
            log.debug("parseAttributes: field={}", field);
            Optional<Relationship> relationship = buildRelationship(field);
            log.debug("parseAttributes: relationship={}", relationship);

            log.debug("parseAttributes: enhAttribute.getClassName()={}", enhAttribute.getClassName());
            Class<?> attributeClass;
            if (enhAttribute.isPrimitiveType()) {
                attributeClass = JavaTypes.getClass(enhAttribute.getClassName());
            } else {
                attributeClass = Class.forName(enhAttribute.getClassName());
            }

            log.debug("parseAttributes: attributeClass={}", attributeClass);
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

        Optional<Method> joinColumnReadMethod = enhAttribute.getJoinColumnGetMethod().isPresent()
                ? Optional.of(parentClass.getMethod(enhAttribute.getJoinColumnGetMethod().get()))
                : Optional.empty();
        Optional<Method> joinColumnWriteMethod = enhAttribute.getJoinColumnSetMethod().isPresent()
                ? Optional.of(
                parentClass.getMethod(enhAttribute.getJoinColumnSetMethod().get(), Object.class))
                : Optional.empty();

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
            Optional<String> parentPath) throws Exception {
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

    private Integer findSqlType(Class<?> attributeClass, Enumerated enumerated) {
        Integer sqlType = JdbcTypes.sqlTypeFromClass(attributeClass);
        return sqlType;
    }

    private Optional<Class<?>> enumerationType(Class<?> attributeClass, Enumerated enumerated) {
        if (attributeClass.isEnum() && enumerated == null) {
            return Optional.of(Integer.class);
        }

        if (attributeClass.isEnum() && enumerated != null) {
            if (enumerated.value() == null) {
                return Optional.of(Integer.class);
            }

            if (enumerated.value() == EnumType.STRING) {
                return Optional.of(String.class);
            }

            if (enumerated.value() == EnumType.ORDINAL) {
                return Optional.of(Integer.class);
            }
        }

        return Optional.empty();
    }

    private MetaAttribute parseAttribute(
            Field field,
            Class<?> attributeClass,
            Method readMethod,
            Method writeMethod,
            String parentClassName,
            EnhAttribute enhAttribute,
            Optional<String> parentPath) {
        String columnName = enhAttribute.getName();
        Column column = field.getAnnotation(Column.class);

        Id idAnnotation = field.getAnnotation(Id.class);
        boolean nullableColumn = !attributeClass.isPrimitive() && !enhAttribute.isParentEmbeddedId()
                && (idAnnotation == null);
        Optional<DDLData> ddlData;
        if (column != null) {
            String cn = column.name();
            if (cn != null && !cn.trim().isEmpty()) {
                columnName = cn;
            }

            ddlData = buildDDLData(column, nullableColumn);
        } else {
            ddlData = Optional.of(
                    new DDLData(Optional.empty(), Optional.of(255), Optional.of(0), Optional.of(0),
                            Optional.of(nullableColumn), Optional.of(false)));
        }

        Enumerated enumerated = field.getAnnotation(Enumerated.class);
        log.debug("readAttribute: enumerated={}", enumerated);
        Optional<Class<?>> enumerationType = enumerationType(attributeClass, enumerated);
        log.debug("readAttribute: dbConfiguration={}", dbConfiguration);
        Class<?> readWriteType = findDatabaseType(field, attributeClass, enumerationType);
        log.debug("readAttribute: readWriteType={}", readWriteType);
        Integer sqlType = findSqlType(readWriteType, enumerated);
        log.debug("readAttribute: sqlType={}", sqlType);
        log.debug("readAttribute: parentPath.isEmpty() ={}", parentPath.isEmpty());
        String path = parentPath.map(s -> s + "." + enhAttribute.getName()).orElseGet(enhAttribute::getName);
        log.debug("readAttribute: path={}", path);
        log.debug("readAttribute: idAnnotation={}", idAnnotation);
        if (idAnnotation != null) {
            AttributeMapper<?, ?> attributeMapper = dbConfiguration.getDbTypeMapper()
                    .attributeMapper(attributeClass,
                            readWriteType);
            log.debug("readAttribute: id attributeMapper={}", attributeMapper);
//            Optional<AttributeMapper> optionalAM = Optional.empty();
//            if (attributeMapper != null) {
//                optionalAM = Optional.of(attributeMapper);
//            }

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
                    .withAttributeMapper(attributeMapper);
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

        AttributeMapper<?, ?> attributeMapper = dbConfiguration.getDbTypeMapper()
                .attributeMapper(attributeClass, readWriteType);
        builder.withAttributeMapper(attributeMapper);

        // Basic annotation
        Basic basic = field.getAnnotation(Basic.class);
        if (basic != null) {
            if (!basic.optional()) {
                builder.isNullable(false);
            }
        }

        MetaAttribute attribute = builder.build();
        log.debug("readAttribute: attribute: {}", attribute);
        return attribute;
    }

    private Class<?> findDatabaseType(
            Field field,
            Class<?> attributeClass,
            Optional<Class<?>> enumerationType) {
        Optional<Class<?>> optional = temporalType(field, attributeClass);
        log.debug("findDatabaseType: optional={}", optional);
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

    private Optional<DDLData> buildDDLData(Column column, boolean nullableColumn) {
        Optional<String> columnDefinition = Optional.empty();
        String cd = column.columnDefinition();
        if (cd != null && !cd.trim().isEmpty()) {
            columnDefinition = Optional.of(cd.trim());
        }

        Optional<Integer> length = Optional.of(column.length());

        Optional<Integer> precision = Optional.empty();
        int p = column.precision();
        if (p != 0) {
            precision = Optional.of(p);
        }

        Optional<Integer> scale = Optional.empty();
        int s = column.scale();
        if (s != 0) {
            scale = Optional.of(s);
        }

//    if (columnDefinition.isEmpty() && length.isEmpty() && precision.isEmpty() && scale.isEmpty()) {
//      return Optional.empty();
//    }

        Optional<Boolean> nullable =
                !nullableColumn ? Optional.of(Boolean.FALSE) : Optional.of(column.nullable());
        Optional<Boolean> unique = Optional.of(column.unique());

        return Optional.of(new DDLData(columnDefinition, length, precision, scale, nullable, unique));
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
        Optional<JoinColumnDataList> joinColumnDataList = RelationshipUtils.buildJoinColumnDataList(
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
            log.debug("buildRelationship: field.getType()={}", field.getType());
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


    private PkGeneration buildPkGeneration(Field field) {
        GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
        if (generatedValue == null) {
            PkStrategy pkStrategy = dbConfiguration.getDbJdbc().findPkStrategy(PkGenerationType.PLAIN);
            PkGeneration pkGeneration = new PkGeneration();
            pkGeneration.setPkStrategy(pkStrategy);
            return pkGeneration;
        }

        PkGenerationType pkGenerationType = decodePkGenerationType(generatedValue.strategy());
        PkStrategy pkStrategy = dbConfiguration.getDbJdbc().findPkStrategy(pkGenerationType);
        log.debug("buildPkGeneration: dbConfiguration.getDbJdbc()={}", dbConfiguration.getDbJdbc());
        log.debug("buildPkGeneration: pkStrategy={}", pkStrategy);
        PkGeneration pkGeneration = new PkGeneration();
        pkGeneration.setPkStrategy(pkStrategy);
        pkGeneration.setGenerator(generatedValue.generator());

        SequenceGenerator sequenceGenerator = field.getAnnotation(SequenceGenerator.class);
        if (pkStrategy == PkStrategy.SEQUENCE) {
            if (sequenceGenerator != null) {
                if (generatedValue.strategy() != GenerationType.SEQUENCE) {
                    throw new IllegalArgumentException("Generated Value Strategy must be 'SEQUENCE'");
                }

                if (sequenceGenerator.name() != null && generatedValue.generator() != null
                        && !sequenceGenerator.name().equals(generatedValue.generator())) {
                    throw new IllegalArgumentException(
                            "Generator '" + generatedValue.generator() + "' not found"); ///
                }

                PkSequenceGenerator pkSequenceGenerator = new PkSequenceGenerator();
                pkSequenceGenerator.setSequenceName(sequenceGenerator.sequenceName());
                pkSequenceGenerator.setSchema(sequenceGenerator.schema());
                pkSequenceGenerator.setAllocationSize(sequenceGenerator.allocationSize());
                pkSequenceGenerator.setCatalog(sequenceGenerator.schema());
                pkSequenceGenerator.setInitialValue(sequenceGenerator.initialValue());
                pkGeneration.setPkSequenceGenerator(pkSequenceGenerator);
            }
        }
//	    else {
//		PkSequenceGenerator pkSequenceGenerator = generateDefaultSequence(tableName);
//		pkGeneration.setPkSequenceGenerator(pkSequenceGenerator);
//	    }

        return pkGeneration;
    }

    private PkSequenceGenerator generateDefaultSequenceGenerator(String tableName) {
        PkSequenceGenerator pkSequenceGenerator = new PkSequenceGenerator();
        pkSequenceGenerator.setSequenceName(tableName.toUpperCase() + "_PK_SEQ");
//		pkSequenceGenerator.setSchema(sequenceGenerator.schema());
        pkSequenceGenerator.setAllocationSize(Integer.valueOf(1));
//		pkSequenceGenerator.setCatalog(sequenceGenerator.schema());
        pkSequenceGenerator.setInitialValue(1);
        return pkSequenceGenerator;
    }

    private PkGenerationType decodePkGenerationType(GenerationType generationType) {
        if (generationType == GenerationType.AUTO) {
            return PkGenerationType.AUTO;
        }

        if (generationType == GenerationType.IDENTITY) {
            return PkGenerationType.IDENTITY;
        }

        if (generationType == GenerationType.SEQUENCE) {
            return PkGenerationType.SEQUENCE;
        }

        if (generationType == GenerationType.TABLE) {
            return PkGenerationType.TABLE;
        }

        return null;
    }

    private void finalizeRelationships(
            MetaEntity entity,
            Map<String, MetaEntity> entities,
            List<RelationshipMetaAttribute> attributes) {
        entity.getEmbeddables().forEach(embeddable -> {
            finalizeRelationships(embeddable, entities, embeddable.getRelationshipAttributes());
        });

        for (RelationshipMetaAttribute a : attributes) {
            log.debug("finalizeRelationships: a={}", a);
            Relationship relationship = a.getRelationship();
            if (relationship instanceof OneToOneRelationship) {
                MetaEntity toEntity = entities.get(a.getType().getName());
                log.debug("finalizeRelationships: OneToOne toEntity={}", toEntity);
                if (toEntity == null) {
                    throw new IllegalArgumentException(
                            "One to One entity not found (" + a.getType().getName() + ")");
                }

                OneToOneRelationship oneToOne = (OneToOneRelationship) relationship;
                a.setRelationship(
                        oneToOneHelper.finalizeRelationship(oneToOne, a, entity, toEntity, dbConfiguration));
            } else if (relationship instanceof ManyToOneRelationship) {
                MetaEntity toEntity = entities.get(a.getType().getName());
                log.debug("finalizeRelationships: ManyToOne toEntity={}", toEntity);
                if (toEntity == null) {
                    throw new IllegalArgumentException(
                            "Many to One entity not found (" + a.getType().getName() + ")");
                }

                ManyToOneRelationship manyToOne = (ManyToOneRelationship) relationship;
                a.setRelationship(
                        manyToOneHelper.finalizeRelationship(manyToOne, a, entity, toEntity, dbConfiguration));
            } else if (relationship instanceof OneToManyRelationship) {
                MetaEntity toEntity = entities.get(relationship.getTargetEntityClass().getName());
                log.debug("finalizeRelationships: OneToMany toEntity={}; a.getType().getName()={}",
                        toEntity,
                        a.getType().getName());
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
                log.debug("finalizeRelationships: ManyToMany toEntity={}; a.getType().getName()={}",
                        toEntity,
                        a.getType().getName());
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

    private void printAttributes(Map<String, MetaEntity> entities) {
        for (Map.Entry<String, MetaEntity> entry : entities.entrySet()) {
            MetaEntity entity = entry.getValue();
            List<AbstractMetaAttribute> attributes = entity.getAttributes();
            entity.getAttributes().forEach(a -> log.debug("printAttributes: a={}", a));
            entity.getRelationshipAttributes()
                    .forEach(a -> log.debug("printAttributes: a.getRelationship()={}", a.getRelationship()));
        }
    }

}
