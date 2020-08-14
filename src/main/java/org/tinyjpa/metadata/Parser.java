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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.AttributeUtil;
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.JdbcTypes;
import org.tinyjpa.jdbc.JoinColumnAttribute;
import org.tinyjpa.jdbc.PkGenerationType;
import org.tinyjpa.jdbc.relationship.ManyToOne;
import org.tinyjpa.jdbc.relationship.OneToMany;
import org.tinyjpa.jdbc.relationship.OneToOne;
import org.tinyjpa.jdbc.relationship.RelationshipJoinTable;

public class Parser {
	private Logger LOG = LoggerFactory.getLogger(Parser.class);
	private AliasGenerator aliasGenerator = new AliasGenerator();

	public Map<String, Entity> parse(List<EnhEntity> enhancedClasses) throws Exception {
		Map<String, Entity> entities = new HashMap<>();
		for (EnhEntity enhEntity : enhancedClasses) {
			Entity entity = parse(enhEntity, entities.values());
			if (entity != null)
				entities.put(enhEntity.getClassName(), entity);
		}

		finalizeRelationships(entities);
		printAttributes(entities);
		return entities;
	}

	private Entity parse(EnhEntity enhEntity, Collection<Entity> parsedEntities) throws Exception {
		Class<?> c = Class.forName(enhEntity.getClassName());
		javax.persistence.Entity ec = c.getAnnotation(javax.persistence.Entity.class);
		if (ec == null) {
			LOG.warn("@Entity annotation not found");
			return null;
		}

		LOG.info("Reading attributes...");
		List<Attribute> attributes = readAttributes(enhEntity);
		if (enhEntity.getMappedSuperclass() != null) {
			List<Attribute> msAttributes = readAttributes(enhEntity.getMappedSuperclass());
			attributes.addAll(msAttributes);
		}

		LOG.info("Reading Id...");
		Optional<Attribute> optional = attributes.stream().filter(a -> a.isId()).findFirst();
		if (!optional.isPresent()) {
			LOG.warn("@Id annotation not found");
			return null;
		}

		Attribute id = optional.get();
		attributes.remove(id);

		String tableName = c.getSimpleName();
		Table table = c.getAnnotation(Table.class);
		if (table != null && table.name() != null && table.name().trim().length() > 0)
			tableName = table.name();

		String alias = aliasGenerator.calculateAlias(tableName, parsedEntities);
		return new Entity(c, tableName, alias, id, attributes);
	}

	private List<Attribute> readAttributes(EnhEntity enhEntity) throws Exception {
		List<Attribute> attributes = new ArrayList<>();
		for (EnhAttribute enhAttribute : enhEntity.getEnhAttributes()) {
			Attribute attribute = readAttribute(enhEntity.getClassName(), enhAttribute);
			attributes.add(attribute);
		}

		return attributes;
	}

	private List<Attribute> readAttributes(List<EnhAttribute> enhAttributes, String parentClassName) throws Exception {
		List<Attribute> attributes = new ArrayList<>();
		for (EnhAttribute enhAttribute : enhAttributes) {
			Attribute attribute = readAttribute(parentClassName, enhAttribute);
			attributes.add(attribute);
		}

		return attributes;
	}

