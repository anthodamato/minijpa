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

public class JavaTypes {

    public static Class<?> getClass(String className) throws ClassNotFoundException {
        if (className.equals("byte"))
            return Byte.TYPE;

        if (className.equals("short"))
            return Short.TYPE;

        if (className.equals("int"))
            return Integer.TYPE;

        if (className.equals("long"))
            return Long.TYPE;

        if (className.equals("float"))
            return Float.TYPE;

        if (className.equals("double"))
            return Double.TYPE;

        if (className.equals("boolean"))
            return Boolean.TYPE;

        if (className.equals("char"))
            return Character.TYPE;

        return Class.forName(className);
    }

    public static Class<?> getWrapperClass(String className) throws ClassNotFoundException {
        if (className.equals("byte"))
            return Byte.class;

        if (className.equals("short"))
            return Short.class;

        if (className.equals("int"))
            return Integer.class;

        if (className.equals("long"))
            return Long.class;

        if (className.equals("float"))
            return Float.class;

        if (className.equals("double"))
            return Double.class;

        if (className.equals("boolean"))
            return Boolean.class;

        if (className.equals("char"))
            return Character.class;

        return Class.forName(className);
    }
}
