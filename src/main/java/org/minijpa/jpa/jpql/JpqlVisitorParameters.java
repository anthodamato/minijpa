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
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.model.FromTable;
import org.minijpa.jdbc.model.OrderBy;
import org.minijpa.jdbc.model.Value;
import org.minijpa.jdbc.model.aggregate.GroupBy;
import org.minijpa.jdbc.model.condition.Condition;
import org.minijpa.jdbc.model.join.FromJoin;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class JpqlVisitorParameters {
	
	boolean distinct = false;
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
