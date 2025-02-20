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
package org.minijpa.jdbc.mapper;

/**
 * Converts objects of class K to objects of class V and vice versa.
 *
 * @param <K>
 * @param <V>
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public interface ObjectConverter<K, V> {

    /**
     * Convert an object of class K to an object of class V.
     *
     * @param k
     * @return
     */
    V convertTo(K k);

    /**
     * Convert an object of class V to an object of class K.
     *
     * @param v
     * @return
     */
    K convertFrom(V v);
}
