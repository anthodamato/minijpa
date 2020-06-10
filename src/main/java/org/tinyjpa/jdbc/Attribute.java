package org.tinyjpa.jdbc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.tinyjpa.metadata.GeneratedValue;

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

	public Attribute(String name, String columnName, Class<?> type, Method readMethod, Method writeMethod, boolean id,
			Integer sqlType, GeneratedValue generatedValue, boolean embedded, List<Attribute> embeddedAttributes) {
		super();
		this.name = name;
		this.columnName = columnName;
		this.type = type;
		this.readMethod = readMethod;
		this.writeMethod = writeMethod;
		this.id = id;
		this.sqlType = sqlType;
		this.generatedValue = generatedValue;
		this.embedded = embedded;
		this.embeddedAttributes = embeddedAttributes;
	}

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

	public Attribute findChildByName(String attributeName) {
		if (getEmbeddedAttributes() == null)
			return null;

		for (Attribute a : getEmbeddedAttributes()) {
			if (a.getName().equals(attributeName))
				return a;
		}

		return null;
	}

	public List<Attribute> expandAttribute() {
		List<Attribute> list = new ArrayList<>();
//		LOG.info("expandAttribute: embedded=" + embedded + "; name=" + name);
		if (embedded) {
			for (Attribute a : embeddedAttributes) {
				list.addAll(a.expandAttribute());
			}
		} else
			list.add(this);

		return list;
	}

	@Override
	public String toString() {
		return "Name=" + name + "; columnName=" + columnName + "; embedded=" + embedded;
	}

}
