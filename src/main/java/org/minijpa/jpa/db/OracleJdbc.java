package org.minijpa.jpa.db;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.util.Optional;
import org.minijpa.jdbc.DDLData;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.NameTranslator;
import org.minijpa.jdbc.PkSequenceGenerator;
import org.minijpa.jdbc.db.BasicDbJdbc;

public class OracleJdbc extends BasicDbJdbc {

    private final NameTranslator nameTranslator = new OracleNameTranslator();

    @Override
    public NameTranslator getNameTranslator() {
	return nameTranslator;
    }

    @Override
    public String sequenceNextValueStatement(MetaEntity entity) {
	PkSequenceGenerator pkSequenceGenerator = entity.getId().getPkGeneration().getPkSequenceGenerator();
	return "select " + pkSequenceGenerator.getSequenceName() + ".nextval from dual";
    }

    @Override
    public String forUpdate(LockType lockType) {
	if (lockType == LockType.PESSIMISTIC_WRITE)
	    return "for update";

	return "";
    }

    @Override
    public String buildColumnDefinition(Class<?> type, Optional<DDLData> ddlData) {
	if (type == Long.class || (type.isPrimitive() && type.getName().equals("long")))
	    return "number(19)";

	if (type == Integer.class || (type.isPrimitive() && type.getName().equals("int")))
	    return "number(10)";

	if (type == Double.class || (type.isPrimitive() && type.getName().equals("double")))
	    return "double precision";

	if (type == Float.class || (type.isPrimitive() && type.getName().equals("float")))
	    return "number(19,4)";

	if (type == Boolean.class || (type.isPrimitive() && type.getName().equals("boolean")))
	    return "number(1)";

	if (type == Time.class)
	    return "date";

	return super.buildColumnDefinition(type, ddlData);
    }

    @Override
    public String falseOperator() {
	return "= 0";
    }

    @Override
    public String trueOperator() {
	return "= 1";
    }

}
