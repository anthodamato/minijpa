/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jdbc.model;

import java.util.List;
import org.minijpa.jdbc.QueryParameter;

/**
 *
 * @author adamato
 */
public class StatementParameters {

    private final SqlStatement sqlStatement;
    private final List<QueryParameter> parameters;

    public StatementParameters(SqlStatement sqlStatement, List<QueryParameter> parameters) {
	this.sqlStatement = sqlStatement;
	this.parameters = parameters;
    }

    public SqlStatement getSqlStatement() {
	return sqlStatement;
    }

    public List<QueryParameter> getParameters() {
	return parameters;
    }

}
