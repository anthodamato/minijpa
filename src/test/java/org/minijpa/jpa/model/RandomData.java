/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author adamato
 */
@Entity
@Table(name = "random_data")
public class RandomData {

	@Id
	@GeneratedValue
	private Long id;

	private Integer fib1;
	private Integer fib2;
	private Integer fib3;
	private Integer fib4;
	private Integer fib5;
	private Integer multiply2;
	@Column(name = "fib_string")
	private String fibString;

	public Long getId() {
		return id;
	}

	public Integer getFib1() {
		return fib1;
	}

	public void setFib1(Integer fib1) {
		this.fib1 = fib1;
	}

	public Integer getFib2() {
		return fib2;
	}

	public void setFib2(Integer fib2) {
		this.fib2 = fib2;
	}

	public Integer getFib3() {
		return fib3;
	}

	public Integer getFib4() {
		return fib4;
	}

	public void setFib4(Integer fib4) {
		this.fib4 = fib4;
	}

	public Integer getFib5() {
		return fib5;
	}

	public void setFib5(Integer fib5) {
		this.fib5 = fib5;
	}

	public void setFib3(Integer fib3) {
		this.fib3 = fib3;
	}

	public Integer getMultiply2() {
		return multiply2;
	}

	public void setMultiply2(Integer multiply2) {
		this.multiply2 = multiply2;
	}

	public String getFibString() {
		return fibString;
	}

	public void setFibString(String fibString) {
		this.fibString = fibString;
	}

}
