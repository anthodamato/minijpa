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
package org.minijpa.jpa.db;

import java.util.List;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;
import org.minijpa.sql.model.SqlStatement;

/**
 * @author adamato
 */
public class StatementParameters {

  private final SqlStatement sqlStatement;
  private final List<QueryParameter> parameters;
  private StatementType statementType = StatementType.PLAIN;
  private List<MetaEntity> fetchJoinMetaEntities;
  private List<RelationshipMetaAttribute> fetchJoinMetaAttributes;

  public StatementParameters(SqlStatement sqlStatement, List<QueryParameter> parameters) {
    this.sqlStatement = sqlStatement;
    this.parameters = parameters;
  }

  public StatementParameters(
      SqlStatement sqlStatement,
      List<QueryParameter> parameters,
      StatementType statementType,
      List<MetaEntity> fetchJoinMetaEntities,
      List<RelationshipMetaAttribute> fetchJoinMetaAttributes) {
    this.sqlStatement = sqlStatement;
    this.parameters = parameters;
    this.statementType = statementType;
    this.fetchJoinMetaEntities = fetchJoinMetaEntities;
    this.fetchJoinMetaAttributes = fetchJoinMetaAttributes;
  }

  public SqlStatement getSqlStatement() {
    return sqlStatement;
  }

  public List<QueryParameter> getParameters() {
    return parameters;
  }

  public StatementType getStatementType() {
    return statementType;
  }

  public List<MetaEntity> getFetchJoinMetaEntities() {
    return fetchJoinMetaEntities;
  }

  public List<RelationshipMetaAttribute> getFetchJoinMetaAttributes() {
    return fetchJoinMetaAttributes;
  }
}