	private Attribute readAttribute(String parentClassName, EnhAttribute enhAttribute) throws Exception {
		String columnName = enhAttribute.getName();
		Class<?> c = Class.forName(parentClassName);
		LOG.info("readAttribute: columnName=" + columnName);
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
		Attribute attribute = null;
		if (idAnnotation == null) {
			List<Attribute> embeddedAttributes = null;
			boolean embedded = enhAttribute.isEmbedded();
			if (embedded) {
				embeddedAttributes = readAttributes(enhAttribute.getEmbeddedAttributes(), enhAttribute.getClassName());
				LOG.info("readAttribute: embeddedAttributes.size()=" + embeddedAttributes.size());
				if (embeddedAttributes.isEmpty()) {
					embedded = false;
					embeddedAttributes = null;
				}
			}

			boolean id = field.getAnnotation(EmbeddedId.class) != null;
//			LOG.info("readAttribute: enhAttribute.getName()=" + enhAttribute.getName());
//			LOG.info("readAttribute: embedded=" + embedded);

			Attribute.Builder builder = new Attribute.Builder(enhAttribute.getName()).withColumnName(columnName)
					.withType(attributeClass).withReadMethod(readMethod).withWriteMethod(writeMethod).isId(id)
					.withSqlType(JdbcTypes.sqlTypeFromClass(attributeClass)).isEmbedded(embedded)
					.withEmbeddedAttributes(embeddedAttributes);
			javax.persistence.OneToOne oneToOne = field.getAnnotation(javax.persistence.OneToOne.class);
			javax.persistence.ManyToOne manyToOne = field.getAnnotation(javax.persistence.ManyToOne.class);
			javax.persistence.OneToMany oneToMany = field.getAnnotation(javax.persistence.OneToMany.class);
			JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
			if (oneToOne != null) {
				builder.withOneToOne(createOneToOne(oneToOne, joinColumn, null));
				builder.withRelationship(createOneToOne(oneToOne, joinColumn, null));
			} else if (manyToOne != null) {
				builder.withManyToOne(createManyToOne(manyToOne, joinColumn, null));
				builder.withRelationship(createManyToOne(manyToOne, joinColumn, null));
			} else if (oneToMany != null) {
				Class<?> collectionClass = findAttributeImpl(c, readMethod);
				if (collectionClass == null) {
					collectionClass = AttributeUtil.findImplementationClass(attributeClass);
				}

				Class<?> targetEntity = oneToMany.targetEntity();
				if (targetEntity == null || targetEntity == Void.TYPE) {
					targetEntity = ReflectionUtil.findTargetEntity(field);
				}

				builder.withOneToMany(createOneToMany(oneToMany, joinColumn, null, collectionClass, targetEntity));
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

			attribute = new Attribute.Builder(enhAttribute.getName()).withColumnName(columnName)
					.withType(attributeClass).withReadMethod(readMethod).withWriteMethod(writeMethod).isId(true)
					.withSqlType(JdbcTypes.sqlTypeFromClass(attributeClass)).withGeneratedValue(gv).build();
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

	private String createDefaultJoinColumn(Attribute owningAttribute, Entity toEntity) {
		return owningAttribute.getName() + "_" + toEntity.getId().getColumnName();
	}

	private String createDefaultOneToManyOwnerJoinColumn(Entity entity, Attribute attribute) {
		return entity.getTableName() + "_" + attribute.getColumnName();
	}

	private JoinColumnAttribute createJoinColumnOwningAttribute(Entity entity, Attribute attribute, String joinColumn) {
		String jc = joinColumn;
		if (jc == null)
			jc = createDefaultOneToManyOwnerJoinColumn(entity, attribute);

		return new JoinColumnAttribute.Builder().withColumnName(jc).withType(attribute.getType())
				.withSqlType(attribute.getSqlType()).withForeignKeyAttribute(attribute).build();
	}

	private List<JoinColumnAttribute> createJoinColumnOwningAttributes(Entity entity) {
		List<Attribute> attributes = entity.getId().expand();
		List<JoinColumnAttribute> joinColumnAttributes = new ArrayList<>();
		for (Attribute a : attributes) {
			JoinColumnAttribute joinColumnAttribute = createJoinColumnOwningAttribute(entity, a, null);
			joinColumnAttributes.add(joinColumnAttribute);
		}

		return joinColumnAttributes;
	}

	private String createDefaultOneToManyJoinColumn(Attribute relationshipAttribute, Attribute id) {
		return relationshipAttribute.getName() + "_" + id.getColumnName();
	}

	private JoinColumnAttribute createJoinColumnTargetAttribute(Attribute id, Attribute relationshipAttribute,
			String joinColumn) {
		String jc = joinColumn;
		if (jc == null)
			jc = createDefaultOneToManyJoinColumn(relationshipAttribute, id);

		return new JoinColumnAttribute.Builder().withColumnName(jc).withType(id.getType()).withSqlType(id.getSqlType())
				.withForeignKeyAttribute(id).build();
	}

	private List<JoinColumnAttribute> createJoinColumnTargetAttributes(Entity entity, Attribute relationshipAttribute) {
		List<Attribute> attributes = entity.getId().expand();
		List<JoinColumnAttribute> joinColumnAttributes = new ArrayList<>();
		for (Attribute a : attributes) {
			JoinColumnAttribute joinColumnAttribute = createJoinColumnTargetAttribute(a, relationshipAttribute, null);
			joinColumnAttributes.add(joinColumnAttribute);
		}

		return joinColumnAttributes;
	}

	private String createDefaultOneToManyJoinTable(Entity owner, Entity target) {
		return owner.getTableName() + "_" + target.getTableName();
	}

	private void finalizeRelationships(Entity entity, Map<String, Entity> entities, List<Attribute> attributes) {
		for (Attribute a : attributes) {
			LOG.info("finalizeRelationships: a=" + a);
			if (a.isEmbedded()) {
				finalizeRelationships(entity, entities, a.getEmbeddedAttributes());
				return;
			}

			if (a.isOneToOne()) {
				Entity toEntity = entities.get(a.getType().getName());
				LOG.info("finalizeRelationships: OneToOne toEntity=" + toEntity);
				if (toEntity == null)
					throw new IllegalArgumentException("One to One entity not found (" + a.getType().getName() + ")");

				OneToOne oneToOne = a.getOneToOne();
				OneToOne.Builder builder = new OneToOne.Builder().with(oneToOne);
				if (oneToOne.isOwner()) {
					String joinColumnName = oneToOne.getJoinColumn();
					if (oneToOne.getJoinColumn() == null) {
						joinColumnName = createDefaultJoinColumn(a, toEntity);
						builder = builder.withJoinColumn(joinColumnName);
					}

					JoinColumnAttribute joinColumnAttribute = new JoinColumnAttribute.Builder()
							.withColumnName(joinColumnName).withType(toEntity.getId().getType())
							.withSqlType(toEntity.getId().getSqlType()).withForeignKeyAttribute(a).build();
					entity.getJoinColumnAttributes().add(joinColumnAttribute);
					builder = builder.withTargetAttribute(toEntity.findAttributeWithMappedBy(a.getName()));
				}

				if (!a.getOneToOne().isOwner()) {
					builder = builder.withOwningEntity(toEntity);
					builder = builder.withOwningAttribute(toEntity.getAttribute(oneToOne.getMappedBy()));
				}

				builder.withAttributeType(toEntity);
				a.setOneToOne(builder.build());
				a.setRelationship(builder.build());
			} else if (a.isManyToOne()) {
				Entity toEntity = entities.get(a.getType().getName());
				LOG.info("finalizeRelationships: ManyToOne toEntity=" + toEntity);
				if (toEntity == null)
					throw new IllegalArgumentException("Many to One entity not found (" + a.getType().getName() + ")");

				ManyToOne manyToOne = a.getManyToOne();
				ManyToOne.Builder builder = new ManyToOne.Builder().with(manyToOne);
				if (a.getManyToOne().isOwner() && a.getManyToOne().getJoinColumn() == null) {
					String joinColumnName = createDefaultJoinColumn(a, toEntity);
					LOG.info("finalizeRelationships: ManyToOne joinColumnName=" + joinColumnName);
					builder = builder.withJoinColumn(joinColumnName);

					JoinColumnAttribute joinColumnAttribute = new JoinColumnAttribute.Builder()
							.withColumnName(joinColumnName).withType(toEntity.getId().getType())
							.withSqlType(toEntity.getId().getSqlType()).withForeignKeyAttribute(a).build();
					entity.getJoinColumnAttributes().add(joinColumnAttribute);
				}

				LOG.info("finalizeRelationships: ManyToOne manyToOne=" + manyToOne);
				builder = builder.withAttributeType(toEntity);
				a.setManyToOne(builder.build());
				a.setRelationship(builder.build());
			} else if (a.isOneToMany()) {
				OneToMany oneToMany = a.getOneToMany();
				Entity toEntity = entities.get(oneToMany.getTargetEntityClass().getName());
				LOG.info("finalizeRelationships: OneToMany toEntity=" + toEntity + "; a.getType().getName()="
						+ a.getType().getName());
				if (toEntity == null)
					throw new IllegalArgumentException(
							"One to Many target entity not found (" + oneToMany.getTargetEntityClass().getName() + ")");

				OneToMany.Builder builder = new OneToMany.Builder().with(a.getOneToMany());
				builder = builder.withAttributeType(toEntity);
				if (oneToMany.isOwner()) {
					if (oneToMany.getJoinColumn() == null) {
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
								.withColumnName(oneToMany.getJoinColumn()).withType(entity.getId().getType())
								.withSqlType(entity.getId().getSqlType()).withForeignKeyAttribute(entity.getId())
								.build();
						toEntity.getJoinColumnAttributes().add(joinColumnAttribute);
					}
				} else {
					builder = builder.withOwningEntity(toEntity);
					builder = builder.withOwningAttribute(toEntity.getAttribute(oneToMany.getMappedBy()));
					Attribute attribute = toEntity.getAttribute(oneToMany.getMappedBy());
					LOG.info("finalizeRelationships: OneToMany targetAttribute=" + attribute);
					builder = builder.withTargetAttribute(attribute);
				}

				OneToMany otm = builder.build();
				a.setOneToMany(otm);
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
	private void finalizeRelationships(Map<String, Entity> entities) {
		for (Map.Entry<String, Entity> entry : entities.entrySet()) {
			Entity entity = entry.getValue();
			List<Attribute> attributes = entity.getAttributes();
			finalizeRelationships(entity, entities, attributes);
		}
	}

	private void printAttributes(Map<String, Entity> entities) {
		for (Map.Entry<String, Entity> entry : entities.entrySet()) {
			Entity entity = entry.getValue();
			List<Attribute> attributes = entity.getAttributes();
			for (int i = 0; i < attributes.size(); ++i) {
				Attribute a = attributes.get(i);
				LOG.info("printAttributes: a=" + a);
				if (a.isManyToOne()) {
					LOG.info("printAttributes: a.getManyToOne()=" + a.getManyToOne());
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
