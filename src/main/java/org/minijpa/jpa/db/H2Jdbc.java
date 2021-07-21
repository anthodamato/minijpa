package org.minijpa.jpa.db;

import java.util.Optional;
import org.minijpa.jdbc.DDLData;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.PkSequenceGenerator;
import org.minijpa.jdbc.db.BasicDbJdbc;

public class H2Jdbc extends BasicDbJdbc {

    @Override
    public String sequenceNextValueStatement(MetaEntity entity) {
	PkSequenceGenerator pkSequenceGenerator = entity.getId().getPkGeneration().getPkSequenceGenerator();
	return "CALL NEXT VALUE FOR " + pkSequenceGenerator.getSequenceName() + "";
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
