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
package org.minijpa.jdbc.mapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.time.OffsetTime;
import java.util.Calendar;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author adamato
 */
public class JdbcOffsetTimeMapper implements JdbcAttributeMapper {

    private Logger LOG = LoggerFactory.getLogger(JdbcOffsetTimeMapper.class);

    @Override
    public void setObject(PreparedStatement preparedStatement, int index, Object value) throws SQLException {
	OffsetTime offsetTime = (OffsetTime) value;
	Time time = Time.valueOf(offsetTime.toLocalTime());
	Calendar calendar = Calendar.getInstance();
	calendar.setTimeZone(TimeZone.getDefault());
	preparedStatement.setTime(index, time, calendar);
    }

}
