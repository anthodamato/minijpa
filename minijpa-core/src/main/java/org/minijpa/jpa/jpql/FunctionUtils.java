/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.minijpa.jpa.jpql;

import org.minijpa.sql.model.Value;
import org.minijpa.sql.model.function.Avg;
import org.minijpa.sql.model.function.Count;
import org.minijpa.sql.model.function.Max;
import org.minijpa.sql.model.function.Min;
import org.minijpa.sql.model.function.Sum;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class FunctionUtils {

    public static Value createAggregateFunction(
            AggregateFunctionType aggregateFunctionType,
            Object argument,
            boolean distinct) {
        switch (aggregateFunctionType) {
            case AVG:
                return new Avg(argument);
            case COUNT:
                return new Count(argument, distinct);
            case MAX:
                return new Max(argument);
            case MIN:
                return new Min(argument);
            case SUM:
                return new Sum(argument);
            default:
                break;
        }

        throw new IllegalArgumentException("Unknown aggregate function type: " + aggregateFunctionType);
    }

}
