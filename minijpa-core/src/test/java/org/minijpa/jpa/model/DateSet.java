/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.model;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
@Entity
@Table(name = "dateset")
public class DateSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "util_date")
    private java.util.Date utilDate;

    @Column(name = "util_calendar")
    private java.util.Calendar utilCalendar;

    @Column(name = "sql_date")
    private java.sql.Date sqlDate;

    @Column(name = "sql_time")
    private java.sql.Time sqlTime;

    @Column(name = "sql_timestamp")
    private java.sql.Timestamp sqlTimestamp;

    @Column(name = "local_date")
    private LocalDate localDate;

    @Column(name = "local_time")
    private LocalTime localTime;

    @Column(name = "local_date_time")
    private LocalDateTime localDateTime;

    @Column(name = "offset_time")
    private OffsetTime offsetTime;

    @Column(name = "offset_date_time")
    private OffsetDateTime offsetDateTime;

    @Column(name = "duration")
    private Duration duration;

    @Column(name = "instant")
    private Instant instant;

    @Column(name = "zoned_date_time")
    private ZonedDateTime zonedDateTime;

    public Long getId() {
	return id;
    }

    public void setId(Long id) {
	this.id = id;
    }

    public Date getUtilDate() {
	return utilDate;
    }

    public void setUtilDate(Date utilDate) {
	this.utilDate = utilDate;
    }

    public Calendar getUtilCalendar() {
	return utilCalendar;
    }

    public void setUtilCalendar(Calendar utilCalendar) {
	this.utilCalendar = utilCalendar;
    }

    public java.sql.Date getSqlDate() {
	return sqlDate;
    }

    public void setSqlDate(java.sql.Date sqlDate) {
	this.sqlDate = sqlDate;
    }

    public Time getSqlTime() {
	return sqlTime;
    }

    public void setSqlTime(Time sqlTime) {
	this.sqlTime = sqlTime;
    }

    public Timestamp getSqlTimestamp() {
	return sqlTimestamp;
    }

    public void setSqlTimestamp(Timestamp sqlTimestamp) {
	this.sqlTimestamp = sqlTimestamp;
    }

    public LocalDate getLocalDate() {
	return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
	this.localDate = localDate;
    }

    public LocalTime getLocalTime() {
	return localTime;
    }

    public void setLocalTime(LocalTime localTime) {
	this.localTime = localTime;
    }

    public LocalDateTime getLocalDateTime() {
	return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
	this.localDateTime = localDateTime;
    }

    public OffsetTime getOffsetTime() {
	return offsetTime;
    }

    public void setOffsetTime(OffsetTime offsetTime) {
	this.offsetTime = offsetTime;
    }

    public OffsetDateTime getOffsetDateTime() {
	return offsetDateTime;
    }

    public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
	this.offsetDateTime = offsetDateTime;
    }

    public Duration getDuration() {
	return duration;
    }

    public void setDuration(Duration duration) {
	this.duration = duration;
    }

    public Instant getInstant() {
	return instant;
    }

    public void setInstant(Instant instant) {
	this.instant = instant;
    }

    public ZonedDateTime getZonedDateTime() {
	return zonedDateTime;
    }

    public void setZonedDateTime(ZonedDateTime zonedDateTime) {
	this.zonedDateTime = zonedDateTime;
    }

}
