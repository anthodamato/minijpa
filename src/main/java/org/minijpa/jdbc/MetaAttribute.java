package org.minijpa.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.minijpa.jdbc.mapper.JdbcAttributeMapper;

import org.minijpa.jdbc.relationship.FetchType;
import org.minijpa.jdbc.relationship.Relationship;

public class MetaAttribute extends AbstractAttribute {

    private String name;
    private Method readMethod;
    private Method writeMethod;
    private boolean id;
    private PkGeneration pkGeneration;
    private boolean embedded;
    private List<MetaAttribute> embeddedAttributes;
    private Relationship relationship;
    private boolean collection = false;
    private Field javaMember;
    // calculated fields
    private List<MetaAttribute> expandedAttributeList;
    private boolean nullable = true;

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

    public PkGeneration getPkGeneration() {
	return pkGeneration;
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

    public boolean isCollection() {
	return collection;
    }

    public boolean isNullable() {
	return nullable;
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
	if (embedded)
	    for (MetaAttribute a : embeddedAttributes) {
		list.addAll(a.expand());
	    }
	else if (expandRelationship())
	    list.add(this);

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

    @Override
    public String toString() {
	return "(Name=" + name + "; columnName=" + columnName + "; embedded=" + embedded + ")";
    }

    public static class Builder {

	private final String name;
	private String columnName;
	private Class<?> type;
	private Class<?> readWriteDbType;
	private DbTypeMapper dbTypeMapper;
	private Method readMethod;
	private Method writeMethod;
	private boolean id;
	private Integer sqlType;
	private PkGeneration pkGeneration;
	private boolean embedded;
	private List<MetaAttribute> embeddedAttributes;
	private Relationship relationship;
	private boolean collection = false;
	private Field javaMember;
	private JdbcAttributeMapper jdbcAttributeMapper;
	private Class<?> collectionImplementationClass;
	private boolean nullable = true;

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

	public Builder withPkGeneration(PkGeneration generatedValue) {
	    this.pkGeneration = generatedValue;
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

	public Builder withJdbcAttributeMapper(JdbcAttributeMapper jdbcAttributeMapper) {
	    this.jdbcAttributeMapper = jdbcAttributeMapper;
	    return this;
	}

	public Builder withCollectionImplementationClass(Class<?> collectionImplementationClass) {
	    this.collectionImplementationClass = collectionImplementationClass;
	    return this;
	}

	public Builder isNullable(boolean nullable) {
	    this.nullable = nullable;
	    return this;
	}

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
	    attribute.pkGeneration = pkGeneration;
	    attribute.embedded = embedded;
	    attribute.embeddedAttributes = embeddedAttributes;
	    attribute.relationship = relationship;
	    attribute.collection = collection;
	    attribute.javaMember = javaMember;
	    attribute.jdbcAttributeMapper = jdbcAttributeMapper;
	    attribute.collectionImplementationClass = collectionImplementationClass;
	    attribute.nullable = nullable;
	    return attribute;
	}
    }
}
