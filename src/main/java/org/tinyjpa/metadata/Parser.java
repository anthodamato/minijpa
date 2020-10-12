package org.tinyjpa.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.CollectionUtils;
import org.tinyjpa.jdbc.JdbcTypes;
import org.tinyjpa.jdbc.JoinColumnAttribute;
import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.PkGenerationType;
import org.tinyjpa.jdbc.relationship.ManyToOne;
import org.tinyjpa.jdbc.relationship.OneToMany;
import org.tinyjpa.jdbc.relationship.OneToOne;
import org.tinyjpa.jdbc.relationship.Relationship;
import org.tinyjpa.jdbc.relationship.RelationshipJoinTable;
import org.tinyjpa.metadata.enhancer.EnhAttribute;
import org.tinyjpa.metadata.enhancer.EnhEntity;

public class Parser {
	private Logger LOG = LoggerFactory.getLogger(Parser.class);
	private AliasGenerator aliasGenerator = new AliasGenerator();

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
				mappedSuperclassEntity = parseMappedSuperclass(enhEntity.getMappedSuperclass(), parsedEntities);

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
		MetaEntity metaEntity = null;
		if (optionalMetaEntity.isPresent()) {
			metaEntity = optionalMetaEntity.get();
		} else {
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
			metaEntity = new MetaEntity(c, null, null, null, null, embeddedAttributes, null, null);
		}

		return metaEntity;
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

	private MetaEntity parseMappedSuperclass(EnhEntity enhEntity, Collection<MetaEntity> parsedEntities)
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

