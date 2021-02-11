/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.model;

import java.time.OffsetDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author adamato
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "date_of")
    private OffsetDateTime dateOf;
    @OneToOne
    private Customer customer;
    @ManyToMany
    private List<Product> products;

    public Long getId() {
	return id;
    }

    public OffsetDateTime getDateOf() {
	return dateOf;
    }

    public void setDateOf(OffsetDateTime dateOf) {
	this.dateOf = dateOf;
    }

    public Customer getCustomer() {
	return customer;
    }

    public void setCustomer(Customer customer) {
	this.customer = customer;
    }

    public List<Product> getProducts() {
	return products;
    }

    public void setProducts(List<Product> products) {
	this.products = products;
    }

}
