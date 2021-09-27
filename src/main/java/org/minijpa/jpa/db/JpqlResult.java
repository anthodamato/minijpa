/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.db;

import org.minijpa.jdbc.model.SqlStatement;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class JpqlResult {

    private final SqlStatement sqlStatement;
    private final String sql;

    public JpqlResult(SqlStatement sqlStatement, String sql) {
	this.sqlStatement = sqlStatement;
	this.sql = sql;
    }

    public SqlStatement getSqlStatement() {
	return sqlStatement;
    }

    public String getSql() {
	return sql;
    }

}