	private MetaAttribute readAttribute(String parentClassName, EnhAttribute enhAttribute) throws Exception {
		String columnName = enhAttribute.getName();
		Class<?> c = Class.forName(parentClassName);
		LOG.info("Reading attribute '" + columnName + "'");
//		LOG.info("readAttributes: c.getClassLoader()=" + c.getClassLoader());
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

		Id idAnnotation = field.getAnnotation(Id.class);
		MetaAttribute attribute = null;
		if (idAnnotation == null) {
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
			MetaAttribute.Builder builder = new MetaAttribute.Builder(enhAttribute.getName()).withColumnName(columnName)
					.withType(attributeClass).withReadMethod(readMethod).withWriteMethod(writeMethod).isId(id)
					.withSqlType(JdbcTypes.sqlTypeFromClass(attributeClass)).isEmbedded(embedded)
					.withEmbeddedAttributes(embeddedAttributes).isCollection(isCollection).withJavaMember(field);
			javax.persistence.OneToOne oneToOne = field.getAnnotation(javax.persistence.OneToOne.class);
			javax.persistence.ManyToOne manyToOne = field.getAnnotation(javax.persistence.ManyToOne.class);
			javax.persistence.OneToMany oneToMany = field.getAnnotation(javax.persistence.OneToMany.class);
			JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
			if (oneToOne != null) {
				builder.withRelationship(createOneToOne(oneToOne, joinColumn, null));
			} else if (manyToOne != null) {
				builder.withRelationship(createManyToOne(manyToOne, joinColumn, null));
			} else if (oneToMany != null) {
				Class<?> collectionClass = findAttributeImpl(c, readMethod);
				if (collectionClass == null) {
					collectionClass = CollectionUtils.findImplementationClass(attributeClass);
				}

				Class<?> targetEntity = oneToMany.targetEntity();
				if (targetEntity == null || targetEntity == Void.TYPE) {
					targetEntity = ReflectionUtil.findTargetEntity(field);
				}

				builder.withRelationship(createOneToMany(oneToMany, joinColumn, null, collectionClass, targetEntity));
			}

			attribute = builder.build();
			LOG.info("readAttribute: attribute: " + attribute);
		} else {
			GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
			org.tinyjpa.jdbc.GeneratedValue gv = null;
			if (generatedValue != null) {
				PkGenerationType pkGenerationType = decodePkGenerationType(generatedValue.strategy());
				gv = new org.tinyjpa.jdbc.GeneratedValue(pkGenerationType, generatedValue.generator());
			}

			attribute = new MetaAttribute.Builder(enhAttribute.getName()).withColumnName(columnName)
					.withType(attributeClass).withReadMethod(readMethod).withWriteMethod(writeMethod).isId(true)
					.withSqlType(JdbcTypes.sqlTypeFromClass(attributeClass)).withGeneratedValue(gv)
					.withJavaMember(field).build();
		}

		return attribute;
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

	private OneToOne createOneToOne(javax.persistence.OneToOne oneToOne, JoinColumn joinColumn, String joinColumnName) {
		OneToOne.Builder builder = new OneToOne.Builder();
		if (joinColumn != null)
			builder = builder.withJoinColumn(joinColumn.name());

		LOG.info("createOneToOne: oneToOne.mappedBy()=" + oneToOne.mappedBy());
		if (oneToOne.mappedBy() != null && !oneToOne.mappedBy().isEmpty())
			builder = builder.withMappedBy(oneToOne.mappedBy());

		if (oneToOne.fetch() != null) {
			if (oneToOne.fetch() == FetchType.EAGER)
				builder = builder.withFetchType(org.tinyjpa.jdbc.relationship.FetchType.EAGER);
			else if (oneToOne.fetch() == FetchType.LAZY)
				builder = builder.withFetchType(org.tinyjpa.jdbc.relationship.FetchType.LAZY);
		}

		return builder.build();
	}

	private ManyToOne createManyToOne(javax.persistence.ManyToOne manyToOne, JoinColumn joinColumn,
			String joinColumnName) {
		ManyToOne.Builder builder = new ManyToOne.Builder();
		if (joinColumn != null)
			builder = builder.withJoinColumn(joinColumn.name());

		if (manyToOne.fetch() != null) {
			if (manyToOne.fetch() == FetchType.EAGER)
				builder = builder.withFetchType(org.tinyjpa.jdbc.relationship.FetchType.EAGER);
			else if (manyToOne.fetch() == FetchType.LAZY)
				builder = builder.withFetchType(org.tinyjpa.jdbc.relationship.FetchType.LAZY);
		}

		return builder.build();
	}

	private OneToMany createOneToMany(javax.persistence.OneToMany oneToMany, JoinColumn joinColumn,
			String joinColumnName, Class<?> collectionClass, Class<?> targetEntity) {
		OneToMany.Builder builder = new OneToMany.Builder();
		if (joinColumn != null)
			builder = builder.withJoinColumn(joinColumn.name());

		LOG.info("createOneToMany: oneToMany.mappedBy()=" + oneToMany.mappedBy() + "; oneToMany.fetch()="
				+ oneToMany.fetch());
		if (oneToMany.mappedBy() != null && !oneToMany.mappedBy().isEmpty())
			builder = builder.withMappedBy(oneToMany.mappedBy());

		if (oneToMany.fetch() != null) {
			if (oneToMany.fetch() == FetchType.EAGER)
				builder = builder.withFetchType(org.tinyjpa.jdbc.relationship.FetchType.EAGER);
			else if (oneToMany.fetch() == FetchType.LAZY)
				builder = builder.withFetchType(org.tinyjpa.jdbc.relationship.FetchType.LAZY);
		}

		builder = builder.withCollectionClass(collectionClass);
		builder = builder.withTargetEntityClass(targetEntity);
		return builder.build();
	}

	private String createDefaultJoinColumn(MetaAttribute owningAttribute, MetaEntity toEntity) {
		return owningAttribute.getName() + "_" + toEntity.getId().getColumnName();
	}

	private String createDefaultOneToManyOwnerJoinColumn(MetaEntity entity, MetaAttribute attribute) {
		return entity.getTableName() + "_" + attribute.getColumnName();
	}

	private JoinColumnAttribute createJoinColumnOwningAttribute(MetaEntity entity, MetaAttribute attribute,
			String joinColumn) {
		String jc = joinColumn;
		if (jc == null)
			jc = createDefaultOneToManyOwnerJoinColumn(entity, attribute);

		return new JoinColumnAttribute.Builder().withColumnName(jc).withType(attribute.getType())
				.withSqlType(attribute.getSqlType()).withForeignKeyAttribute(attribute).build();
	}

	private List<JoinColumnAttribute> createJoinColumnOwningAttributes(MetaEntity entity) {
		List<MetaAttribute> attributes = entity.getId().expand();
		List<JoinColumnAttribute> joinColumnAttributes = new ArrayList<>();
		for (MetaAttribute a : attributes) {
			JoinColumnAttribute joinColumnAttribute = createJoinColumnOwningAttribute(entity, a, null);
			joinColumnAttributes.add(joinColumnAttribute);
		}

		return joinColumnAttributes;
	}

	private String createDefaultOneToManyJoinColumn(MetaAttribute relationshipAttribute, MetaAttribute id) {
		return relationshipAttribute.getName() + "_" + id.getColumnName();
	}

	private JoinColumnAttribute createJoinColumnTargetAttribute(MetaAttribute id, MetaAttribute relationshipAttribute,
			String joinColumn) {
		String jc = joinColumn;
		if (jc == null)
			jc = createDefaultOneToManyJoinColumn(relationshipAttribute, id);

		return new JoinColumnAttribute.Builder().withColumnName(jc).withType(id.getType()).withSqlType(id.getSqlType())
				.withForeignKeyAttribute(id).build();
	}

	private List<JoinColumnAttribute> createJoinColumnTargetAttributes(MetaEntity entity,
			MetaAttribute relationshipAttribute) {
		List<MetaAttribute> attributes = entity.getId().expand();
		List<JoinColumnAttribute> joinColumnAttributes = new ArrayList<>();
		for (MetaAttribute a : attributes) {
			JoinColumnAttribute joinColumnAttribute = createJoinColumnTargetAttribute(a, relationshipAttribute, null);
			joinColumnAttributes.add(joinColumnAttribute);
		}

		return joinColumnAttributes;
	}

	private String createDefaultOneToManyJoinTable(MetaEntity owner, MetaEntity target) {
		return owner.getTableName() + "_" + target.getTableName();
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

			if (relationship instanceof OneToOne) {
				MetaEntity toEntity = entities.get(a.getType().getName());
				LOG.info("finalizeRelationships: OneToOne toEntity=" + toEntity);
				if (toEntity == null)
					throw new IllegalArgumentException("One to One entity not found (" + a.getType().getName() + ")");

				OneToOne oneToOne = (OneToOne) relationship;
				OneToOne.Builder builder = new OneToOne.Builder().with(oneToOne);
				if (relationship.isOwner()) {
					String joinColumnName = relationship.getJoinColumn();
					if (relationship.getJoinColumn() == null) {
						joinColumnName = createDefaultJoinColumn(a, toEntity);
						builder = builder.withJoinColumn(joinColumnName);
					}

					JoinColumnAttribute joinColumnAttribute = new JoinColumnAttribute.Builder()
							.withColumnName(joinColumnName).withType(toEntity.getId().getType())
							.withSqlType(toEntity.getId().getSqlType()).withForeignKeyAttribute(a).build();
					entity.getJoinColumnAttributes().add(joinColumnAttribute);
					builder = builder.withTargetAttribute(toEntity.findAttributeWithMappedBy(a.getName()));
				}

				if (!relationship.isOwner()) {
					builder = builder.withOwningEntity(toEntity);
					builder = builder.withOwningAttribute(toEntity.getAttribute(relationship.getMappedBy()));
				}

				builder.withAttributeType(toEntity);
				a.setRelationship(builder.build());
			} else if (relationship instanceof ManyToOne) {
				MetaEntity toEntity = entities.get(a.getType().getName());
				LOG.info("finalizeRelationships: ManyToOne toEntity=" + toEntity);
				if (toEntity == null)
					throw new IllegalArgumentException("Many to One entity not found (" + a.getType().getName() + ")");

				ManyToOne manyToOne = (ManyToOne) relationship;
				ManyToOne.Builder builder = new ManyToOne.Builder().with(manyToOne);
				if (relationship.isOwner() && relationship.getJoinColumn() == null) {
					String joinColumnName = createDefaultJoinColumn(a, toEntity);
					LOG.info("finalizeRelationships: ManyToOne joinColumnName=" + joinColumnName);
					builder = builder.withJoinColumn(joinColumnName);

					JoinColumnAttribute joinColumnAttribute = new JoinColumnAttribute.Builder()
							.withColumnName(joinColumnName).withType(toEntity.getId().getType())
							.withSqlType(toEntity.getId().getSqlType()).withForeignKeyAttribute(a).build();
					entity.getJoinColumnAttributes().add(joinColumnAttribute);
				}

				LOG.info("finalizeRelationships: ManyToOne manyToOne=" + relationship);
				builder = builder.withAttributeType(toEntity);
				a.setRelationship(builder.build());
			} else if (relationship instanceof OneToMany) {
				MetaEntity toEntity = entities.get(relationship.getTargetEntityClass().getName());
				LOG.info("finalizeRelationships: OneToMany toEntity=" + toEntity + "; a.getType().getName()="
						+ a.getType().getName());
				if (toEntity == null)
					throw new IllegalArgumentException("One to Many target entity not found ("
							+ relationship.getTargetEntityClass().getName() + ")");

				OneToMany oneToMany = (OneToMany) relationship;
				OneToMany.Builder builder = new OneToMany.Builder().with(oneToMany);
				builder = builder.withAttributeType(toEntity);
				if (relationship.isOwner()) {
					if (relationship.getJoinColumn() == null) {
						String joinTableName = createDefaultOneToManyJoinTable(entity, toEntity);
						String joinTableAlias = aliasGenerator.calculateAlias(joinTableName, entities.values());
						List<JoinColumnAttribute> joinColumnOwningAttributes = createJoinColumnOwningAttributes(entity);
						List<JoinColumnAttribute> joinColumnTargetAttributes = createJoinColumnTargetAttributes(
								toEntity, a);
						RelationshipJoinTable relationshipJoinTable = new RelationshipJoinTable(joinTableName,
								joinTableAlias, joinColumnOwningAttributes, joinColumnTargetAttributes, entity.getId(),
								toEntity.getId());
						builder = builder.withJoinTable(relationshipJoinTable);
					} else {
						// TODO: current implemented with just one join column, more columns could be
						// used
						JoinColumnAttribute joinColumnAttribute = new JoinColumnAttribute.Builder()
								.withColumnName(relationship.getJoinColumn()).withType(entity.getId().getType())
								.withSqlType(entity.getId().getSqlType()).withForeignKeyAttribute(entity.getId())
								.build();
						toEntity.getJoinColumnAttributes().add(joinColumnAttribute);
					}
				} else {
					builder = builder.withOwningEntity(toEntity);
					builder = builder.withOwningAttribute(toEntity.getAttribute(relationship.getMappedBy()));
					MetaAttribute attribute = toEntity.getAttribute(relationship.getMappedBy());
					LOG.info("finalizeRelationships: OneToMany targetAttribute=" + attribute);
					builder = builder.withTargetAttribute(attribute);
				}

				OneToMany otm = builder.build();
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
				LOG.info("printAttributes: a=" + a);
				if (a.getRelationship() != null) {
					LOG.info("printAttributes: a.getRelationship()=" + a.getRelationship());
				}
			}
		}
	}

	/**
	 * @TODO Should be read using the bytecode manipulation tool. This is the case
	 *       of attribute type already set. For example: <code>
	 * &#64;OneToMany(mappedBy = "department") private Collection<Employee> employees =
	 *                     new HashSet<>();
	 * </code>
	 * 
	 * @param parentClass the entity class
	 * @param readMethod  attribute get method
	 * @return the get method return class
	 * @throws Exception
	 */
	private Class<?> findAttributeImpl(Class<?> parentClass, Method readMethod) throws Exception {
		Object object = parentClass.newInstance();

		Map<Class<?>, Map<Object, Object>> pendingNewEntities = new HashMap<>();
		Map<Object, Object> map = new HashMap<>();
		map.put(object, object);
		pendingNewEntities.put(parentClass, map);
		Object getResult = readMethod.invoke(object);

		if (getResult != null)
			return getResult.getClass();

		return null;
	}
}
