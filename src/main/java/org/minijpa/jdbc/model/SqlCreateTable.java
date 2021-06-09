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
package org.minijpa.jdbc.model;

import java.util.List;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.Pk;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class SqlCreateTable implements SqlDDLStatement {

    private final String tableName;
    private final Pk pk;
    private final List<MetaAttribute> attributes;
    private final List<ForeignKeyDeclaration> foreignKeyDeclarations;

    public SqlCreateTable(String tableName,
	    Pk pk,
	    List<MetaAttribute> attributes,
	    List<ForeignKeyDeclaration> foreignKeyDeclarations) {
	this.tableName = tableName;
	this.pk = pk;
	this.attributes = attributes;
	this.foreignKeyDeclarations = foreignKeyDeclarations;
    }

    public String getTableName() {
	return tableName;
    }

    public Pk getPk() {
	return pk;
    }

    public List<MetaAttribute> getAttributes() {
	return attributes;
    }

    public List<ForeignKeyDeclaration> getForeignKeyDeclarations() {
	return foreignKeyDeclarations;
    }

}
