package org.tinyjpa.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.JdbcTypes;
import org.tinyjpa.jdbc.PkGenerationType;
import org.tinyjpa.jdbc.relationship.OneToOne;

public class Parser {
	private Logger LOG = LoggerFactory.getLogger(Parser.class);

	public Map<String, Entity> parse(List<EnhEntity> enhancedClasses) throws Exception {
		Map<String, Entity> entities = new HashMap<>();
		for (EnhEntity enhEntity : enhancedClasses) {
			Entity entity = parse(enhEntity);
			if (entity != null)
				entities.put(enhEntity.getClassName(), entity);
		}

		finalizeRelationships(entities);
		return entities;
	}

	private Entity parse(EnhEntity enhEntity) throws Exception {
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

		return new Entity(c, tableName, id, attributes);
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
//			if (column == null) {
//				column = readMethod.getAnnotation(Column.class);
//			}

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
			javax.persistence.OneToOne oneToOne = field.getAnnotation(javax.persistence.OneToOne.class);

			Attribute.Builder builder = new Attribute.Builder(enhAttribute.getName()).withColumnName(columnName)
					.withType(attributeClass).withReadMethod(readMethod).withWriteMethod(writeMethod).isId(id)
					.withSqlType(JdbcTypes.sqlTypeFromClass(attributeClass)).isEmbedded(embedded)
					.withEmbeddedAttributes(embeddedAttributes);
			if (oneToOne != null) {
				JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
				builder.withOneToOne(createOneToOne(oneToOne, joinColumn, null));
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

	private String createDefaultJoinColumn(Attribute owningAttribute, Entity toEntity) {
		return owningAttribute.getName() + "_" + toEntity.getId().getColumnName();
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
			for (int i = 0; i < attributes.size(); ++i) {
				Attribute a = attributes.get(i);
//				LOG.info("finalizeRelationships: a.isOneToOne()=" + a.isOneToOne());
				if (a.isOneToOne()) {
					Entity toEntity = entities.get(a.getType().getName());
					LOG.info("finalizeRelationships: toEntity=" + toEntity);
					if (toEntity == null)
						throw new IllegalArgumentException(
								"One to One entity not found (" + a.getType().getName() + ")");

					OneToOne oneToOne = a.getOneToOne();
					if (a.getOneToOne().isOwner() && a.getOneToOne().getJoinColumn() == null) {
						String joinColumnName = createDefaultJoinColumn(a, toEntity);
						oneToOne = a.getOneToOne().copyWithJoinColumn(joinColumnName);
					}

					if (!a.getOneToOne().isOwner()) {
						oneToOne = a.getOneToOne().copyWithOwningEntity(toEntity);
						oneToOne = oneToOne
								.copyWithOwningOneToOne(toEntity.getAttribute(oneToOne.getMappedBy()).getOneToOne());
						oneToOne = oneToOne.copyWithOwningAttribute(toEntity.getAttribute(oneToOne.getMappedBy()));
					}

					Attribute clonedAttribute = a.copyWithOneToOne(oneToOne, toEntity);
					attributes.set(i, clonedAttribute);
				}
			}
		}
	}

}
