/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.model;

import java.sql.Date;
import java.util.Collection;
import javax.persistence.*;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
@Entity
@NamedQuery(name = "notShippedOrders", query = "SELECT DISTINCT o FROM SimpleOrder AS o JOIN o.lineItems AS l WHERE l.shipped = :shipped")
@NamedNativeQuery(
        name = "nativeNotShippedOrders",
        query = "SELECT distinct o.id, o.created_at from simple_order o INNER JOIN simple_order_line_item soli on o.id=soli.SimpleOrder_id inner join line_item l on soli.lineItems_id=l.id where l.shipped=:shipped",
        resultClass = SimpleOrder.class)
@NamedNativeQuery(
        name = "nativeNotShippedOrdersNoType",
        query = "SELECT distinct o.id, o.created_at from simple_order o INNER JOIN simple_order_line_item soli on o.id=soli.SimpleOrder_id inner join line_item l on soli.lineItems_id=l.id where l.shipped=:shipped")
@Table(name = "simple_order")
public class SimpleOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    private java.sql.Date createdAt;

    @OneToMany
    private Collection<LineItem> lineItems;

    public Long getId() {
        return id;
    }

    public Collection<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(Collection<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

}
