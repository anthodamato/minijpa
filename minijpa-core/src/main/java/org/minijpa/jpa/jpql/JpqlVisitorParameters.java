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
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.sql.model.FromTable;
import org.minijpa.sql.model.OrderBy;
import org.minijpa.sql.model.Value;
import org.minijpa.sql.model.aggregate.GroupBy;
import org.minijpa.sql.model.condition.Condition;
import org.minijpa.sql.model.join.FromJoin;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class JpqlVisitorParameters {

	boolean distinct = false;
	/**
	 * Association between jpql alias and entity alias.
	 */
	Map<String, String> aliases = new HashMap<>();
	MetaEntity sourceEntity;
	List<FromTable> fromTables = new ArrayList<>();
	List<FromJoin> fromJoins = new ArrayList<>();
	List<Value> values = new ArrayList<>();
	// column aliases that can be used in subqueries
	Map<String, List<Value>> resultVariables = new HashMap<>();
	List<FetchParameter> fetchParameters = new ArrayList<>();
	List<Condition> conditions = new ArrayList<>();
	MetaEntity identificationVariableEntity;
	GroupBy groupBy;
	List<OrderBy> orderByList = new ArrayList<>();
}
