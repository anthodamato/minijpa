/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.model;

import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 *
 * @author adamato
 */
@Entity
@Table(name = "property")
public class Property {

    @GeneratedValue
    @Id
    private Long id;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false)
    private PropertyType propertyType;

    private String address;

    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<PropertyOwner> owners;

    public Long getId() {
	return id;
    }

    public PropertyType getPropertyType() {
	return propertyType;
    }

    public void setPropertyType(PropertyType propertyType) {
	this.propertyType = propertyType;
    }

    public String getAddress() {
	return address;
    }

    public void setAddress(String address) {
	this.address = address;
    }

    public Collection<PropertyOwner> getOwners() {
	return owners;
    }

    public void setOwners(Collection<PropertyOwner> owners) {
	this.owners = owners;
    }

}
