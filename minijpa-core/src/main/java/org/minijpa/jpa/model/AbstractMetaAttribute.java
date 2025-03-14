package org.minijpa.jpa.model;

import org.minijpa.jdbc.DDLData;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.mapper.ObjectConverter;
import org.minijpa.jpa.db.AttributeFetchParameter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class AbstractMetaAttribute extends AbstractAttribute implements AttributeFetchParameter {

    // attribute name
    protected String name;
    // The attribute path. If this is a basic attribute the path is the attribute
    // name.
    // If the parent is an embeddable the path is the embeddable path. For example
    // 'jobInfo.jobDescription'.
    protected String path;
    // methods for read/write ops
    protected Method readMethod;
    protected Method writeMethod;
    // calculated fields
    protected boolean nullable = true;
    protected Field javaMember;
    protected ObjectConverter objectConverter;
    protected DDLData ddlData;

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Method getReadMethod() {
        return readMethod;
    }

    public Method getWriteMethod() {
        return writeMethod;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isEager() {
        return true;
    }

    public boolean isLazy() {
        return false;
    }

    public ObjectConverter getObjectConverter() {
        return objectConverter;
    }

    public DDLData getDdlData() {
        return ddlData;
    }

    public Field getJavaMember() {
        return javaMember;
    }

    @Override
    public AbstractMetaAttribute getAttribute() {
        return this;
    }

    public Object getValue(Object parentInstance)
            throws Exception {
        return getReadMethod().invoke(parentInstance);
    }

    @Override
    public QueryParameter queryParameter(Object value) {
        return new QueryParameter(columnName, value, sqlType, getObjectConverter());
    }

    @Override
    public String toString() {
        return "AbstractMetaAttribute{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", readMethod=" + readMethod +
                ", writeMethod=" + writeMethod +
                ", nullable=" + nullable +
                '}';
    }
}
