/*
 * Copyright (C) 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.minijpa.jdbc.model;

import java.util.List;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public interface SqlStatementGenerator {

    public String export(SqlInsert sqlInsert);

    public String export(SqlUpdate sqlUpdate);

    public String export(SqlDelete sqlDelete);

    public String export(SqlSelect sqlSelect);

    public String export(SqlCreateTable sqlCreateTable);

    public String export(SqlCreateSequence sqlCreateSequence);

    public List<String> export(List<SqlDDLStatement> sqlDDLStatement);

    public String export(SqlCreateJoinTable sqlCreateJoinTable);
}
