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

import java.util.Arrays;
import java.util.List;
import org.minijpa.jdbc.JoinColumnAttribute;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.Pk;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class SingleJoinColumnMapping implements JoinColumnMapping {

	private final JoinColumnAttribute joinColumnAttribute;
	private final MetaAttribute attribute;
	private final Pk pk;

	public SingleJoinColumnMapping(JoinColumnAttribute joinColumnAttribute, MetaAttribute attribute, Pk pk) {
		this.joinColumnAttribute = joinColumnAttribute;
		this.attribute = attribute;
		this.pk = pk;
	}

	@Override
	public MetaAttribute getAttribute() {
		return attribute;
	}

	@Override
	public boolean isComposite() {
		return false;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public JoinColumnAttribute get(int index) {
		if (index == 0)
			return joinColumnAttribute;

		throw new IndexOutOfBoundsException("Index '" + index + "' out of bounds");
	}

	@Override
	public JoinColumnAttribute get() {
		return joinColumnAttribute;
	}

	@Override
	public Pk getForeignKey() {
		return pk;
	}

	@Override
	public boolean isLazy() {
		return joinColumnAttribute.getAttribute().isLazy();
	}

	@Override
	public List<JoinColumnAttribute> getJoinColumnAttributes() {
		return Arrays.asList(joinColumnAttribute);
	}

}
