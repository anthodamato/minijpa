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

import javassist.CtMethod;

public class PropertyMethod {

    CtMethod method;
    boolean enhance = true;
    /**
     * true if the method must be created.
     */
    boolean add = false;
    boolean exists = false;
    boolean create = false;

    public PropertyMethod() {
    }

    public PropertyMethod(CtMethod method, boolean enhance) {
        this.method = method;
        this.enhance = enhance;
    }

    @Override
    public String toString() {
        return "PropertyMethod{" +
                "method=" + method +
                ", enhance=" + enhance +
                ", add=" + add +
                ", create=" + create +
                '}';
    }
}
