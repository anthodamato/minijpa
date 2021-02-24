/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jdbc.relationship;

/**
 *
 * @author adamato
 */
public class JoinTableAttributes {

    private String schema;
    private String name;

    public JoinTableAttributes(String schema, String name) {
	this.schema = schema;
	this.name = name;
    }

    public String getSchema() {
	return schema;
    }

    public String getName() {
	return name;
    }

}
