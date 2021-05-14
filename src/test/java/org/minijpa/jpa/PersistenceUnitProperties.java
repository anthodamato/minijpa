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
package org.minijpa.jpa;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class PersistenceUnitProperties {

    private static Logger LOG = LoggerFactory.getLogger(PersistenceUnitProperties.class);

    public static Map<String, String> getProperties() {
	String minijpaTest = System.getProperty("minijpa.test");
	if (minijpaTest == null || minijpaTest.isBlank())
	    return null;

	LOG.debug("getProperties: minijpaTest=" + minijpaTest);
	if (minijpaTest.equals("mysql")) {
	    Map<String, String> map = new HashMap<>();
	    map.put("javax.persistence.jdbc.url", "jdbc:mysql://localhost:3306/test?user=root&password=password");
	    map.put("javax.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
	    return map;
	}

	return null;
    }
}
