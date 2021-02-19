/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jdbc.mapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;

/**
 *
 * @author adamato
 */
public class JdbcOffsetDateTimeMapper implements JdbcAttributeMapper {

    @Override
    public void setObject(PreparedStatement preparedStatement, int index, Object value) throws SQLException {
	OffsetDateTime offsetDateTime = (OffsetDateTime) value;
	preparedStatement.setTimestamp(index, Timestamp.from(offsetDateTime.toInstant()));
    }

}
