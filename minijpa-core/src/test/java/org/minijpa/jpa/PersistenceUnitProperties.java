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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.minijpa.jdbc.ConnectionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class PersistenceUnitProperties {

	private static Logger LOG = LoggerFactory.getLogger(PersistenceUnitProperties.class);
	private static ConnectionProperties connectionProperties = new ConnectionProperties();

	public static Map<String, String> getProperties() throws IOException {
		String minijpaTest = System.getProperty("minijpa.test");

		LOG.debug("getProperties: minijpaTest=" + minijpaTest);
		Map<String, String> properties = connectionProperties.load(System.getProperty("minijpa.test"));
		Map<String, String> map = new HashMap<>();
		if (minijpaTest != null && !minijpaTest.isEmpty()) {
			map.put("javax.persistence.jdbc.url", properties.get("url"));
			map.put("javax.persistence.jdbc.driver", properties.get("driver"));
		}

		map.put("javax.persistence.jdbc.user", properties.get("user"));
		map.put("javax.persistence.jdbc.password", properties.get("password"));
		return map;
	}

	public static String getFalseCondition() {
		String minijpaTest = System.getProperty("minijpa.test");
		if (minijpaTest == null || minijpaTest.isBlank())
			return "=false";

		if (minijpaTest.equals("oracle")) {
			return "=0";
		}

		return "=false";
	}

	public static String getTrueCondition() {
		String minijpaTest = System.getProperty("minijpa.test");
		if (minijpaTest == null || minijpaTest.isBlank())
			return "=true";

		if (minijpaTest.equals("oracle")) {
			return "=1";
		}

		return "=true";
	}
}
