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
package org.minijpa.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <T>
 * @author adamato
 */
public class ModelValueArray<T> {

    private Logger LOG = LoggerFactory.getLogger(ModelValueArray.class);
    private final List<T> models = new ArrayList<>();
    private final List<Object> values = new ArrayList<>();

    public ModelValueArray() {
    }

    public ModelValueArray(List<T> models, List<Object> values) {
        this.models.addAll(models);
        this.values.addAll(values);
    }

    public void add(T model, Object value) {
        models.add(model);
        values.add(value);
    }

    public void add(ModelValueArray<T> modelValueArray) {
        for (int i = 0; i < modelValueArray.size(); ++i) {
            add(modelValueArray.getModel(i), modelValueArray.getValue(i));
        }
    }

    public List<T> getModels() {
        return models;
    }

    public List<Object> getValues() {
        return values;
    }

    public T getModel(int index) {
        return models.get(index);
    }

    public Object getValue(int index) {
        return values.get(index);
    }

    public int indexOfModel(T model) {
        for (int i = 0; i < size(); ++i) {
            if (getModel(i) == model)
                return i;
        }

        return -1;
    }

    public int indexOfModel(Function<T, ?> p, Object subModel) {
        for (int i = 0; i < size(); ++i) {
            if (p.apply(getModel(i)) == subModel)
                return i;
        }

        return -1;
    }

    public boolean isEmpty() {
        return models.isEmpty();
    }

    public int size() {
        return models.size();
    }

}
