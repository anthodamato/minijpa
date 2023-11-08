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
package org.minijpa.jdbc;

public enum Database {
    UNKNOWN("unknown"), // Unknown database
    APACHE_DERBY("derby"), // Apache Derby
    MYSQL("mysql"), // MySQL
    MARIADB("mariadb"), // MariaDB
    POSTGRES("postgres"), // PostgresSQL
    ORACLE("oracle"), // Oracle
    H2("h2") // H2
    ;

    private String dbId;

    Database(String dbid) {
        this.dbId = dbid;
    }

    public String getDbId() {
        return dbId;
    }

    public static Database getDatabaseById(String db) {
        if (db == null || db.trim().length() == 0)
            return APACHE_DERBY;

        for (Database database : values()) {
            if (database.dbId.equals(db))
                return database;
        }

        return Database.UNKNOWN;
    }

}
