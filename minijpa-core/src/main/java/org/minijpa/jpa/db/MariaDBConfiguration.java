/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jpa.db;

import org.minijpa.jdbc.DbTypeMapper;
import org.minijpa.jdbc.JdbcRunner;
import org.minijpa.jdbc.mapper.MariaDBDbTypeMapper;
import org.minijpa.sql.model.MariaDBSqlStatementGenerator;
import org.minijpa.sql.model.SqlStatementGenerator;

public class MariaDBConfiguration implements DbConfiguration {

    private final DbJdbc dbJdbc;
    private final DbTypeMapper dbTypeMapper;
    private final SqlStatementGenerator sqlStatementGenerator;
    private final JdbcRunner jdbcRunner;
    private final SqlStatementFactory sqlStatementFactory;

    public MariaDBConfiguration() {
        super();
        this.dbJdbc = new MariaDBJdbc();
        this.dbTypeMapper = new MariaDBDbTypeMapper();
        this.sqlStatementGenerator = new MariaDBSqlStatementGenerator();
        this.sqlStatementGenerator.init();
        this.jdbcRunner = new JdbcRunner();
        this.sqlStatementFactory = new SqlStatementFactory();
        this.sqlStatementFactory.init();
    }

    @Override
    public DbJdbc getDbJdbc() {
        return dbJdbc;
    }

    @Override
    public DbTypeMapper getDbTypeMapper() {
        return dbTypeMapper;
    }

    @Override
    public SqlStatementGenerator getSqlStatementGenerator() {
        return sqlStatementGenerator;
    }

    @Override
    public JdbcRunner getJdbcRunner() {
        return jdbcRunner;
    }

    @Override
    public SqlStatementFactory getSqlStatementFactory() {
        return sqlStatementFactory;
    }

}
