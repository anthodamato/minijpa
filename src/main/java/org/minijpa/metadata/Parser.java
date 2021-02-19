package org.minijpa.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.minijpa.jdbc.CollectionUtils;
import org.minijpa.jdbc.JdbcTypes;
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
//    private JdbcAttributeMapper jdbcStringEnumMapper = new JdbcStringEnumMapper();
//    private JdbcAttributeMapper jdbcOrdinalEnumMapper = new JdbcOrdinalEnumMapper();
    private OneToOneHelper oneToOneHelper = new OneToOneHelper();
    private ManyToOneHelper manyToOneHelper = new ManyToOneHelper();
    private OneToManyHelper oneToManyHelper = new OneToManyHelper();
    private ManyToManyHelper manyToManyHelper = new ManyToManyHelper();
    
    public Parser(DbConfiguration dbConfiguration) {
	super();
	this.dbConfiguration = dbConfiguration;
    }
    
    public void fillRelationships(Map<String, MetaEntity> entities) {
	finalizeRelationships(entities);
	printAttributes(entities);
    }
    
    public MetaEntity parse(EnhEntity enhEntity, Collection<MetaEntity> parsedEntities) throws Exception {
	Class<?> c = Class.forName(enhEntity.getClassName());
	Entity ec = c.getAnnotation(Entity.class);
	if (ec == null)
	    throw new Exception("@Entity annotation not found: '" + c.getName() + "'");
	
	LOG.info("Reading '" + enhEntity.getClassName() + "' attributes...");
	List<MetaAttribute> attributes = readAttributes(enhEntity);
	MetaEntity mappedSuperclassEntity = null;
	if (enhEntity.getMappedSuperclass() != null) {
	    Optional<MetaEntity> optional = parsedEntities.stream().filter(e -> e.getMappedSuperclassEntity() != null)
		    .filter(e -> e.getMappedSuperclassEntity().getEntityClass().getName()
		    .equals(enhEntity.getMappedSuperclass().getClassName()))
		    .findFirst();
	    
	    if (optional.isPresent())
		mappedSuperclassEntity = optional.get().getMappedSuperclassEntity();
	    else
		mappedSuperclassEntity = parseMappedSuperclass(enhEntity.getMappedSuperclass());
	    
	    List<MetaAttribute> msAttributes = mappedSuperclassEntity.getAttributes();
	    attributes.addAll(msAttributes);
	    attributes.add(mappedSuperclassEntity.getId());
	}
	
	LOG.info("Getting '" + c.getName() + "' Id...");
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
	LOG.info("Building embeddables...");
	List<MetaEntity> embeddables = buildEmbeddables(enhEntity.getEmbeddables(), attributes, parsedEntities);
	
	attributes.remove(id);
	
	return new MetaEntity(c, name, tableName, alias, id, attributes, mappedSuperclassEntity, embeddables);
    }
    
    private List<MetaEntity> buildEmbeddables(List<EnhEntity> enhEmbeddables, List<MetaAttribute> attributes,
	    Collection<MetaEntity> parsedEntities) throws Exception {
	List<MetaEntity> embeddables = new ArrayList<>();
	for (EnhEntity enhEntity : enhEmbeddables) {
	    MetaEntity embeddable = buildEmbeddable(enhEntity, attributes, parsedEntities);
	    embeddables.add(embeddable);
	}
	
	return embeddables;
    }
    
    private MetaEntity buildEmbeddable(EnhEntity enhEmbeddable, List<MetaAttribute> attributes,
	    Collection<MetaEntity> parsedEntities) throws Exception {
	Optional<MetaEntity> optionalMetaEntity = findParsedEmbeddable(enhEmbeddable.getClassName(), parsedEntities);
	if (optionalMetaEntity.isPresent())
	    return optionalMetaEntity.get();
	
	List<MetaAttribute> embeddedAttributes = new ArrayList<>();
	for (EnhAttribute enhAttribute : enhEmbeddable.getEnhAttributes()) {
	    Optional<MetaAttribute> optional = attributes.stream()
		    .filter(a -> a.isEmbedded() && a.getType().getName().equals(enhEmbeddable.getClassName()))
		    .findFirst();
	    MetaAttribute metaAttribute = optional.get();
	    optional = metaAttribute.getEmbeddedAttributes().stream()
		    .filter(a -> a.getName().equals(enhAttribute.getName())).findFirst();
	    if (optional.isPresent())
		embeddedAttributes.add(optional.get());
	}
	
	Class<?> c = Class.forName(enhEmbeddable.getClassName());
	return new MetaEntity(c, null, null, null, null, embeddedAttributes, null, null);
    }
    
    private Optional<MetaEntity> findParsedEmbeddable(String className, Collection<MetaEntity> parsedEntities) {
	for (MetaEntity metaEntity : parsedEntities) {
	    List<MetaEntity> embeddables = metaEntity.getEmbeddables();
	    if (embeddables == null)
		continue;
	    
	    for (MetaEntity embeddable : embeddables) {
		if (embeddable.getEntityClass().getName().equals(className))
		    return Optional.of(embeddable);
	    }
	}
	
	return Optional.empty();
    }
    
    private MetaEntity parseMappedSuperclass(EnhEntity enhEntity)
	    throws Exception {
	Class<?> c = Class.forName(enhEntity.getClassName());
	MappedSuperclass ec = c.getAnnotation(MappedSuperclass.class);
	if (ec == null)
	    throw new Exception("@MappedSuperclass annotation not found: '" + c.getName() + "'");
	
	LOG.info("Reading mapped superclass '" + enhEntity.getClassName() + "' attributes...");
	List<MetaAttribute> attributes = readAttributes(enhEntity);
	
	Optional<MetaAttribute> optionalId = attributes.stream().filter(a -> a.isId()).findFirst();
	if (!optionalId.isPresent())
	    throw new Exception("@Id annotation not found in mapped superclass: '" + c.getName() + "'");
	
	MetaAttribute id = optionalId.get();
	attributes.remove(id);
	
	return new MetaEntity(c, null, null, null, optionalId.get(), attributes, null, null);
    }
    
    private List<MetaAttribute> readAttributes(EnhEntity enhEntity) throws Exception {
	List<MetaAttribute> attributes = new ArrayList<>();
	for (EnhAttribute enhAttribute : enhEntity.getEnhAttributes()) {
	    MetaAttribute attribute = readAttribute(enhEntity.getClassName(), enhAttribute);
	    attributes.add(attribute);
	}
	
	return attributes;
    }
    
    private List<MetaAttribute> readAttributes(List<EnhAttribute> enhAttributes, String parentClassName)
	    throws Exception {
	List<MetaAttribute> attributes = new ArrayList<>();
	for (EnhAttribute enhAttribute : enhAttributes) {
	    MetaAttribute attribute = readAttribute(parentClassName, enhAttribute);
	    attributes.add(attribute);
	}
	
	return attributes;
    }
    
    private JdbcAttributeMapper findJdbcAttributeMapper(Class<?> attributeClass, Integer sqlType) {
	JdbcAttributeMapper jdbcAttributeMapper = dbConfiguration.getDbTypeMapper().mapJdbcAttribute(attributeClass, sqlType);
	if (jdbcAttributeMapper != null)
	    return jdbcAttributeMapper;

//	if (attributeClass.isEnum() && sqlType == Types.VARCHAR)
//	    return jdbcStringEnumMapper;
//
//	if (attributeClass.isEnum() && sqlType == Types.INTEGER)
//	    return jdbcOrdinalEnumMapper;
	return null;
    }
    
    private Integer findSqlType(Class<?> attributeClass, Enumerated enumerated) {
	Integer sqlType = JdbcTypes.sqlTypeFromClass(attributeClass);
	if (sqlType != Types.NULL)
	    return sqlType;
	
	if (attributeClass.isEnum() && enumerated == null)
	    return Types.INTEGER;
	
	if (attributeClass.isEnum() && enumerated != null) {
	    LOG.info("findSqlType: enumerated.value()=" + enumerated.value());
	    
	    if (enumerated.value() == null)
		return Types.INTEGER;
	    
	    if (enumerated.value() == EnumType.STRING)
		return Types.VARCHAR;
	    
	    if (enumerated.value() == EnumType.ORDINAL)
		return Types.INTEGER;
	}
	
	return Types.NULL;
    }
    
    private MetaAttribute readAttribute(String parentClassName, EnhAttribute enhAttribute) throws Exception {
	String columnName = enhAttribute.getName();
	Class<?> c = Class.forName(parentClassName);
	LOG.info("Reading attribute '" + columnName + "'");
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
	
	LOG.info("readAttribute: attributeClass=" + attributeClass);
	Id idAnnotation = field.getAnnotation(Id.class);
	Enumerated enumerated = field.getAnnotation(Enumerated.class);
	LOG.info("readAttribute: enumerated=" + enumerated);
	Integer sqlType = findSqlType(attributeClass, enumerated);
	LOG.info("readAttribute: sqlType=" + sqlType);
	LOG.info("readAttribute: attributeClass.isEnum()=" + attributeClass.isEnum());
	Class<?> readWriteType = dbConfiguration.getDbTypeMapper().map(attributeClass, sqlType);
	JdbcAttributeMapper jdbcAttributeMapper = findJdbcAttributeMapper(attributeClass, sqlType);
	if (idAnnotation != null) {
	    MetaAttribute.Builder builder = new MetaAttribute.Builder(enhAttribute.getName()).withColumnName(columnName)
		    .withType(attributeClass).withReadWriteDbType(readWriteType).withDbTypeMapper(dbConfiguration.getDbTypeMapper())
		    .withReadMethod(readMethod).withWriteMethod(writeMethod).isId(true).withSqlType(sqlType)
		    .withJavaMember(field);
	    
	    PkGeneration gv = buildPkGeneration(field);
	    builder.withPkGeneration(gv).withJdbcAttributeMapper(jdbcAttributeMapper);
	    return builder.build();
	}
	
	List<MetaAttribute> embeddedAttributes = null;
	boolean embedded = enhAttribute.isEmbedded();
	if (embedded) {
	    embeddedAttributes = readAttributes(enhAttribute.getEmbeddedAttributes(), enhAttribute.getClassName());
	    if (embeddedAttributes.isEmpty()) {
		embedded = false;
		embeddedAttributes = null;
	    }
	}
	
	boolean id = field.getAnnotation(EmbeddedId.class) != null;
//			LOG.info("readAttribute: enhAttribute.getName()=" + enhAttribute.getName());
//			LOG.info("readAttribute: embedded=" + embedded);

	boolean isCollection = CollectionUtils.isCollectionClass(attributeClass);
	Class<?> collectionImplementationClass = null;
	if (isCollection)
	    collectionImplementationClass = CollectionUtils.findCollectionImplementationClass(attributeClass);
	
	MetaAttribute.Builder builder = new MetaAttribute.Builder(enhAttribute.getName()).withColumnName(columnName)
		.withType(attributeClass).withReadWriteDbType(readWriteType).withDbTypeMapper(dbConfiguration.getDbTypeMapper())
		.withReadMethod(readMethod).withWriteMethod(writeMethod).isId(id).withSqlType(sqlType)
		.isEmbedded(embedded).withEmbeddedAttributes(embeddedAttributes).isCollection(isCollection)
		.withJavaMember(field).withJdbcAttributeMapper(jdbcAttributeMapper).withCollectionImplementationClass(collectionImplementationClass);
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
	    
	    builder.withRelationship(oneToManyHelper.createOneToMany(oneToMany, joinColumn, null, collectionClass, targetEntity));
	} else if (manyToMany != null) {
	    Class<?> collectionClass = null;
	    Class<?> targetEntity = manyToMany.targetEntity();
	    if (targetEntity == null || targetEntity == Void.TYPE)
		targetEntity = ReflectionUtil.findTargetEntity(field);
	    
	    builder.withRelationship(manyToManyHelper.createManyToMany(manyToMany, joinColumn, null, collectionClass, targetEntity));
	}
	
	MetaAttribute attribute = builder.build();
	LOG.info("readAttribute: attribute: " + attribute);
	return attribute;
    }
    
    private PkGeneration buildPkGeneration(Field field) {
	GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
	if (generatedValue == null)
	    return null;
	
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
	    LOG.info("finalizeRelationships: a=" + a);
	    if (a.isEmbedded()) {
		finalizeRelationships(entity, entities, a.getEmbeddedAttributes());
		return;
	    }
	    
	    Relationship relationship = a.getRelationship();
	    if (relationship == null)
		continue;
	    
	    if (relationship instanceof OneToOneRelationship) {
		MetaEntity toEntity = entities.get(a.getType().getName());
		LOG.info("finalizeRelationships: OneToOne toEntity=" + toEntity);
		if (toEntity == null)
		    throw new IllegalArgumentException("One to One entity not found (" + a.getType().getName() + ")");
		
		OneToOneRelationship oneToOne = (OneToOneRelationship) relationship;
		a.setRelationship(oneToOneHelper.finalizeRelationship(oneToOne, a, entity, toEntity, dbConfiguration));
	    } else if (relationship instanceof ManyToOneRelationship) {
		MetaEntity toEntity = entities.get(a.getType().getName());
		LOG.info("finalizeRelationships: ManyToOne toEntity=" + toEntity);
		if (toEntity == null)
		    throw new IllegalArgumentException("Many to One entity not found (" + a.getType().getName() + ")");
		
		ManyToOneRelationship manyToOne = (ManyToOneRelationship) relationship;
		a.setRelationship(manyToOneHelper.finalizeRelationship(manyToOne, a, entity, toEntity, dbConfiguration));
	    } else if (relationship instanceof OneToManyRelationship) {
		MetaEntity toEntity = entities.get(relationship.getTargetEntityClass().getName());
		LOG.info("finalizeRelationships: OneToMany toEntity=" + toEntity + "; a.getType().getName()="
			+ a.getType().getName());
		if (toEntity == null)
		    throw new IllegalArgumentException("One to Many target entity not found ("
			    + relationship.getTargetEntityClass().getName() + ")");
		
		OneToManyRelationship oneToMany = (OneToManyRelationship) relationship;
		OneToManyRelationship otm = oneToManyHelper.finalizeRelationship(oneToMany, a, entity, toEntity, dbConfiguration, aliasGenerator, entities);
		a.setRelationship(otm);
	    } else if (relationship instanceof ManyToManyRelationship) {
		MetaEntity toEntity = entities.get(relationship.getTargetEntityClass().getName());
		LOG.info("finalizeRelationships: ManyToMany toEntity=" + toEntity + "; a.getType().getName()="
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
		LOG.info("printAttributes: a=" + a);
		if (a.getRelationship() != null)
		    LOG.info("printAttributes: a.getRelationship()=" + a.getRelationship());
	    }
	}
    }
    
}
