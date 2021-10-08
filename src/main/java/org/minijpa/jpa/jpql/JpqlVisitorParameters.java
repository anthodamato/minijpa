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
import org.minijpa.jdbc.model.FromTable;
import org.minijpa.jdbc.model.Value;
import org.minijpa.jdbc.model.join.FromJoin;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class JpqlVisitorParameters {

	Map<String, String> aliases = new HashMap<>();
	FromTable fromTable;
	List<FromJoin> fromJoins = new ArrayList<>();
	List<Value> values;
	List<FetchParameter> fetchParameters;
}
