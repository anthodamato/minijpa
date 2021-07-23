/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.model;

import java.util.Calendar;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
@Entity
@Table(name = "temporaldateset")
public class TemporalDateSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "datetodate")
    @Temporal(TemporalType.DATE)
    private Date dateToDate;

    @Column(name = "datetotime")
    @Temporal(TemporalType.TIME)
    private Date dateToTime;

    @Column(name = "datetotimestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateToTimestamp;

    @Column(name = "calendartodate")
    @Temporal(TemporalType.DATE)
    private Calendar calendarToDate;

//    @Column(name = "calendartotime")
//    @Temporal(TemporalType.TIME)
//    private Calendar calendarToTime;
    @Column(name = "calendartotimestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar calendarToTimestamp;

    public Long getId() {
	return id;
    }

    public void setId(Long id) {
	this.id = id;
    }

    public Date getDateToDate() {
	return dateToDate;
    }

    public void setDateToDate(Date dateToDate) {
	this.dateToDate = dateToDate;
    }

    public Date getDateToTime() {
	return dateToTime;
    }

    public void setDateToTime(Date dateToTime) {
	this.dateToTime = dateToTime;
    }

    public Date getDateToTimestamp() {
	return dateToTimestamp;
    }

    public void setDateToTimestamp(Date dateToTimestamp) {
	this.dateToTimestamp = dateToTimestamp;
    }

    public Calendar getCalendarToDate() {
	return calendarToDate;
    }

    public void setCalendarToDate(Calendar calendarToDate) {
	this.calendarToDate = calendarToDate;
    }

//    public Calendar getCalendarToTime() {
//	return calendarToTime;
//    }
//
//    public void setCalendarToTime(Calendar calendarToTime) {
//	this.calendarToTime = calendarToTime;
//    }
//
    public Calendar getCalendarToTimestamp() {
	return calendarToTimestamp;
    }

    public void setCalendarToTimestamp(Calendar calendarToTimestamp) {
	this.calendarToTimestamp = calendarToTimestamp;
    }

}
