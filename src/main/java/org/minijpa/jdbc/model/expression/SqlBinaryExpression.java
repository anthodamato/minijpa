/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jdbc.model.expression;

import java.util.Optional;
import org.minijpa.jdbc.model.TableColumn;
import org.minijpa.jdbc.model.Value;

/**
 *
 * @author adamato
 */
public class SqlBinaryExpression implements SqlExpression, Value {

    private final SqlExpressionOperator operator;
    private Optional<TableColumn> leftTableColumn = Optional.empty();
    private Optional<String> leftExpression = Optional.empty();
    private Optional<TableColumn> rightTableColumn = Optional.empty();
    private Optional<String> rightExpression = Optional.empty();

    public SqlBinaryExpression(SqlExpressionOperator operator, TableColumn leftTableColumn, TableColumn rightTableColumn) {
	this.operator = operator;
	this.leftTableColumn = Optional.of(leftTableColumn);
	this.rightTableColumn = Optional.of(rightTableColumn);
    }

    public SqlBinaryExpression(SqlExpressionOperator operator, TableColumn leftTableColumn, String rightExpression) {
	this.operator = operator;
	this.leftTableColumn = Optional.of(leftTableColumn);
	this.rightExpression = Optional.of(rightExpression);
    }

    public SqlBinaryExpression(SqlExpressionOperator operator, String leftExpression, TableColumn rightTableColumn) {
	this.operator = operator;
	this.leftExpression = Optional.of(leftExpression);
	this.rightTableColumn = Optional.of(rightTableColumn);
    }

    public SqlBinaryExpression(SqlExpressionOperator operator, String leftExpression, String rightExpression) {
	this.operator = operator;
	this.leftExpression = Optional.of(leftExpression);
	this.rightExpression = Optional.of(rightExpression);
    }

    public SqlBinaryExpression(SqlExpressionOperator operator, TableColumn leftTableColumn, String leftExpression, TableColumn rightTableColumn, String rightExpression) {
	this.operator = operator;
	this.leftTableColumn = Optional.ofNullable(leftTableColumn);
	this.leftExpression = Optional.ofNullable(leftExpression);
	this.rightTableColumn = Optional.ofNullable(rightTableColumn);
	this.rightExpression = Optional.ofNullable(rightExpression);
    }

    @Override
    public SqlExpressionOperator getOperator() {
	return operator;
    }

    public Optional<TableColumn> getLeftTableColumn() {
	return leftTableColumn;
    }

    public Optional<String> getLeftExpression() {
	return leftExpression;
    }

    public Optional<TableColumn> getRightTableColumn() {
	return rightTableColumn;
    }

    public Optional<String> getRightExpression() {
	return rightExpression;
    }

}
