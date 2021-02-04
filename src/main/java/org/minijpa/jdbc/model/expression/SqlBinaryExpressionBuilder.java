/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jdbc.model.expression;

import org.minijpa.jdbc.model.TableColumn;

public class SqlBinaryExpressionBuilder {

    private final SqlExpressionOperator operator;
    private TableColumn leftTableColumn;
    private String leftExpression;
    private TableColumn rightTableColumn;
    private String rightExpression;

     public SqlBinaryExpressionBuilder(SqlExpressionOperator operator) {
	this.operator = operator;
    }

    public SqlBinaryExpressionBuilder setLeftTableColumn(TableColumn leftTableColumn) {
	this.leftTableColumn = leftTableColumn;
	return this;
    }

    public SqlBinaryExpressionBuilder setLeftExpression(String leftExpression) {
	this.leftExpression = leftExpression;
	return this;
    }

    public SqlBinaryExpressionBuilder setRightTableColumn(TableColumn rightTableColumn) {
	this.rightTableColumn = rightTableColumn;
	return this;
    }

    public SqlBinaryExpressionBuilder setRightExpression(String rightExpression) {
	this.rightExpression = rightExpression;
	return this;
    }

    public SqlBinaryExpression build() {
	return new SqlBinaryExpression(operator, leftTableColumn, leftExpression, rightTableColumn, rightExpression);
    }

}
