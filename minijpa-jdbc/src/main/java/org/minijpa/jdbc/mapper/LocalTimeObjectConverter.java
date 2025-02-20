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

import java.sql.Time;
import java.time.LocalTime;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class LocalTimeObjectConverter implements ObjectConverter<LocalTime, Time> {

    @Override
    public Time convertTo(LocalTime k) {
        return Time.valueOf(k);
    }

    @Override
    public LocalTime convertFrom(Time v) {
        return v.toLocalTime();
    }

}
