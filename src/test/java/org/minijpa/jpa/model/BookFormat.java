package org.minijpa.jpa.model;

import javax.persistence.Embeddable;

@Embeddable
public class BookFormat {

    private String format;
    private Integer pages;

    public String getFormat() {
	return format;
    }

    public void setFormat(String format) {
	this.format = format;
    }

    public Integer getPages() {
	return pages;
    }

    public void setPages(Integer pages) {
	this.pages = pages;
    }

}
