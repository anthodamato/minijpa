/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jdbc.mapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 *
 * @author adamato
 */
public class JdbcLocalDateMapper implements JdbcAttributeMapper {

    @Override
    public void setObject(PreparedStatement preparedStatement, int index, Object value) throws SQLException {
	preparedStatement.setDate(index, Date.valueOf((LocalDate) value));
    }

}
