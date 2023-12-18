package org.minijpa.jpa.model;

import java.lang.reflect.Method;
import java.util.Optional;

import org.minijpa.jdbc.mapper.AttributeMapper;

public abstract class AbstractMetaAttribute extends AbstractAttribute {

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

    public Optional<AttributeMapper> getAttributeMapper() {
        return Optional.empty();
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
