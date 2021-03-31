package org.minijpa.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;
import org.minijpa.jdbc.AttributeUtil;

import org.minijpa.jdbc.CollectionUtils;
import org.minijpa.jdbc.JdbcTypes;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.PkGeneration;
import org.minijpa.jdbc.PkGenerationType;
import org.minijpa.jdbc.PkSequenceGenerator;
import org.minijpa.jdbc.PkStrategy;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.mapper.JdbcAttributeMapper;
import org.minijpa.jdbc.relationship.ManyToManyRelationship;
import org.minijpa.jdbc.relationship.ManyToOneRelationship;
import org.minijpa.jdbc.relationship.OneToManyRelationship;
import org.minijpa.jdbc.relationship.OneToOneRelationship;
import org.minijpa.jdbc.relationship.Relationship;
import org.minijpa.metadata.enhancer.EnhAttribute;
import org.minijpa.metadata.enhancer.EnhEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Parser {

    private final Logger LOG = LoggerFactory.getLogger(Parser.class);
    private final DbConfiguration dbConfiguration;
    private final AliasGenerator aliasGenerator = new AliasGenerator();
    private final OneToOneHelper oneToOneHelper = new OneToOneHelper();
    private final ManyToOneHelper manyToOneHelper = new ManyToOneHelper();
    private final OneToManyHelper oneToManyHelper = new OneToManyHelper();
    private final ManyToManyHelper manyToManyHelper = new ManyToManyHelper();

    public Parser(DbConfiguration dbConfiguration) {
	super();
	this.dbConfiguration = dbConfiguration;
    }

    public void fillRelationships(Map<String, MetaEntity> entities) {
	finalizeRelationships(entities);
	printAttributes(entities);
    }

    public MetaEntity parse(EnhEntity enhEntity, Collection<MetaEntity> parsedEntities) throws Exception {
	Optional<MetaEntity> optional = parsedEntities.stream().
		filter(e -> e.getEntityClass().getName()
		.equals(enhEntity.getClassName()))
		.findFirst();
	if (optional.isPresent())
	    return optional.get();

	Class<?> c = Class.forName(enhEntity.getClassName());
	Entity ec = c.getAnnotation(Entity.class);
	if (ec == null)
	    throw new Exception("@Entity annotation not found: '" + c.getName() + "'");

	LOG.debug("Reading '" + enhEntity.getClassName() + "' attributes...");
	List<MetaAttribute> attributes = readAttributes(enhEntity, parsedEntities);
	MetaEntity mappedSuperclassEntity = null;
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
	    attributes.add(mappedSuperclassEntity.getId());
	}

	LOG.debug("Getting '" + c.getName() + "' Id...");
	MetaAttribute id = null;
	if (mappedSuperclassEntity != null)
	    id = mappedSuperclassEntity.getId();
	else {
	    Optional<MetaAttribute> optionalId = attributes.stream().filter(a -> a.isId()).findFirst();
	    if (!optionalId.isPresent())
		throw new Exception("@Id annotation not found: '" + c.getName() + "'");

	    id = optionalId.get();
	}

	String name = c.getSimpleName();
	if (ec.name() != null && !ec.name().trim().isEmpty())
	    name = ec.name();

	String tableName = c.getSimpleName();
	Table table = c.getAnnotation(Table.class);
	if (table != null && table.name() != null && table.name().trim().length() > 0)
	    tableName = table.name();

	String alias = aliasGenerator.calculateAlias(tableName, parsedEntities);
	attributes.remove(id);

	Method modificationAttributeReadMethod = c.getMethod(enhEntity.getModificationAttributeGetMethod());
	Optional<Method> lazyLoadedAttributeReadMethod = Optional.empty();
	if (enhEntity.getLazyLoadedAttributeGetMethod().isPresent())
	    lazyLoadedAttributeReadMethod = Optional.of(c.getMethod(enhEntity.getLazyLoadedAttributeGetMethod().get()));

	Method lockTypeAttributeReadMethod = c.getMethod(enhEntity.getLockTypeAttributeGetMethod().get());
	Method lockTypeAttributeWriteMethod = c.getMethod(enhEntity.getLockTypeAttributeSetMethod().get(), LockType.class);
	return new MetaEntity(c, name, tableName, alias, id, attributes, mappedSuperclassEntity,
		modificationAttributeReadMethod, lazyLoadedAttributeReadMethod,
		Optional.of(lockTypeAttributeReadMethod), Optional.of(lockTypeAttributeWriteMethod));
    }

    private MetaEntity parseEmbeddable(EnhEntity enhEntity, Collection<MetaEntity> parsedEntities) throws Exception {
	Optional<MetaEntity> optional = parsedEntities.stream().
		filter(e -> e.getEntityClass().getName()
		.equals(enhEntity.getClassName()))
		.findFirst();
	if (optional.isPresent())
	    return optional.get();

	Class<?> c = Class.forName(enhEntity.getClassName());
	Embeddable ec = c.getAnnotation(Embeddable.class);
	if (ec == null)
	    throw new Exception("@Embeddable annotation not found: '" + c.getName() + "'");

	LOG.debug("Reading '" + enhEntity.getClassName() + "' attributes...");
	List<MetaAttribute> attributes = readAttributes(enhEntity, parsedEntities);

	Method modificationAttributeReadMethod = null;
	if (enhEntity.getModificationAttributeGetMethod() != null)
	    modificationAttributeReadMethod = c.getMethod(enhEntity.getModificationAttributeGetMethod());

	Optional<Method> lazyLoadedAttributeReadMethod = Optional.empty();
	if (enhEntity.getLazyLoadedAttributeGetMethod().isPresent())
	    lazyLoadedAttributeReadMethod = Optional.of(c.getMethod(enhEntity.getLazyLoadedAttributeGetMethod().get()));

	return new MetaEntity(c, null, null, null, null, attributes, null, modificationAttributeReadMethod,
		lazyLoadedAttributeReadMethod, Optional.empty(), Optional.empty());
    }

    private MetaEntity parseMappedSuperclass(EnhEntity enhEntity, Collection<MetaEntity> parsedEntities)
	    throws Exception {
	Class<?> c = Class.forName(enhEntity.getClassName());
	MappedSuperclass ec = c.getAnnotation(MappedSuperclass.class);
	if (ec == null)
	    throw new Exception("@MappedSuperclass annotation not found: '" + c.getName() + "'");

	LOG.debug("Reading mapped superclass '" + enhEntity.getClassName() + "' attributes...");
	List<MetaAttribute> attributes = readAttributes(enhEntity, parsedEntities);

	Optional<MetaAttribute> optionalId = attributes.stream().filter(a -> a.isId()).findFirst();
	if (!optionalId.isPresent())
	    throw new Exception("@Id annotation not found in mapped superclass: '" + c.getName() + "'");

	MetaAttribute id = optionalId.get();
	attributes.remove(id);

	Method modificationAttributeReadMethod = c.getMethod(enhEntity.getModificationAttributeGetMethod());
	Optional<Method> lazyLoadedAttributeReadMethod = Optional.empty();
	if (enhEntity.getLazyLoadedAttributeGetMethod().isPresent())
	    lazyLoadedAttributeReadMethod = Optional.of(c.getMethod(enhEntity.getLazyLoadedAttributeGetMethod().get()));

	return new MetaEntity(c, null, null, null, optionalId.get(), attributes, null,
		modificationAttributeReadMethod, lazyLoadedAttributeReadMethod, Optional.empty(), Optional.empty());
    }

    private List<MetaAttribute> readAttributes(EnhEntity enhEntity, Collection<MetaEntity> parsedEntities) throws Exception {
	List<MetaAttribute> attributes = new ArrayList<>();
	for (EnhAttribute enhAttribute : enhEntity.getEnhAttributes()) {
	    MetaAttribute attribute = readAttribute(enhEntity.getClassName(), enhAttribute, parsedEntities);
	    attributes.add(attribute);
	}

	return attributes;
    }

    private JdbcAttributeMapper findJdbcAttributeMapper(Class<?> attributeClass, Integer sqlType) {
	JdbcAttributeMapper jdbcAttributeMapper = dbConfiguration.getDbTypeMapper().mapJdbcAttribute(attributeClass, sqlType);
	if (jdbcAttributeMapper != null)
	    return jdbcAttributeMapper;

	return null;
    }

    private Integer findSqlType(Class<?> attributeClass, Enumerated enumerated) {
	Integer sqlType = JdbcTypes.sqlTypeFromClass(attributeClass);
	if (sqlType != Types.NULL)
	    return sqlType;

	if (attributeClass.isEnum() && enumerated == null)
	    return Types.INTEGER;

	if (attributeClass.isEnum() && enumerated != null) {
	    LOG.debug("findSqlType: enumerated.value()=" + enumerated.value());

	    if (enumerated.value() == null)
		return Types.INTEGER;

	    if (enumerated.value() == EnumType.STRING)
		return Types.VARCHAR;

	    if (enumerated.value() == EnumType.ORDINAL)
		return Types.INTEGER;
	}

	return Types.NULL;
    }

    private MetaAttribute readAttribute(String parentClassName, EnhAttribute enhAttribute,
	    Collection<MetaEntity> parsedEntities) throws Exception {
	String columnName = enhAttribute.getName();
	Class<?> c = Class.forName(parentClassName);
	LOG.debug("Reading attribute '" + columnName + "'");
	Field field = c.getDeclaredField(enhAttribute.getName());
	Class<?> attributeClass = null;
	if (enhAttribute.isPrimitiveType())
	    attributeClass = JavaTypes.getClass(enhAttribute.getClassName());
	else
	    attributeClass = Class.forName(enhAttribute.getClassName());

	Method readMethod = c.getMethod(enhAttribute.getGetMethod());
	Method writeMethod = c.getMethod(enhAttribute.getSetMethod(), attributeClass);
	Column column = field.getAnnotation(Column.class);
	if (column != null) {
	    String cn = column.name();
	    if (cn != null && cn.trim().length() > 0)
		columnName = cn;
	}

	LOG.debug("readAttribute: attributeClass=" + attributeClass);
	Id idAnnotation = field.getAnnotation(Id.class);
	Enumerated enumerated = field.getAnnotation(Enumerated.class);
	LOG.debug("readAttribute: enumerated=" + enumerated);
	Integer sqlType = findSqlType(attributeClass, enumerated);
	LOG.debug("readAttribute: sqlType=" + sqlType);
	Class<?> readWriteType = dbConfiguration.getDbTypeMapper().map(attributeClass, sqlType);
	JdbcAttributeMapper jdbcAttributeMapper = findJdbcAttributeMapper(attributeClass, sqlType);
	if (idAnnotation != null) {
	    MetaAttribute.Builder builder = new MetaAttribute.Builder(enhAttribute.getName()).withColumnName(columnName)
		    .withType(attributeClass).withReadWriteDbType(readWriteType).withDbTypeMapper(dbConfiguration.getDbTypeMapper())
		    .withReadMethod(readMethod).withWriteMethod(writeMethod).isId(true).withSqlType(sqlType)
		    .withJavaMember(field).isBasic(true);

	    PkGeneration gv = buildPkGeneration(field);
	    builder.withPkGeneration(gv).withJdbcAttributeMapper(jdbcAttributeMapper);
	    return builder.build();
	}

	boolean embedded = enhAttribute.isEmbedded();
	MetaEntity embeddableEntity = null;
	if (embedded) {
	    embeddableEntity = parseEmbeddable(enhAttribute.getEmbeddedEnhEntity(), parsedEntities);
	}

	boolean id = field.getAnnotation(EmbeddedId.class) != null;
	boolean isCollection = CollectionUtils.isCollectionClass(attributeClass);
	Class<?> collectionImplementationClass = null;
	if (isCollection)
	    collectionImplementationClass = CollectionUtils.findCollectionImplementationClass(attributeClass);

	boolean version = field.getAnnotation(Version.class) != null;

	MetaAttribute.Builder builder = new MetaAttribute.Builder(enhAttribute.getName()).withColumnName(columnName)
		.withType(attributeClass).withReadWriteDbType(readWriteType).withDbTypeMapper(dbConfiguration.getDbTypeMapper())
		.withReadMethod(readMethod).withWriteMethod(writeMethod).isId(id).withSqlType(sqlType)
		.isEmbedded(embedded).isCollection(isCollection)
		.withJavaMember(field).withJdbcAttributeMapper(jdbcAttributeMapper).
		withCollectionImplementationClass(collectionImplementationClass).
		withEmbeddableMetaEntity(embeddableEntity).isVersion(version).
		isBasic(AttributeUtil.isBasicAttribute(attributeClass));
	if (id)
	    builder.withPkGeneration(new PkGeneration());

	OneToOne oneToOne = field.getAnnotation(OneToOne.class);
	ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
	OneToMany oneToMany = field.getAnnotation(OneToMany.class);
	ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
	JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
	if (oneToOne != null)
	    builder.withRelationship(oneToOneHelper.createOneToOne(oneToOne, joinColumn));
	else if (manyToOne != null)
	    builder.withRelationship(manyToOneHelper.createManyToOne(manyToOne, joinColumn));
	else if (oneToMany != null) {
	    Class<?> collectionClass = null;
	    Class<?> targetEntity = oneToMany.targetEntity();
	    if (targetEntity == null || targetEntity == Void.TYPE)
		targetEntity = ReflectionUtil.findTargetEntity(field);

	    JoinTable joinTable = field.getAnnotation(JoinTable.class);
	    builder.withRelationship(oneToManyHelper.createOneToMany(oneToMany, joinColumn, null, collectionClass, targetEntity, joinTable));
	} else if (manyToMany != null) {
	    Class<?> collectionClass = null;
	    Class<?> targetEntity = manyToMany.targetEntity();
	    if (targetEntity == null || targetEntity == Void.TYPE)
		targetEntity = ReflectionUtil.findTargetEntity(field);

	    JoinTable joinTable = field.getAnnotation(JoinTable.class);
	    builder.withRelationship(manyToManyHelper.createManyToMany(manyToMany, joinColumn, null, collectionClass, targetEntity, joinTable));
	}

	// Basic annotation
	Basic basic = field.getAnnotation(Basic.class);
	if (basic != null && !id) {
	    if (!basic.optional())
		builder.isNullable(false);
	}

	MetaAttribute attribute = builder.build();
	LOG.debug("readAttribute: attribute: " + attribute);
	return attribute;
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
	PkGeneration pkGeneration = new PkGeneration();
	pkGeneration.setPkStrategy(pkStrategy);
	pkGeneration.setGenerator(generatedValue.generator());

	SequenceGenerator sequenceGenerator = field.getAnnotation(SequenceGenerator.class);
	if (pkStrategy == PkStrategy.SEQUENCE)
	    if (sequenceGenerator != null) {
		if (generatedValue.strategy() != GenerationType.SEQUENCE)
		    throw new IllegalArgumentException("Generated Value Strategy must be 'SEQUENCE'");

		if (sequenceGenerator.name() != null && generatedValue.generator() != null && !sequenceGenerator.name().equals(generatedValue.generator()))
		    throw new IllegalArgumentException("Generator '" + generatedValue.generator() + "' not found"); ///

		PkSequenceGenerator pkSequenceGenerator = new PkSequenceGenerator();
		pkSequenceGenerator.setSequenceName(sequenceGenerator.sequenceName());
		pkSequenceGenerator.setSchema(sequenceGenerator.schema());
		pkSequenceGenerator.setAllocationSize(sequenceGenerator.allocationSize());
		pkSequenceGenerator.setCatalog(sequenceGenerator.schema());
		pkSequenceGenerator.setInitialValue(sequenceGenerator.initialValue());
		pkGeneration.setPkSequenceGenerator(pkSequenceGenerator);
	    } else {
//		PkSequenceGenerator pkSequenceGenerator = new PkSequenceGenerator();
//		pkSequenceGenerator.setSequenceName(sequenceGenerator.sequenceName());
//		pkSequenceGenerator.setSchema(sequenceGenerator.schema());
//		pkSequenceGenerator.setAllocationSize(sequenceGenerator.allocationSize());
//		pkSequenceGenerator.setCatalog(sequenceGenerator.schema());
//		pkSequenceGenerator.setInitialValue(sequenceGenerator.initialValue());
	    }

	return pkGeneration;
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
	for (MetaAttribute a : attributes) {
	    LOG.debug("finalizeRelationships: a=" + a);
	    if (a.isEmbedded()) {
		finalizeRelationships(entity, entities, a.getEmbeddableMetaEntity().getAttributes());
		return;
	    }

	    Relationship relationship = a.getRelationship();
	    if (relationship == null)
		continue;

	    if (relationship instanceof OneToOneRelationship) {
		MetaEntity toEntity = entities.get(a.getType().getName());
		LOG.debug("finalizeRelationships: OneToOne toEntity=" + toEntity);
		if (toEntity == null)
		    throw new IllegalArgumentException("One to One entity not found (" + a.getType().getName() + ")");

		OneToOneRelationship oneToOne = (OneToOneRelationship) relationship;
		a.setRelationship(oneToOneHelper.finalizeRelationship(oneToOne, a, entity, toEntity, dbConfiguration));
	    } else if (relationship instanceof ManyToOneRelationship) {
		MetaEntity toEntity = entities.get(a.getType().getName());
		LOG.debug("finalizeRelationships: ManyToOne toEntity=" + toEntity);
		if (toEntity == null)
		    throw new IllegalArgumentException("Many to One entity not found (" + a.getType().getName() + ")");

		ManyToOneRelationship manyToOne = (ManyToOneRelationship) relationship;
		a.setRelationship(manyToOneHelper.finalizeRelationship(manyToOne, a, entity, toEntity, dbConfiguration));
	    } else if (relationship instanceof OneToManyRelationship) {
		MetaEntity toEntity = entities.get(relationship.getTargetEntityClass().getName());
		LOG.debug("finalizeRelationships: OneToMany toEntity=" + toEntity + "; a.getType().getName()="
			+ a.getType().getName());
		if (toEntity == null)
		    throw new IllegalArgumentException("One to Many target entity not found ("
			    + relationship.getTargetEntityClass().getName() + ")");

		OneToManyRelationship oneToMany = (OneToManyRelationship) relationship;
		OneToManyRelationship otm = oneToManyHelper.finalizeRelationship(oneToMany, a, entity, toEntity, dbConfiguration, aliasGenerator, entities);
		a.setRelationship(otm);
	    } else if (relationship instanceof ManyToManyRelationship) {
		MetaEntity toEntity = entities.get(relationship.getTargetEntityClass().getName());
		LOG.debug("finalizeRelationships: ManyToMany toEntity=" + toEntity + "; a.getType().getName()="
			+ a.getType().getName());
		if (toEntity == null)
		    throw new IllegalArgumentException("Many to Many target entity not found ("
			    + relationship.getTargetEntityClass().getName() + ")");

		ManyToManyRelationship manyToMany = (ManyToManyRelationship) relationship;
		ManyToManyRelationship otm = manyToManyHelper.finalizeRelationship(manyToMany, a, entity, toEntity, dbConfiguration, aliasGenerator, entities);
		a.setRelationship(otm);
	    }
	}
    }

    /**
     * It's a post-processing step needed to complete entity data. For example, filling out the 'join column' one to one
     * relationships if missing.
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
		LOG.debug("printAttributes: a=" + a);
		if (a.getRelationship() != null)
		    LOG.debug("printAttributes: a.getRelationship()=" + a.getRelationship());
	    }
	}
    }

}
