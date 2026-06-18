package org.minijpa.jpa.model;

import java.util.Collection;

import javax.persistence.*;

@Entity
@Table(name = "store")
public class Store {

	@Id
	@GeneratedValue
	private Long id;

	private String name;

	@OneToMany
	@JoinTable(name = "store_items")
	private Collection<Item> items;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<Item> getItems() {
		return items;
	}

	public void setItems(Collection<Item> items) {
		this.items = items;
	}

}
