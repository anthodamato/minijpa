package org.tinyjpa.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tinyjpa.jdbc.relationship.FetchType;
import org.tinyjpa.jdbc.relationship.Relationship;

public class MetaAttribute extends AbstractAttribute {
	private String name;
	private Method readMethod;
	private Method writeMethod;
	private boolean id;
	private PkGeneration generatedValue;
	private boolean embedded;
	private List<MetaAttribute> embeddedAttributes;
	private Relationship relationship;
	private boolean collection = false;
	private Field javaMember;
	// calculated fields
	private List<MetaAttribute> expandedAttributeList;

	public String getName() {
		return name;
	}

	public Method getReadMethod() {
		return readMethod;
	}

	public Method getWriteMethod() {
		return writeMethod;
	}

	public boolean isId() {
		return id;
	}

	public PkGeneration getGeneratedValue() {
		return generatedValue;
	}

	public boolean isEmbedded() {
		return embedded;
	}

	public List<MetaAttribute> getEmbeddedAttributes() {
		return embeddedAttributes;
	}

	public Relationship getRelationship() {
		return relationship;
	}

	public void setRelationship(Relationship relationship) {
		this.relationship = relationship;
	}

	public Field getJavaMember() {
		return javaMember;
	}

	public MetaAttribute findChildByName(String attributeName) {
		if (getEmbeddedAttributes() == null)
			return null;

		for (MetaAttribute a : getEmbeddedAttributes()) {
			if (a.getName().equals(attributeName))
				return a;
		}

		return null;
	}

	protected boolean expandRelationship() {
		if (relationship == null)
			return true;

		return false;
	}

	public List<MetaAttribute> expand() {
		if (expandedAttributeList != null)
			return expandedAttributeList;

		List<MetaAttribute> list = new ArrayList<>();
//		LOG.info("expandAttribute: embedded=" + embedded + "; name=" + name);
		if (embedded) {
			for (MetaAttribute a : embeddedAttributes) {
				list.addAll(a.expand());
			}
		} else if (expandRelationship()) {
			list.add(this);
		}

		expandedAttributeList = Collections.unmodifiableList(list);
		return expandedAttributeList;
	}

	public boolean isEager() {
		if (relationship == null)
			return false;

		return relationship.getFetchType() == FetchType.EAGER;
	}

	public boolean isLazy() {
		if (relationship == null)
			return false;

		return relationship.getFetchType() == FetchType.LAZY;
	}

	public boolean isCollection() {
		return collection;
	}

	@Override
	public String toString() {
		return "(Name=" + name + "; columnName=" + columnName + "; embedded=" + embedded + ")";
	}

	public static class Builder {
		private String name;
		private String columnName;
		private Class<?> type;
		private Class<?> readWriteDbType;
		private DbTypeMapper dbTypeMapper;
		private Method readMethod;
		private Method writeMethod;
		private boolean id;
		private Integer sqlType;
		private PkGeneration generatedValue;
		private boolean embedded;
		private List<MetaAttribute> embeddedAttributes;
		private Relationship relationship;
		private boolean collection = false;
		private Field javaMember;

		public Builder(String name) {
			super();
			this.name = name;
			this.columnName = name;
		}

		public Builder withColumnName(String columnName) {
			this.columnName = columnName;
			return this;
		}

		public Builder withType(Class<?> type) {
			this.type = type;
			return this;
		}

		public Builder withReadWriteDbType(Class<?> readWriteDbType) {
			this.readWriteDbType = readWriteDbType;
			return this;
		}

		public Builder withDbTypeMapper(DbTypeMapper dbTypeMapper) {
			this.dbTypeMapper = dbTypeMapper;
			return this;
		}

		public Builder withReadMethod(Method readMethod) {
			this.readMethod = readMethod;
			return this;
		}

		public Builder withWriteMethod(Method writeMethod) {
			this.writeMethod = writeMethod;
			return this;
		}

		public Builder isId(boolean id) {
			this.id = id;
			return this;
		}

		public Builder withSqlType(Integer sqlType) {
			this.sqlType = sqlType;
			return this;
		}

		public Builder withGeneratedValue(PkGeneration generatedValue) {
			this.generatedValue = generatedValue;
			return this;
		}

		public Builder isEmbedded(boolean embedded) {
			this.embedded = embedded;
			return this;
		}

		public Builder withEmbeddedAttributes(List<MetaAttribute> embeddedAttributes) {
			this.embeddedAttributes = embeddedAttributes;
			return this;
		}

		public Builder withRelationship(Relationship relationship) {
			this.relationship = relationship;
			return this;
		}

		public Builder isCollection(boolean collection) {
			this.collection = collection;
			return this;
		}

		public Builder withJavaMember(Field field) {
			this.javaMember = field;
			return this;
		}

//		public Builder with(Attribute attribute) {
//			this.name = attribute.name;
//			this.columnName = attribute.columnName;
//			this.type = attribute.type;
//			this.readMethod = attribute.readMethod;
//			this.writeMethod = attribute.writeMethod;
//			this.id = attribute.id;
//			this.sqlType = attribute.sqlType;
//			this.generatedValue = attribute.generatedValue;
//			this.embedded = attribute.embedded;
//			this.embeddedAttributes = attribute.embeddedAttributes;
//			this.entity = attribute.entity;
//			return this;
//		}

		public MetaAttribute build() {
			MetaAttribute attribute = new MetaAttribute();
			attribute.name = name;
			attribute.columnName = columnName;
			attribute.type = type;
			attribute.readWriteDbType = readWriteDbType;
			attribute.dbTypeMapper = dbTypeMapper;
			attribute.readMethod = readMethod;
			attribute.writeMethod = writeMethod;
			attribute.id = id;
			attribute.sqlType = sqlType;
			attribute.generatedValue = generatedValue;
			attribute.embedded = embedded;
			attribute.embeddedAttributes = embeddedAttributes;
			attribute.relationship = relationship;
			attribute.collection = collection;
			attribute.javaMember = javaMember;
			return attribute;
		}
	}
}
