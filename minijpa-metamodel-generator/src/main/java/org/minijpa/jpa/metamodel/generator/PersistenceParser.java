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
package org.minijpa.jpa.metamodel.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PersistenceParser extends DefaultHandler {

    private Logger LOG = LoggerFactory.getLogger(PersistenceParser.class);
    private PersistenceMetaData persistenceMetaData;
    private Map<String, PersistenceUnitData> persistenceUnitMetaDatas = new HashMap<>();
    private String persistentUnitName;
    private PersistenceUnitData persistenceUnitData;
    private List<String> managedClassNames = new ArrayList<>();
    private boolean startClass;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("persistence-unit")) {
            Map<String, String> map = new HashMap<>();
            readPersistentUnitName(attributes, map);
            persistentUnitName = map.get("name");
        }

        if (qName.equals("class"))
            startClass = true;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("persistence-unit")) {
            if (persistentUnitName == null || persistentUnitName.equals(""))
                LOG.warn("Persistent unit name not set");
            else {
                if (persistentUnitName != null && persistentUnitName.trim().length() > 0) {
                    persistenceUnitData = new PersistenceUnitData.Builder().withName(persistentUnitName)
                            .withManagedClassNames(Collections.unmodifiableList(new ArrayList<>(managedClassNames)))
                            .build();
                }

                persistenceUnitMetaDatas.put(persistentUnitName, persistenceUnitData);
            }

            persistentUnitName = null;
            persistenceUnitData = null;
            managedClassNames.clear();
        }

        if (qName.equals("persistence")) {
            persistenceMetaData = new PersistenceMetaData(Collections.unmodifiableMap(persistenceUnitMetaDatas));
        }

        if (qName.equals("class"))
            startClass = false;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (startClass)
            managedClassNames.add(new String(ch, start, length));
    }

    private void readPersistentUnitName(Attributes attributes, Map<String, String> props) {
        int length = attributes.getLength();
        String value = null;
        for (int i = 0; i < length; ++i) {
            String localName = attributes.getLocalName(i);
            if (localName.equals("name"))
                value = attributes.getValue(i);
        }

        if (value != null && value.length() > 0)
            props.put("name", value);
    }

    public PersistenceMetaData getPersistenceMetaData() {
        return persistenceMetaData;
    }

}
