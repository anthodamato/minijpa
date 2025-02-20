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
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class RelationshipUtils {

    private static JoinColumnData buildJoinColumnData(JoinColumn joinColumn) {
        return new JoinColumnData(Optional.ofNullable(joinColumn.name()),
                Optional.ofNullable(joinColumn.referencedColumnName()));
    }

    private static JoinColumnDataList buildJoinColumnDataList(JoinColumn joinColumn) {
        if (joinColumn == null)
            return null;

        JoinColumnData joinColumnData = buildJoinColumnData(joinColumn);
        return new JoinColumnDataList(List.of(joinColumnData));
    }

    private static JoinColumnDataList buildJoinColumnDataList(JoinColumns joinColumns) {
        if (joinColumns == null)
            return null;

        List<JoinColumnData> joinColumnDatas = new ArrayList<>();
        for (JoinColumn joinColumn : joinColumns.value()) {
            joinColumnDatas.add(buildJoinColumnData(joinColumn));
        }

        return new JoinColumnDataList((List<JoinColumnData>) Collections.unmodifiableList(joinColumnDatas));
    }

    public static JoinColumnDataList buildJoinColumnDataList(JoinColumn joinColumn, JoinColumns joinColumns) {
        if (joinColumn != null && joinColumns != null)
            throw new IllegalArgumentException("@JoinColumn and @JoinColumns both declared");

        if (joinColumn != null)
            return buildJoinColumnDataList(joinColumn);

        return buildJoinColumnDataList(joinColumns);
    }
}
