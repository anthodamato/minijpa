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

import javax.persistence.LockModeType;
import org.minijpa.jdbc.LockType;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class LockTypeUtils {

    public static LockType toLockType(LockModeType lockModeType) {
	if (lockModeType == null)
	    return LockType.NONE;

	switch (lockModeType) {
	    case OPTIMISTIC:
	    case READ:
		return LockType.OPTIMISTIC;
	    case OPTIMISTIC_FORCE_INCREMENT:
	    case WRITE:
		return LockType.OPTIMISTIC;
	    case PESSIMISTIC_READ:
		return LockType.PESSIMISTIC_READ;
	    case PESSIMISTIC_WRITE:
		return LockType.PESSIMISTIC_WRITE;
	    case PESSIMISTIC_FORCE_INCREMENT:
		return LockType.PESSIMISTIC_FORCE_INCREMENT;
	    default:
		return LockType.NONE;
	}
    }

    public static LockModeType toLockModeType(LockType lockType) {
	if (lockType == null)
	    return LockModeType.NONE;

	switch (lockType) {
	    case OPTIMISTIC:
		return LockModeType.OPTIMISTIC;
	    case OPTIMISTIC_FORCE_INCREMENT:
		return LockModeType.OPTIMISTIC;
	    case PESSIMISTIC_READ:
		return LockModeType.PESSIMISTIC_READ;
	    case PESSIMISTIC_WRITE:
		return LockModeType.PESSIMISTIC_WRITE;
	    case PESSIMISTIC_FORCE_INCREMENT:
		return LockModeType.PESSIMISTIC_FORCE_INCREMENT;
	    default:
		return LockModeType.NONE;
	}
    }
}
