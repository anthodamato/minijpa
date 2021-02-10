/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jdbc;

/**
 *
 * @author adamato
 */
public class PkSequenceGenerator {

    private String name;
    private String sequenceName;
    private String schema;
    private int allocationSize;
    private int initialValue;
    private String catalog;

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getSequenceName() {
	return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
	this.sequenceName = sequenceName;
    }

    public String getSchema() {
	return schema;
    }

    public void setSchema(String schema) {
	this.schema = schema;
    }

    public int getAllocationSize() {
	return allocationSize;
    }

    public void setAllocationSize(int allocationSize) {
	this.allocationSize = allocationSize;
    }

    public int getInitialValue() {
	return initialValue;
    }

    public void setInitialValue(int initialValue) {
	this.initialValue = initialValue;
    }

    public String getCatalog() {
	return catalog;
    }

    public void setCatalog(String catalog) {
	this.catalog = catalog;
    }

}
