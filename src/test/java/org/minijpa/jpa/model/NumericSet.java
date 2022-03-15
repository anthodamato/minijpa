package org.minijpa.jpa.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "numeric_set")
public class NumericSet {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "int_value")
	private Integer intValue;
	@Column(name = "double_value")
	private Double doubleValue;
	@Column(name = "bd_value")
	private BigDecimal bdValue;
	@Column(name = "long_value")
	private Long longValue;

	public Long getId() {
		return id;
	}

	public Integer getIntValue() {
		return intValue;
	}

	public void setIntValue(Integer intValue) {
		this.intValue = intValue;
	}

	public Double getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(Double doubleValue) {
		this.doubleValue = doubleValue;
	}

	public BigDecimal getBdValue() {
		return bdValue;
	}

	public void setBdValue(BigDecimal bdValue) {
		this.bdValue = bdValue;
	}

	public Long getLongValue() {
		return longValue;
	}

	public void setLongValue(Long longValue) {
		this.longValue = longValue;
	}

}
