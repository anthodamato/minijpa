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
package org.minijpa.jdbc.relationship;

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
    public int count() {
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

}
