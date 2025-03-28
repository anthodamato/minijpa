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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetTime;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class OffsetTimeToTimestampObjectConverter implements ObjectConverter<OffsetTime, Timestamp> {
    
    @Override
    public Timestamp convertTo(OffsetTime k) {
	return Timestamp.valueOf(k.atDate(LocalDate.now()).toLocalDateTime());
    }
    
    @Override
    public OffsetTime convertFrom(Timestamp v) {
	return v.toLocalDateTime().toLocalTime().atOffset(OffsetTime.now().getOffset());
    }
    
}
