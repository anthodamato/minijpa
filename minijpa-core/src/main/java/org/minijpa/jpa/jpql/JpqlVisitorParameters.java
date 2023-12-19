/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.jpql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jpa.db.StatementType;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;
import org.minijpa.sql.model.FromTable;
import org.minijpa.sql.model.OrderBy;
import org.minijpa.sql.model.Value;
import org.minijpa.sql.model.aggregate.GroupBy;
import org.minijpa.sql.model.condition.Condition;
import org.minijpa.sql.model.join.FromJoin;

import javax.persistence.Parameter;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class JpqlVisitorParameters {

    public boolean distinct = false;
    /**
     * Association between jpql alias and entity alias.
     */
    Map<String, String> aliases = new HashMap<>();
    public MetaEntity sourceEntity;
    public List<FromTable> fromTables = new ArrayList<>();
    public List<FromJoin> fromJoins = new ArrayList<>();
    public List<Value> values = new ArrayList<>();
    // column aliases that can be used in subqueries
    Map<String, List<Value>> resultVariables = new HashMap<>();
    public List<FetchParameter> fetchParameters = new ArrayList<>();
    public List<Condition> conditions = new ArrayList<>();
    public MetaEntity identificationVariableEntity;
    public GroupBy groupBy;
    public List<OrderBy> orderByList = new ArrayList<>();
    public List<QueryParameter> parameters = new ArrayList<>();
    Map<Parameter<?>, Object> parameterMap = Map.of();
    Map<String, Object> hints;
    public StatementType statementType = StatementType.PLAIN;
    public List<MetaEntity> fetchJoinMetaEntities = new ArrayList<>();
    public List<RelationshipMetaAttribute> fetchJoinMetaAttributes = new ArrayList<>();
}