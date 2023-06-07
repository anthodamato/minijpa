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
package org.minijpa.jpa.model.relationship;

import java.util.List;

import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.Pk;
import org.minijpa.jpa.model.RelationshipMetaAttribute;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public interface JoinColumnMapping {

  public RelationshipMetaAttribute getAttribute();

  public boolean isComposite();

  public int size();

  public JoinColumnAttribute get(int index);

  public JoinColumnAttribute get();

  public List<JoinColumnAttribute> getJoinColumnAttributes();

  public Pk getForeignKey();

  public boolean isLazy();
}
