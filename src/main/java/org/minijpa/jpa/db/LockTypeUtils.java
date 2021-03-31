/*
 * Copyright (C) 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
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
}
