package org.tinyjpa.jdbc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.tinyjpa.jdbc.relationship.FetchType;
import org.tinyjpa.jdbc.relationship.ManyToOne;
import org.tinyjpa.jdbc.relationship.OneToMany;
import org.tinyjpa.jdbc.relationship.OneToOne;

public class Attribute {
//	private Logger LOG = LoggerFactory.getLogger(Attribute.class);
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
	private OneToOne oneToOne;
	private ManyToOne manyToOne;
	private OneToMany oneToMany;
	/**
	 * if this attribute represents an entity, for a one to one relationship for
	 * example, then this field will be that entity.
	 */
	private Entity entity;

	public String getName() {
		return name;
	}

	public String getColumnName() {
		return columnName;
	}

	public Method getReadMethod() {
		return readMethod;
	}

	public Method getWriteMethod() {
		return writeMethod;
	}

	public Class<?> getType() {
		return type;
	}

	public boolean isId() {
		return id;
	}

	public Integer getSqlType() {
		return sqlType;
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

	public OneToOne getOneToOne() {
		return oneToOne;
	}

	public boolean isOneToOne() {
		return oneToOne != null;
	}

	public ManyToOne getManyToOne() {
		return manyToOne;
	}

	public boolean isManyToOne() {
		return manyToOne != null;
	}

	public OneToMany getOneToMany() {
		return oneToMany;
	}

	public boolean isOneToMany() {
		return oneToMany != null;
	}

	public Entity getEntity() {
		return entity;
	}

	public boolean isEntity() {
		return entity != null;
	}

	public void setOneToOne(OneToOne oneToOne) {
		this.oneToOne = oneToOne;
	}

	public void setManyToOne(ManyToOne manyToOne) {
		this.manyToOne = manyToOne;
	}

	public void setOneToMany(OneToMany oneToMany) {
		this.oneToMany = oneToMany;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
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
		if (!isEntity() && !isOneToMany())
			return true;

		if (isOneToOne() && getOneToOne().isOwner())
			return true;

		if (isManyToOne())
			return true;

//		if (isOneToMany() && getOneToMany().isOwner())
//			return true;

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
		if (isOneToOne() && getOneToOne().getFetchType() == FetchType.EAGER)
			return true;

		if (isManyToOne() && getManyToOne().getFetchType() == FetchType.EAGER)
			return true;

		if (isOneToMany() && getOneToMany().getFetchType() == FetchType.EAGER)
			return true;

		return false;
	}

	public boolean isLazy() {
		if (isOneToOne() && getOneToOne().getFetchType() == FetchType.LAZY)
			return true;

		if (isManyToOne() && getManyToOne().getFetchType() == FetchType.LAZY)
			return true;

		if (isOneToMany() && getOneToMany().getFetchType() == FetchType.LAZY)
			return true;

		return false;
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
		private OneToOne oneToOne;
		private ManyToOne manyToOne;
		private OneToMany oneToMany;
		private Entity entity;

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

		public Builder withOneToOne(OneToOne oneToOne) {
			this.oneToOne = oneToOne;
			return this;
		}

		public Builder withManyToOne(ManyToOne manyToOne) {
			this.manyToOne = manyToOne;
			return this;
		}

		public Builder withOneToMany(OneToMany oneToMany) {
			this.oneToMany = oneToMany;
			return this;
		}

		public Builder isEntity(Entity entity) {
			this.entity = entity;
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
//			this.oneToOne = attribute.oneToOne;
//			this.manyToOne = attribute.manyToOne;
//			this.oneToMany = attribute.oneToMany;
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
			attribute.oneToOne = oneToOne;
			attribute.manyToOne = manyToOne;
			attribute.oneToMany = oneToMany;
			attribute.entity = entity;
			return attribute;
		}
	}
}
