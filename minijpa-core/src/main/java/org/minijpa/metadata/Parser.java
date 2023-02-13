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
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.Basic;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

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
import org.minijpa.jpa.db.QueryResultMapping;
import org.minijpa.jpa.model.BasicAttributePk;
import org.minijpa.jpa.model.EmbeddedPk;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.Pk;
import org.minijpa.jpa.model.relationship.ManyToManyRelationship;
import org.minijpa.jpa.model.relationship.ManyToOneRelationship;
import org.minijpa.jpa.model.relationship.OneToManyRelationship;
import org.minijpa.jpa.model.relationship.OneToOneRelationship;
import org.minijpa.jpa.model.relationship.Relationship;
import org.minijpa.metadata.enhancer.EnhAttribute;
import org.minijpa.metadata.enhancer.EnhEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Parser {

    private final Logger LOG = LoggerFactory.getLogger(Parser.class);
    private final DbConfiguration dbConfiguration;
    private final OneToOneHelper oneToOneHelper = new OneToOneHelper();
    private final ManyToOneHelper manyToOneHelper = new ManyToOneHelper();
    private final OneToManyHelper oneToManyHelper = new OneToManyHelper();
    private final ManyToManyHelper manyToManyHelper = new ManyToManyHelper();
    private final JpaParser jpaParser = new JpaParser();

    public Parser(DbConfiguration dbConfiguration) {
        super();
        this.dbConfiguration = dbConfiguration;
    }

    public void fillRelationships(Map<String, MetaEntity> entities) {
        entities.forEach((k, v) -> {
            LOG.debug("fillRelationships: v.getName()={}", v.getName());
            v.getBasicAttributes().forEach(a -> LOG.debug("fillRelationships: ba a.getName()={}", a.getName()));
        });

        finalizeRelationships(entities);
        entities.forEach((k, v) -> {
            LOG.debug("fillRelationships: 2 v.getName()={}", v.getName());
            v.getBasicAttributes().forEach(a -> LOG.debug("fillRelationships: 2 ba a.getName()={}", a.getName()));
        });

        printAttributes(entities);
    }

    public Optional<Map<String, QueryResultMapping>> parseSqlResultSetMappings(Map<String, MetaEntity> entities) {
        Map<String, QueryResultMapping> map = new HashMap<>();
        for (Map.Entry<String, MetaEntity> entry : entities.entrySet()) {
            MetaEntity metaEntity = entry.getValue();
            Optional<Map<String, QueryResultMapping>> optional = jpaParser
                    .parseQueryResultMappings(metaEntity.getEntityClass(), entities);
            if (optional.isPresent()) {
                // checks for uniqueness
                for (Map.Entry<String, QueryResultMapping> e : optional.get().entrySet()) {
                    if (map.containsKey(e.getKey()))
                        throw new IllegalStateException("@SqlResultSetMapping '" + e.getKey() + "' already declared");
                }

                map.putAll(optional.get());
            }
        }

        if (map.isEmpty())
            return Optional.empty();

        return Optional.of(map);
    }

    public MetaEntity parse(EnhEntity enhEntity, Collection<MetaEntity> parsedEntities) throws Exception {
        Optional<MetaEntity> optional = parsedEntities.stream()
                .filter(e -> e.getEntityClass().getName().equals(enhEntity.getClassName())).findFirst();
        if (optional.isPresent())
            return optional.get();

        Class<?> c = Class.forName(enhEntity.getClassName());
        Entity ec = c.getAnnotation(Entity.class);
        if (ec == null)
            throw new Exception("@Entity annotation not found: '" + c.getName() + "'");

        String name = c.getSimpleName();
        if (ec.name() != null && !ec.name().trim().isEmpty())
            name = ec.name();

        String tableName = c.getSimpleName();
        Table table = c.getAnnotation(Table.class);
        if (table != null && table.name() != null && table.name().trim().length() > 0)
            tableName = table.name();

        LOG.debug("Reading '" + enhEntity.getClassName() + "' attributes...");
        List<MetaAttribute> attributes = parseAttributes(enhEntity, Optional.empty());
        List<MetaEntity> embeddables = parseEmbeddables(enhEntity, parsedEntities, Optional.empty());
        MetaEntity mappedSuperclassEntity = null;
        LOG.debug("parse: enhEntity.getMappedSuperclass()={}", enhEntity.getMappedSuperclass());
        if (enhEntity.getMappedSuperclass() != null)
            LOG.debug("parse: enhEntity.getMappedSuperclass().getClassName()={}",
                    enhEntity.getMappedSuperclass().getClassName());

        if (enhEntity.getMappedSuperclass() != null) {
            optional = parsedEntities.stream().filter(e -> e.getMappedSuperclassEntity() != null)
                    .filter(e -> e.getMappedSuperclassEntity().getEntityClass().getName()
                            .equals(enhEntity.getMappedSuperclass().getClassName()))
                    .findFirst();

            if (optional.isPresent())
                mappedSuperclassEntity = optional.get().getMappedSuperclassEntity();
            else
                mappedSuperclassEntity = parseMappedSuperclass(enhEntity.getMappedSuperclass(), parsedEntities);

            List<MetaAttribute> msAttributes = mappedSuperclassEntity.getAttributes();
            attributes.addAll(msAttributes);
        }

        LOG.debug("Getting '" + c.getName() + "' Id...");
        Pk id = null;
        if (mappedSuperclassEntity != null)
            id = mappedSuperclassEntity.getId();
        else {
            Optional<MetaAttribute> optionalId = attributes.stream().filter(a -> a.isId()).findFirst();
            if (optionalId.isPresent()) {
                Field field = c.getDeclaredField(optionalId.get().getName());
                PkGeneration gv = buildPkGeneration(field);
                if (gv.getPkStrategy() == PkStrategy.SEQUENCE && gv.getPkSequenceGenerator() == null) {
                    gv.setPkSequenceGenerator(generateDefaultSequenceGenerator(tableName));
                }

                id = new BasicAttributePk(optionalId.get(), gv);
                attributes.remove(optionalId.get());
            } else {
                Optional<MetaEntity> optionalME = embeddables.stream().filter(e -> e.isEmbeddedId()).findFirst();
                if (optionalME.isEmpty())
                    throw new Exception("@Id or @EmbeddedId annotation not found: '" + c.getName() + "'");

                id = new EmbeddedPk(optionalME.get());
                embeddables.remove(optionalME.get());
            }
        }

        Method modificationAttributeReadMethod = null;
        try {
            modificationAttributeReadMethod = c.getMethod(enhEntity.getModificationAttributeGetMethod());
        } catch (NoSuchMethodException e1) {
            LOG.error("parse: special method '" + enhEntity.getModificationAttributeGetMethod() + "' not found");
            throw e1;
        } catch (Exception e1) {
            LOG.error(e1.getMessage());
            throw e1;
        }

        Optional<Method> lazyLoadedAttributeReadMethod = Optional.empty();
        if (enhEntity.getLazyLoadedAttributeGetMethod().isPresent())
            lazyLoadedAttributeReadMethod = Optional.of(c.getMethod(enhEntity.getLazyLoadedAttributeGetMethod().get()));

        Optional<Method> joinColumnPostponedUpdateAttributeReadMethod = Optional.empty();
        if (enhEntity.getJoinColumnPostponedUpdateAttributeGetMethod().isPresent())
            joinColumnPostponedUpdateAttributeReadMethod = Optional
                    .of(c.getMethod(enhEntity.getJoinColumnPostponedUpdateAttributeGetMethod().get()));

        Method lockTypeAttributeReadMethod = c.getMethod(enhEntity.getLockTypeAttributeGetMethod().get());
        Method lockTypeAttributeWriteMethod = c.getMethod(enhEntity.getLockTypeAttributeSetMethod().get(),
                LockType.class);
        Method entityStatusAttributeReadMethod = c.getMethod(enhEntity.getEntityStatusAttributeGetMethod().get());
        Method entityStatusAttributeWriteMethod = c.getMethod(enhEntity.getEntityStatusAttributeSetMethod().get(),
                EntityStatus.class);
        List<MetaAttribute> basicAttributes = attributes.stream().filter(a -> a.isBasic()).collect(Collectors.toList());
        List<MetaAttribute> relationshipAttributes = attributes.stream().filter(a -> a.getRelationship() != null)
                .collect(Collectors.toList());
        return new MetaEntity.Builder().withEntityClass(c).withName(name).withTableName(tableName)
                .withId(id).withAttributes(attributes)
                .withBasicAttributes(Collections.unmodifiableList(basicAttributes))
                .withRelationshipAttributes(relationshipAttributes).withEmbeddables(embeddables)
                .withMappedSuperclassEntity(mappedSuperclassEntity)
                .withModificationAttributeReadMethod(modificationAttributeReadMethod)
                .withLazyLoadedAttributeReadMethod(lazyLoadedAttributeReadMethod)
                .withJoinColumnPostponedUpdateAttributeReadMethod(joinColumnPostponedUpdateAttributeReadMethod)
                .withLockTypeAttributeReadMethod(Optional.of(lockTypeAttributeReadMethod))
                .withLockTypeAttributeWriteMethod(Optional.of(lockTypeAttributeWriteMethod))
                .withEntityStatusAttributeReadMethod(Optional.of(entityStatusAttributeReadMethod))
                .withEntityStatusAttributeWriteMethod(Optional.of(entityStatusAttributeWriteMethod)).build();
    }

    private MetaEntity parseEmbeddable(String parentClassName, EnhAttribute enhAttribute, EnhEntity enhEntity,
            Collection<MetaEntity> parsedEntities, Optional<String> parentPath) throws Exception {
        Optional<MetaEntity> optional = parsedEntities.stream()
                .filter(e -> e.getEntityClass().getName().equals(enhEntity.getClassName())).findFirst();
        if (optional.isPresent())
            return optional.get();

        Class<?> c = Class.forName(enhEntity.getClassName());
        Embeddable ec = c.getAnnotation(Embeddable.class);
        if (ec == null)
            throw new Exception("@Embeddable annotation not found: '" + c.getName() + "'");

        LOG.debug("Reading '" + enhEntity.getClassName() + "' attributes...");
        String path = parentPath.isEmpty() ? enhAttribute.getName() : parentPath.get() + "." + enhAttribute.getName();
        List<MetaAttribute> attributes = parseAttributes(enhEntity, Optional.of(path));
        List<MetaEntity> embeddables = parseEmbeddables(enhEntity, parsedEntities, Optional.of(path));

        Method modificationAttributeReadMethod = null;
        if (enhEntity.getModificationAttributeGetMethod() != null)
            modificationAttributeReadMethod = c.getMethod(enhEntity.getModificationAttributeGetMethod());

        Optional<Method> lazyLoadedAttributeReadMethod = Optional.empty();
        if (enhEntity.getLazyLoadedAttributeGetMethod().isPresent())
            lazyLoadedAttributeReadMethod = Optional.of(c.getMethod(enhEntity.getLazyLoadedAttributeGetMethod().get()));

        Optional<Method> joinColumnPostponedUpdateAttributeReadMethod = Optional.empty();
        if (enhEntity.getJoinColumnPostponedUpdateAttributeGetMethod().isPresent())
            joinColumnPostponedUpdateAttributeReadMethod = Optional
                    .of(c.getMethod(enhEntity.getJoinColumnPostponedUpdateAttributeGetMethod().get()));

        Class<?> attributeClass = Class.forName(enhAttribute.getClassName());
        Class<?> parentClass = Class.forName(parentClassName);
        Method readMethod = parentClass.getMethod(enhAttribute.getGetMethod());
        Method writeMethod = parentClass.getMethod(enhAttribute.getSetMethod(), attributeClass);

        Field field = parentClass.getDeclaredField(enhAttribute.getName());
        boolean id = field.getAnnotation(EmbeddedId.class) != null;

        List<MetaAttribute> basicAttributes = attributes.stream().filter(a -> a.isBasic()).collect(Collectors.toList());
        List<MetaAttribute> relationshipAttributes = attributes.stream().filter(a -> a.getRelationship() != null)
                .collect(Collectors.toList());
        return new MetaEntity.Builder().withEntityClass(c).withName(enhAttribute.getName()).isEmbeddedId(id)
                .withAttributes(attributes).withBasicAttributes(Collections.unmodifiableList(basicAttributes))
                .withRelationshipAttributes(relationshipAttributes).withEmbeddables(embeddables)
                .withReadMethod(readMethod).withWriteMethod(writeMethod).withPath(path)
                .withModificationAttributeReadMethod(modificationAttributeReadMethod)
                .withLazyLoadedAttributeReadMethod(lazyLoadedAttributeReadMethod)
                .withJoinColumnPostponedUpdateAttributeReadMethod(joinColumnPostponedUpdateAttributeReadMethod).build();
    }

    private MetaEntity parseMappedSuperclass(EnhEntity enhEntity, Collection<MetaEntity> parsedEntities)
            throws Exception {
        Class<?> c = Class.forName(enhEntity.getClassName());
        MappedSuperclass ec = c.getAnnotation(MappedSuperclass.class);
        if (ec == null)
            throw new Exception("@MappedSuperclass annotation not found: '" + c.getName() + "'");

        LOG.debug("Reading mapped superclass '" + enhEntity.getClassName() + "' attributes...");
        List<MetaAttribute> attributes = parseAttributes(enhEntity, Optional.empty());
        List<MetaEntity> embeddables = parseEmbeddables(enhEntity, parsedEntities, Optional.empty());

        Pk pk = null;
        Optional<MetaAttribute> optionalId = attributes.stream().filter(a -> a.isId()).findFirst();
        if (optionalId.isPresent()) {
            Field field = c.getDeclaredField(optionalId.get().getName());
            PkGeneration gv = buildPkGeneration(field);
            if (gv.getPkStrategy() == PkStrategy.SEQUENCE && gv.getPkSequenceGenerator() == null) {
                gv.setPkSequenceGenerator(generateDefaultSequenceGenerator(c.getSimpleName().toUpperCase()));
            }

            pk = new BasicAttributePk(optionalId.get(), gv);
            attributes.remove(optionalId.get());
        } else {
            Optional<MetaEntity> optionalME = embeddables.stream().filter(e -> e.isEmbeddedId()).findFirst();
            if (optionalME.isEmpty())
                throw new Exception(
                        "@Id or @EmbeddedId annotation not found in mapped superclass: '" + c.getName() + "'");

            pk = new EmbeddedPk(optionalME.get());
            embeddables.remove(optionalME.get());
        }

        Method modificationAttributeReadMethod = c.getMethod(enhEntity.getModificationAttributeGetMethod());
        Optional<Method> lazyLoadedAttributeReadMethod = Optional.empty();
        if (enhEntity.getLazyLoadedAttributeGetMethod().isPresent())
            lazyLoadedAttributeReadMethod = Optional.of(c.getMethod(enhEntity.getLazyLoadedAttributeGetMethod().get()));

        Optional<Method> joinColumnPostponedUpdateAttributeReadMethod = Optional.empty();
        if (enhEntity.getJoinColumnPostponedUpdateAttributeGetMethod().isPresent())
            joinColumnPostponedUpdateAttributeReadMethod = Optional
                    .of(c.getMethod(enhEntity.getJoinColumnPostponedUpdateAttributeGetMethod().get()));

        List<MetaAttribute> basicAttributes = attributes.stream().filter(a -> a.isBasic()).collect(Collectors.toList());
        List<MetaAttribute> relationshipAttributes = attributes.stream().filter(a -> a.getRelationship() != null)
                .collect(Collectors.toList());
        return new MetaEntity.Builder().withEntityClass(c).withId(pk).withAttributes(attributes)
                .withBasicAttributes(Collections.unmodifiableList(basicAttributes))
                .withRelationshipAttributes(relationshipAttributes).withEmbeddables(embeddables)
                .withModificationAttributeReadMethod(modificationAttributeReadMethod)
                .withLazyLoadedAttributeReadMethod(lazyLoadedAttributeReadMethod)
                .withJoinColumnPostponedUpdateAttributeReadMethod(joinColumnPostponedUpdateAttributeReadMethod).build();
    }

    private List<MetaAttribute> parseAttributes(EnhEntity enhEntity, Optional<String> parentPath) throws Exception {
        List<MetaAttribute> attributes = new ArrayList<>();
        for (EnhAttribute enhAttribute : enhEntity.getEnhAttributes()) {
            LOG.debug("readAttributes: enhAttribute.getName()={}", enhAttribute.getName());
            if (enhAttribute.isEmbedded())
                continue;

            MetaAttribute attribute = parseAttribute(enhEntity.getClassName(), enhAttribute, parentPath);
            attributes.add(attribute);
        }

        return attributes;
    }

    private List<MetaEntity> parseEmbeddables(EnhEntity enhEntity, Collection<MetaEntity> parsedEntities,
            Optional<String> parentPath) throws Exception {
        List<MetaEntity> metaEntities = new ArrayList<>();
        for (EnhAttribute enhAttribute : enhEntity.getEnhAttributes()) {
            if (!enhAttribute.isEmbedded())
                continue;

            MetaEntity metaEntity = parseEmbeddable(enhEntity.getClassName(), enhAttribute,
                    enhAttribute.getEmbeddedEnhEntity(), parsedEntities, parentPath);
            metaEntities.add(metaEntity);
        }

        return metaEntities;
    }

    private Integer findSqlType(Class<?> attributeClass, Enumerated enumerated) {
        Integer sqlType = JdbcTypes.sqlTypeFromClass(attributeClass);
        if (sqlType != Types.NULL)
            return sqlType;

        return Types.NULL;
    }

    private Optional<Class<?>> enumerationType(Class<?> attributeClass, Enumerated enumerated) {
        if (attributeClass.isEnum() && enumerated == null)
            return Optional.of(Integer.class);

        if (attributeClass.isEnum() && enumerated != null) {
            if (enumerated.value() == null)
                return Optional.of(Integer.class);

            if (enumerated.value() == EnumType.STRING)
                return Optional.of(String.class);

            if (enumerated.value() == EnumType.ORDINAL)
                return Optional.of(Integer.class);
        }

        return Optional.empty();
    }

    private MetaAttribute parseAttribute(String parentClassName, EnhAttribute enhAttribute, Optional<String> parentPath)
            throws Exception {
        String columnName = enhAttribute.getName();
        Class<?> c = Class.forName(parentClassName);
        LOG.debug("Reading attribute '" + columnName + "'");
        Field field = c.getDeclaredField(enhAttribute.getName());
        Class<?> attributeClass = null;
        if (enhAttribute.isPrimitiveType())
            attributeClass = JavaTypes.getClass(enhAttribute.getClassName());
        else
            attributeClass = Class.forName(enhAttribute.getClassName());

        LOG.debug("readAttribute: attributeClass={}", attributeClass);
        Method readMethod = c.getMethod(enhAttribute.getGetMethod());
        Method writeMethod = c.getMethod(enhAttribute.getSetMethod(), attributeClass);
        Column column = field.getAnnotation(Column.class);

        Id idAnnotation = field.getAnnotation(Id.class);
        boolean nullableColumn = !attributeClass.isPrimitive() && !enhAttribute.isParentEmbeddedId()
                && (idAnnotation == null);
        Optional<DDLData> ddlData;
        if (column != null) {
            String cn = column.name();
            if (cn != null && cn.trim().length() > 0)
                columnName = cn;

            ddlData = buildDDLData(column, nullableColumn);
        } else
            ddlData = Optional.of(new DDLData(Optional.empty(), Optional.of(255), Optional.of(0), Optional.of(0),
                    Optional.of(nullableColumn)));

        Enumerated enumerated = field.getAnnotation(Enumerated.class);
        LOG.debug("readAttribute: enumerated={}", enumerated);
        Optional<Class<?>> enumerationType = enumerationType(attributeClass, enumerated);
        LOG.debug("readAttribute: dbConfiguration={}", dbConfiguration);
        Class<?> readWriteType = findDatabaseType(field, attributeClass, enumerationType);
        LOG.debug("readAttribute: readWriteType={}", readWriteType);
        Integer sqlType = findSqlType(readWriteType, enumerated);
        LOG.debug("readAttribute: sqlType={}", sqlType);
        LOG.debug("readAttribute: parentPath.isEmpty() ={}", parentPath.isEmpty());
        String path = parentPath.isEmpty() ? enhAttribute.getName() : parentPath.get() + "." + enhAttribute.getName();
        LOG.debug("readAttribute: path={}", path);
        LOG.debug("readAttribute: idAnnotation={}", idAnnotation);
        if (idAnnotation != null) {
            AttributeMapper<?, ?> attributeMapper = dbConfiguration.getDbTypeMapper().attributeMapper(attributeClass,
                    readWriteType);
            LOG.debug("readAttribute: id attributeMapper={}", attributeMapper);
            Optional<AttributeMapper> optionalAM = Optional.empty();
            if (attributeMapper != null)
                optionalAM = Optional.of(attributeMapper);

            MetaAttribute.Builder builder = new MetaAttribute.Builder(enhAttribute.getName()).withColumnName(columnName)
                    .withType(attributeClass).withReadWriteDbType(readWriteType).withReadMethod(readMethod)
                    .withWriteMethod(writeMethod).isId(true).withSqlType(sqlType).withJavaMember(field).isBasic(true)
                    .withPath(path).withDDLData(ddlData).withAttributeMapper(optionalAM);

            return builder.build();
        }

        boolean isCollection = CollectionUtils.isCollectionClass(attributeClass);
        Class<?> collectionImplementationClass = null;
        if (isCollection)
            collectionImplementationClass = CollectionUtils.findCollectionImplementationClass(attributeClass);

        boolean version = field.getAnnotation(Version.class) != null;

        MetaAttribute.Builder builder = new MetaAttribute.Builder(enhAttribute.getName()).withColumnName(columnName)
                .withType(attributeClass).withReadWriteDbType(readWriteType).withReadMethod(readMethod)
                .withWriteMethod(writeMethod).withSqlType(sqlType).isCollection(isCollection).withJavaMember(field)
                .withCollectionImplementationClass(collectionImplementationClass).isVersion(version)
                .isBasic(AttributeUtil.isBasicAttribute(attributeClass)).withPath(path).withDDLData(ddlData);

        AttributeMapper<?, ?> attributeMapper = dbConfiguration.getDbTypeMapper().attributeMapper(attributeClass,
                readWriteType);
        if (attributeMapper != null)
            builder.withAttributeMapper(Optional.of(attributeMapper));

        Optional<Relationship> relationship = buildRelationship(field);
        if (relationship.isPresent()) {
            builder.withRelationship(relationship.get());
            Optional<Method> joinColumnReadMethod = enhAttribute.getJoinColumnGetMethod().isPresent()
                    ? Optional.of(c.getMethod(enhAttribute.getJoinColumnGetMethod().get()))
                    : Optional.empty();
            Optional<Method> joinColumnWriteMethod = enhAttribute.getJoinColumnSetMethod().isPresent()
                    ? Optional.of(c.getMethod(enhAttribute.getJoinColumnSetMethod().get(), Object.class))
                    : Optional.empty();
            builder.withJoinColumnReadMethod(joinColumnReadMethod).withJoinColumnWriteMethod(joinColumnWriteMethod);
        }

        // Basic annotation
        Basic basic = field.getAnnotation(Basic.class);
        if (basic != null) {
            if (!basic.optional())
                builder.isNullable(false);
        }

        MetaAttribute attribute = builder.build();
        LOG.debug("readAttribute: attribute: " + attribute);
        return attribute;
    }

    private Class<?> findDatabaseType(Field field, Class<?> attributeClass, Optional<Class<?>> enumerationType) {
        Optional<Class<?>> optional = temporalType(field, attributeClass);
        LOG.debug("findDatabaseType: optional={}", optional);
        if (optional.isPresent())
            return dbConfiguration.getDbTypeMapper().databaseType(optional.get(), enumerationType);

        return dbConfiguration.getDbTypeMapper().databaseType(attributeClass, enumerationType);
    }

    private Optional<Class<?>> temporalType(Field field, Class<?> attributeClass) {
        if (attributeClass != java.util.Date.class && attributeClass != Calendar.class)
            return Optional.empty();

        Temporal temporal = field.getAnnotation(Temporal.class);
        if (temporal == null)
            return Optional.empty();

        TemporalType temporalType = temporal.value();
        if (temporalType == null)
            return Optional.empty();

        if (temporalType == TemporalType.DATE)
            return Optional.of(java.sql.Date.class);

        if (temporalType == TemporalType.TIME)
            return Optional.of(java.sql.Time.class);

        return Optional.of(java.sql.Timestamp.class);
    }

    private Optional<DDLData> buildDDLData(Column column, boolean nullableColumn) {
        Optional<String> columnDefinition = Optional.empty();
        String cd = column.columnDefinition();
        if (cd != null && !cd.trim().isEmpty())
            columnDefinition = Optional.of(cd.trim());

        Optional<Integer> length = Optional.of(column.length());

        Optional<Integer> precision = Optional.empty();
        Integer p = column.precision();
        if (p != 0)
            precision = Optional.of(p);

        Optional<Integer> scale = Optional.empty();
        Integer s = column.scale();
        if (s != 0)
            scale = Optional.of(s);

        if (columnDefinition.isEmpty() && length.isEmpty() && precision.isEmpty() && scale.isEmpty())
            return Optional.empty();

        Optional<Boolean> nullable = !nullableColumn ? Optional.of(Boolean.FALSE) : Optional.of(column.nullable());

        return Optional.of(new DDLData(columnDefinition, length, precision, scale, nullable));
    }

    private Optional<Relationship> buildRelationship(Field field) {
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

        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        JoinColumns joinColumns = field.getAnnotation(JoinColumns.class);
        Optional<JoinColumnDataList> joinColumnDataList = RelationshipUtils.buildJoinColumnDataList(joinColumn,
                joinColumns);
        if (oneToOne != null)
            return Optional.of(oneToOneHelper.createOneToOne(oneToOne, joinColumnDataList));

        if (manyToOne != null)
            return Optional.of(manyToOneHelper.createManyToOne(manyToOne, joinColumnDataList));

        if (oneToMany != null) {
            Class<?> collectionClass = null;
            Class<?> targetEntity = oneToMany.targetEntity();
            if (targetEntity == null || targetEntity == Void.TYPE)
                targetEntity = ReflectionUtil.findTargetEntity(field);

            JoinTable joinTable = field.getAnnotation(JoinTable.class);
            return Optional.of(oneToManyHelper.createOneToMany(oneToMany, collectionClass, targetEntity, joinTable,
                    joinColumnDataList));
        }

        if (manyToMany != null) {
            Class<?> collectionClass = null;
            Class<?> targetEntity = manyToMany.targetEntity();
            if (targetEntity == null || targetEntity == Void.TYPE)
                targetEntity = ReflectionUtil.findTargetEntity(field);

            JoinTable joinTable = field.getAnnotation(JoinTable.class);
            return Optional.of(manyToManyHelper.createManyToMany(manyToMany, collectionClass, targetEntity, joinTable,
                    joinColumnDataList));
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
        LOG.debug("buildPkGeneration: dbConfiguration.getDbJdbc()={}", dbConfiguration.getDbJdbc());
        LOG.debug("buildPkGeneration: pkStrategy={}", pkStrategy);
        PkGeneration pkGeneration = new PkGeneration();
        pkGeneration.setPkStrategy(pkStrategy);
        pkGeneration.setGenerator(generatedValue.generator());

        SequenceGenerator sequenceGenerator = field.getAnnotation(SequenceGenerator.class);
        if (pkStrategy == PkStrategy.SEQUENCE)
            if (sequenceGenerator != null) {
                if (generatedValue.strategy() != GenerationType.SEQUENCE)
                    throw new IllegalArgumentException("Generated Value Strategy must be 'SEQUENCE'");

                if (sequenceGenerator.name() != null && generatedValue.generator() != null
                        && !sequenceGenerator.name().equals(generatedValue.generator()))
                    throw new IllegalArgumentException("Generator '" + generatedValue.generator() + "' not found"); ///

                PkSequenceGenerator pkSequenceGenerator = new PkSequenceGenerator();
                pkSequenceGenerator.setSequenceName(sequenceGenerator.sequenceName());
                pkSequenceGenerator.setSchema(sequenceGenerator.schema());
                pkSequenceGenerator.setAllocationSize(sequenceGenerator.allocationSize());
                pkSequenceGenerator.setCatalog(sequenceGenerator.schema());
                pkSequenceGenerator.setInitialValue(sequenceGenerator.initialValue());
                pkGeneration.setPkSequenceGenerator(pkSequenceGenerator);
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
        if (generationType == GenerationType.AUTO)
            return PkGenerationType.AUTO;

        if (generationType == GenerationType.IDENTITY)
            return PkGenerationType.IDENTITY;

        if (generationType == GenerationType.SEQUENCE)
            return PkGenerationType.SEQUENCE;

        if (generationType == GenerationType.TABLE)
            return PkGenerationType.TABLE;

        return null;
    }

    private void finalizeRelationships(MetaEntity entity, Map<String, MetaEntity> entities,
            List<MetaAttribute> attributes) {
        entity.getEmbeddables().forEach(embeddable -> {
            finalizeRelationships(embeddable, entities, embeddable.getAttributes());
        });

        for (MetaAttribute a : attributes) {
            LOG.debug("finalizeRelationships: a={}", a);
            Relationship relationship = a.getRelationship();
            if (relationship == null)
                continue;

            if (relationship instanceof OneToOneRelationship) {
                MetaEntity toEntity = entities.get(a.getType().getName());
                LOG.debug("finalizeRelationships: OneToOne toEntity={}", toEntity);
                if (toEntity == null)
                    throw new IllegalArgumentException("One to One entity not found (" + a.getType().getName() + ")");

                OneToOneRelationship oneToOne = (OneToOneRelationship) relationship;
                a.setRelationship(oneToOneHelper.finalizeRelationship(oneToOne, a, entity, toEntity, dbConfiguration));
            } else if (relationship instanceof ManyToOneRelationship) {
                MetaEntity toEntity = entities.get(a.getType().getName());
                LOG.debug("finalizeRelationships: ManyToOne toEntity={}", toEntity);
                if (toEntity == null)
                    throw new IllegalArgumentException("Many to One entity not found (" + a.getType().getName() + ")");

                ManyToOneRelationship manyToOne = (ManyToOneRelationship) relationship;
                a.setRelationship(
                        manyToOneHelper.finalizeRelationship(manyToOne, a, entity, toEntity, dbConfiguration));
            } else if (relationship instanceof OneToManyRelationship) {
                MetaEntity toEntity = entities.get(relationship.getTargetEntityClass().getName());
                LOG.debug("finalizeRelationships: OneToMany toEntity={}; a.getType().getName()=", toEntity,
                        a.getType().getName());
                if (toEntity == null)
                    throw new IllegalArgumentException("One to Many target entity not found ("
                            + relationship.getTargetEntityClass().getName() + ")");

                OneToManyRelationship oneToMany = (OneToManyRelationship) relationship;
                OneToManyRelationship otm = oneToManyHelper.finalizeRelationship(oneToMany, a, entity, toEntity,
                        dbConfiguration);
                a.setRelationship(otm);
            } else if (relationship instanceof ManyToManyRelationship) {
                MetaEntity toEntity = entities.get(relationship.getTargetEntityClass().getName());
                LOG.debug("finalizeRelationships: ManyToMany toEntity={}; a.getType().getName()=", toEntity,
                        a.getType().getName());
                if (toEntity == null)
                    throw new IllegalArgumentException("Many to Many target entity not found ("
                            + relationship.getTargetEntityClass().getName() + ")");

                ManyToManyRelationship manyToMany = (ManyToManyRelationship) relationship;
                ManyToManyRelationship otm = manyToManyHelper.finalizeRelationship(manyToMany, a, entity, toEntity,
                        dbConfiguration, entities);
                a.setRelationship(otm);
            }
        }
    }

    /**
     * It's a post-processing step needed to complete entity data. For example,
     * filling out the 'join column' one to one relationships if missing.
     *
     * @param entities the list of all entities
     */
    private void finalizeRelationships(Map<String, MetaEntity> entities) {
        for (Map.Entry<String, MetaEntity> entry : entities.entrySet()) {
            MetaEntity entity = entry.getValue();
            List<MetaAttribute> attributes = entity.getAttributes();
            finalizeRelationships(entity, entities, attributes);
        }
    }

    private void printAttributes(Map<String, MetaEntity> entities) {
        for (Map.Entry<String, MetaEntity> entry : entities.entrySet()) {
            MetaEntity entity = entry.getValue();
            List<MetaAttribute> attributes = entity.getAttributes();
            for (int i = 0; i < attributes.size(); ++i) {
                MetaAttribute a = attributes.get(i);
                LOG.debug("printAttributes: a={}", a);
                if (a.getRelationship() != null)
                    LOG.debug("printAttributes: a.getRelationship()={}", a.getRelationship());
            }
        }
    }

}
