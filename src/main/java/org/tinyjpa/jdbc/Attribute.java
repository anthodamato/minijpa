package org.tinyjpa.jdbc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.tinyjpa.jdbc.relationship.FetchType;
import org.tinyjpa.jdbc.relationship.Relationship;

public class Attribute extends AbstractAttribute {
//	private Logger LOG = LoggerFactory.getLogger(Attribute.class);
	private String name;
	private Method readMethod;
	private Method writeMethod;
	private boolean id;
	private GeneratedValue generatedValue;
	private boolean embedded;
	private List<Attribute> embeddedAttributes;
	private Relationship relationship;

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

	public GeneratedValue getGeneratedValue() {
		return generatedValue;
	}

	public boolean isEmbedded() {
		return embedded;
	}

	public List<Attribute> getEmbeddedAttributes() {
		return embeddedAttributes;
	}

	public Relationship getRelationship() {
		return relationship;
	}

	public void setRelationship(Relationship relationship) {
		this.relationship = relationship;
	}

	public Attribute findChildByName(String attributeName) {
		if (getEmbeddedAttributes() == null)
			return null;

		for (Attribute a : getEmbeddedAttributes()) {
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

	public List<Attribute> expand() {
		List<Attribute> list = new ArrayList<>();
//		LOG.info("expandAttribute: embedded=" + embedded + "; name=" + name);
		if (embedded) {
			for (Attribute a : embeddedAttributes) {
				list.addAll(a.expand());
			}
		} else if (expandRelationship()) {
			list.add(this);
		}

		return list;
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

	@Override
	public String toString() {
		return "(Name=" + name + "; columnName=" + columnName + "; embedded=" + embedded + ")";
	}

	public static class Builder {
		private String name;
		private String columnName;
		private Class<?> type;
		private Method readMethod;
		private Method writeMethod;
		private boolean id;
		private Integer sqlType;
		private GeneratedValue generatedValue;
		private boolean embedded;
		private List<Attribute> embeddedAttributes;
		private Relationship relationship;

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

		public Builder withGeneratedValue(GeneratedValue generatedValue) {
			this.generatedValue = generatedValue;
			return this;
		}

		public Builder isEmbedded(boolean embedded) {
			this.embedded = embedded;
			return this;
		}

		public Builder withEmbeddedAttributes(List<Attribute> embeddedAttributes) {
			this.embeddedAttributes = embeddedAttributes;
			return this;
		}

		public Builder withRelationship(Relationship relationship) {
			this.relationship = relationship;
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

		public Attribute build() {
			Attribute attribute = new Attribute();
			attribute.name = name;
			attribute.columnName = columnName;
			attribute.type = type;
			attribute.readMethod = readMethod;
			attribute.writeMethod = writeMethod;
			attribute.id = id;
			attribute.sqlType = sqlType;
			attribute.generatedValue = generatedValue;
			attribute.embedded = embedded;
			attribute.embeddedAttributes = embeddedAttributes;
			attribute.relationship = relationship;
			return attribute;
		}
	}
}
