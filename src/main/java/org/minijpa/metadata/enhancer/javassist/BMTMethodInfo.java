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
package org.minijpa.metadata.enhancer.javassist;

import java.util.ArrayList;
import java.util.List;

import javassist.CtConstructor;
import javassist.CtMethod;

public class BMTMethodInfo {

    private CtConstructor ctConstructor;
    private CtMethod ctMethod;
    private final List<BMTFieldInfo> bmtFieldInfos = new ArrayList<>();

    public List<BMTFieldInfo> getBmtFieldInfos() {
	return bmtFieldInfos;
    }

    public void addFieldInfos(List<BMTFieldInfo> fieldInfos) {
	bmtFieldInfos.addAll(fieldInfos);
    }

    public CtConstructor getCtConstructor() {
	return ctConstructor;
    }

    public void setCtConstructor(CtConstructor ctConstructor) {
	this.ctConstructor = ctConstructor;
    }

    public CtMethod getCtMethod() {
	return ctMethod;
    }

    public void setCtMethod(CtMethod ctMethod) {
	this.ctMethod = ctMethod;
    }
    
    
}
