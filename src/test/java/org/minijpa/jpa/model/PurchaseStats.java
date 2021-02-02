/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.model;

import java.time.LocalDate;
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
@Table(name = "purchase_stats")
public class PurchaseStats {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "debit_card")
    private Double debitCard;
    @Column(name = "credit_card")
    private Double creditCard;
    private Double cash;

    public Long getId() {
	return id;
    }

    public LocalDate getStartDate() {
	return startDate;
    }

    public void setStartDate(LocalDate startDate) {
	this.startDate = startDate;
    }

    public LocalDate getEndDate() {
	return endDate;
    }

    public void setEndDate(LocalDate endDate) {
	this.endDate = endDate;
    }

    public Double getDebitCard() {
	return debitCard;
    }

    public void setDebitCard(Double debitCard) {
	this.debitCard = debitCard;
    }

    public Double getCreditCard() {
	return creditCard;
    }

    public void setCreditCard(Double creditCard) {
	this.creditCard = creditCard;
    }

    public Double getCash() {
	return cash;
    }

    public void setCash(Double cash) {
	this.cash = cash;
    }

}
