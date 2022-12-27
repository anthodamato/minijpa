package org.minijpa.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "search_data")
public class SearchData {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String model;
    private String pattern;
    private Integer occurences;
    @Column(name = "average_value")
    private Integer averageValue;
    private Float floatAverageValue;

    public Long getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Integer getOccurences() {
        return occurences;
    }

    public void setOccurences(Integer occurences) {
        this.occurences = occurences;
    }

    public Integer getAverageValue() {
        return averageValue;
    }

    public void setAverageValue(Integer averageValue) {
        this.averageValue = averageValue;
    }

    public Float getFloatAverageValue() {
        return floatAverageValue;
    }

    public void setFloatAverageValue(Float floatAverageValue) {
        this.floatAverageValue = floatAverageValue;
    }

}
