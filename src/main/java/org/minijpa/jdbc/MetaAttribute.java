/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.minijpa.jdbc.mapper.JdbcAttributeMapper;

import org.minijpa.jdbc.relationship.FetchType;
import org.minijpa.jdbc.relationship.Relationship;

public class MetaAttribute extends AbstractAttribute {

    private String name;
    private Method readMethod;
    private Method writeMethod;
    private boolean id;
    private Relationship relationship;
    private boolean collection = false;
    private Field javaMember;
    // calculated fields
    private List<MetaAttribute> expandedAttributeList;
    private boolean nullable = true;
    // it'a a version attribute
    private boolean version = false;
    // it's a basic attribute
    private boolean basic;
    // The attribute path. If this is a basic attribute the path is the attribute name.
    // If the parent is an embeddable the path is the embeddable path. For example 'jobInfo.jobDescription'.
    private String path;
    private Optional<Method> joinColumnReadMethod = Optional.empty();
    private Optional<Method> joinColumnWriteMethod = Optional.empty();
    private Optional<DDLData> ddlData = Optional.empty();

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

    public boolean isVersion() {
	return version;
    }

    public void setVersion(boolean version) {
	this.version = version;
    }

    public boolean isBasic() {
	return basic;
    }

    public String getPath() {
	return path;
    }

    public Optional<Method> getJoinColumnReadMethod() {
	return joinColumnReadMethod;
    }

    public Optional<Method> getJoinColumnWriteMethod() {
	return joinColumnWriteMethod;
    }

    public Optional<DDLData> getDdlData() {
	return ddlData;
    }

    public List<MetaAttribute> expand() {
	if (expandedAttributeList != null)
	    return expandedAttributeList;

	List<MetaAttribute> list = new ArrayList<>();
	if (relationship == null)
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

	return relationship.isLazy();
    }

    @Override
    public String toString() {
	return super.toString() + "; (Name=" + name + "; columnName=" + columnName + ")";
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
	private Relationship relationship;
	private boolean collection = false;
	private Field javaMember;
	private JdbcAttributeMapper jdbcAttributeMapper;
	private Class<?> collectionImplementationClass;
	private boolean nullable = true;
	private boolean version = false;
	private boolean basic;
	private String path;
	private Optional<Method> joinColumnReadMethod;
	private Optional<Method> joinColumnWriteMethod;
	private Optional<DDLData> ddlData = Optional.empty();

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

	public Builder isVersion(boolean version) {
	    this.version = version;
	    return this;
	}

	public Builder isBasic(boolean basic) {
	    this.basic = basic;
	    return this;
	}

	public Builder withPath(String path) {
	    this.path = path;
	    return this;
	}

	public Builder withJoinColumnReadMethod(Optional<Method> joinColumnReadMethod) {
	    this.joinColumnReadMethod = joinColumnReadMethod;
	    return this;
	}

	public Builder withJoinColumnWriteMethod(Optional<Method> joinColumnWriteMethod) {
	    this.joinColumnWriteMethod = joinColumnWriteMethod;
	    return this;
	}

	public Builder withDDLData(Optional<DDLData> ddlData) {
	    this.ddlData = ddlData;
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
	    attribute.relationship = relationship;
	    attribute.collection = collection;
	    attribute.javaMember = javaMember;
	    attribute.jdbcAttributeMapper = jdbcAttributeMapper;
	    attribute.collectionImplementationClass = collectionImplementationClass;
	    attribute.nullable = nullable;
	    attribute.version = version;
	    attribute.basic = basic;
	    attribute.path = path;
	    attribute.joinColumnReadMethod = joinColumnReadMethod;
	    attribute.joinColumnWriteMethod = joinColumnWriteMethod;
	    attribute.ddlData = ddlData;
	    return attribute;
	}
    }
}
