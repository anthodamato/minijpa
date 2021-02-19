/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jdbc.mapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author adamato
 */
public class JdbcStringEnumMapper implements JdbcAttributeMapper {

    @Override
    public void setObject(PreparedStatement preparedStatement, int index, Object value) throws SQLException {
	Enum e = (Enum) value;
	preparedStatement.setString(index, e.name());
    }

}
