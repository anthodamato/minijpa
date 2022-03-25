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
package org.minijpa.jdbc.relationship;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class JoinColumnDataList {

    private final List<JoinColumnData> joinColumnDataList;

    public JoinColumnDataList(List<JoinColumnData> joinColumnDataList) {
	this.joinColumnDataList = joinColumnDataList;
    }

    public List<JoinColumnData> getJoinColumnDataList() {
	return joinColumnDataList;
    }

    public Optional<String> getNameByReferenced(String referencedName) {
	Optional<JoinColumnData> o = joinColumnDataList.stream().
		filter(j -> j.getReferencedColumnName().isPresent() && j.getReferencedColumnName().get().equalsIgnoreCase(referencedName)).findFirst();
	return o.isPresent() ? o.get().getName() : Optional.empty();
    }
}
