package org.minijpa.jpa.db;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Optional;
import org.minijpa.jdbc.DDLData;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.PkGenerationType;
import org.minijpa.jdbc.PkSequenceGenerator;
import org.minijpa.jdbc.PkStrategy;
import org.minijpa.jdbc.db.BasicDbJdbc;

public class MySQLJdbc extends BasicDbJdbc {

    @Override
    public PkStrategy findPkStrategy(PkGenerationType pkGenerationType) {
	PkStrategy pkStrategy = super.findPkStrategy(pkGenerationType);
	if (pkStrategy == PkStrategy.SEQUENCE)
	    return PkStrategy.IDENTITY;

	return pkStrategy;
    }

    @Override
    public String sequenceNextValueStatement(MetaEntity entity) {
	PkSequenceGenerator pkSequenceGenerator = entity.getId().getPkGeneration().getPkSequenceGenerator();
	return "VALUES (NEXT VALUE FOR " + pkSequenceGenerator.getSequenceName() + ")";
    }

    @Override
    public String forUpdate(LockType lockType) {
	if (lockType == LockType.PESSIMISTIC_WRITE)
	    return "for update";

	return "";
    }

    @Override
    public String buildColumnDefinition(Class<?> type, Optional<DDLData> ddlData) {
	if (type == Timestamp.class || type == Calendar.class || type == LocalDateTime.class
		|| type == Instant.class || type == ZonedDateTime.class)
	    return "datetime(6)";

	return super.buildColumnDefinition(type, ddlData);
    }

}
