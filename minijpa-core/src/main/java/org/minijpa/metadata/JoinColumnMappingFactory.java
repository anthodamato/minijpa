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
package org.minijpa.metadata;

import java.util.Optional;

import org.minijpa.jdbc.relationship.JoinColumnDataList;
import org.minijpa.jpa.db.DbConfiguration;
import org.minijpa.jpa.model.AbstractMetaAttribute;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;
import org.minijpa.jpa.model.relationship.JoinColumnMapping;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public interface JoinColumnMappingFactory {

  public default String createDefaultJoinColumnName(MetaEntity owningEntity,
      RelationshipMetaAttribute owningAttribute,
      AbstractMetaAttribute foreignKeyAttribute) {
    return owningAttribute.getName() + "_" + foreignKeyAttribute.getColumnName();
  }

  public JoinColumnMapping buildSingleJoinColumnMapping(DbConfiguration dbConfiguration,
      RelationshipMetaAttribute a,
      MetaEntity toEntity, Optional<JoinColumnDataList> joinColumnDataList);

  public JoinColumnMapping buildCompositeJoinColumnMapping(DbConfiguration dbConfiguration,
      RelationshipMetaAttribute a,
      MetaEntity toEntity, Optional<JoinColumnDataList> joinColumnDataList);

  public JoinColumnMapping buildJoinColumnMapping(
      DbConfiguration dbConfiguration,
      RelationshipMetaAttribute a,
      MetaEntity toEntity,
      Optional<JoinColumnDataList> joinColumnDataList);
}
