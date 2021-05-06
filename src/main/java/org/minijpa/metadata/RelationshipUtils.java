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
package org.minijpa.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import org.minijpa.jdbc.relationship.JoinColumnData;
import org.minijpa.jdbc.relationship.JoinColumnDataList;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class RelationshipUtils {

    private static JoinColumnData buildJoinColumnData(JoinColumn joinColumn) {
	return new JoinColumnData(Optional.ofNullable(joinColumn.name()),
		Optional.ofNullable(joinColumn.referencedColumnName()));
    }

    private static Optional<JoinColumnDataList> buildJoinColumnDataList(JoinColumn joinColumn) {
	if (joinColumn == null)
	    return Optional.empty();

	JoinColumnData joinColumnData = buildJoinColumnData(joinColumn);
	JoinColumnDataList joinColumnDataList = new JoinColumnDataList(Arrays.asList(joinColumnData));
	return Optional.of(joinColumnDataList);
    }

    private static Optional<JoinColumnDataList> buildJoinColumnDataList(JoinColumns joinColumns) {
	if (joinColumns == null)
	    return Optional.empty();

	List<JoinColumnData> joinColumnDatas = new ArrayList<>();
	for (JoinColumn joinColumn : joinColumns.value()) {
	    joinColumnDatas.add(buildJoinColumnData(joinColumn));
	}

	return Optional.of(new JoinColumnDataList((List<JoinColumnData>) Collections.unmodifiableList(joinColumnDatas)));
    }

    public static Optional<JoinColumnDataList> buildJoinColumnDataList(
	    JoinColumn joinColumn,
	    JoinColumns joinColumns) {
	if (joinColumn != null && joinColumns != null)
	    throw new IllegalArgumentException("@JoinColumn and @JoinColumns both declared");

	if (joinColumn != null)
	    return buildJoinColumnDataList(joinColumn);

	return buildJoinColumnDataList(joinColumns);
    }
}
