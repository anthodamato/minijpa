package org.minijpa.jpa.db;

import java.util.Optional;
import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jpa.model.AbstractMetaAttribute;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.RelationshipMetaAttribute;

public interface AttributeFetchParameter extends FetchParameter {

  public AbstractMetaAttribute getAttribute();

  public static AttributeFetchParameter build(MetaAttribute attribute) {
    return new AttributeFetchParameterImpl(attribute.getColumnName(), attribute.getSqlType(),
        attribute, attribute.getAttributeMapper());
  }

  public static AttributeFetchParameter build(RelationshipMetaAttribute attribute) {
    return new AttributeFetchParameterImpl(attribute.getColumnName(), attribute.getSqlType(),
        attribute);
  }

  public static AttributeFetchParameter build(AbstractMetaAttribute attribute) {
    if (attribute instanceof MetaAttribute) {
      return build((MetaAttribute) attribute);
    }

    return build((RelationshipMetaAttribute) attribute);
  }
}
