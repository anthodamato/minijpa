/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.model;

import javax.persistence.Id;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class JobEmployeeDetails {

	@Id
	int id;
	private String employeeName;
	private String managerName;

	public JobEmployeeDetails(int id, String employeeName, String managerName) {
		this.id = id;
		this.employeeName = employeeName;
		this.managerName = managerName;
	}

	public JobEmployeeDetails(int id, String employeeName) {
		this.id = id;
		this.employeeName = employeeName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

}
