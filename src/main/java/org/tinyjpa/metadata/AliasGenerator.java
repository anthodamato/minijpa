package org.tinyjpa.metadata;

import java.util.Collection;
import java.util.Optional;

import org.tinyjpa.jdbc.MetaEntity;

public class AliasGenerator {
	public String calculateAlias(String tableName, Collection<MetaEntity> parsedEntities) {
		String alias = calculateBasicAlias(tableName);
		int counter = 1;
		while (true) {
			Optional<MetaEntity> optional = aliasExists(alias, parsedEntities);
			if (!optional.isPresent())
				return alias;

			alias = alias + Integer.toString(counter);
			++counter;
		}
	}

	private String calculateBasicAlias(String tableName) {
		StringBuilder sb = new StringBuilder(tableName.substring(0, 1));
		int index = -1;
		while ((index = tableName.indexOf('_', index + 1)) != -1) {
			if (index + 1 < tableName.length())
				sb.append(tableName.charAt(index + 1));
		}

		return sb.toString().toLowerCase();
	}

	private Optional<MetaEntity> aliasExists(String alias, Collection<MetaEntity> parsedEntities) {
		return parsedEntities.stream().filter(e -> e.getAlias().equals(alias)).findFirst();
	}

}
