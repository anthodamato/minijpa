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

import java.util.Optional;
import org.minijpa.jdbc.DDLData;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.PkSequenceGenerator;
import org.minijpa.jdbc.db.BasicDbJdbc;

public class PostgresJdbc extends BasicDbJdbc {

    @Override
    public String sequenceNextValueStatement(MetaEntity entity) {
	PkSequenceGenerator pkSequenceGenerator = entity.getId().getPkGeneration().getPkSequenceGenerator();
	return "select nextval('" + pkSequenceGenerator.getSequenceName() + "')";
    }

    @Override
    public String forUpdate(LockType lockType) {
	if (lockType == LockType.PESSIMISTIC_WRITE)
	    return "for update";

	return "";
    }

    @Override
    public String buildColumnDefinition(Class<?> type, Optional<DDLData> ddlData) {
	if (type == Double.class || (type.isPrimitive() && type.getName().equals("double")))
	    return "double precision";

	if (type == Float.class || (type.isPrimitive() && type.getName().equals("float")))
	    return "real";

	return super.buildColumnDefinition(type, ddlData);
    }

}
