package org.minijpa.jpa.model;

import java.lang.reflect.Method;
import java.util.Optional;
import org.minijpa.jdbc.mapper.AttributeMapper;

public abstract class AbstractMetaAttribute extends AbstractAttribute {

  // attribute name
  protected String name;
  // methods for read/write ops
  protected Method readMethod;
  protected Method writeMethod;
  // calculated fields
  protected boolean nullable = true;

  public String getName() {
    return name;
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

}
