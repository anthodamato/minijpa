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
 * @param <X>
 * @param <Y>
 */
public interface JdbcAttributeMapper {

    public void setObject(PreparedStatement preparedStatement, int index, Object value) throws SQLException;

//    public Y read(X x);
//
//    public X write(Y y);
}
