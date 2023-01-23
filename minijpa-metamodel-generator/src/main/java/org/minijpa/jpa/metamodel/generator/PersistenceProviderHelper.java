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

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.XMLReader;

public class PersistenceProviderHelper {

    private Logger LOG = LoggerFactory.getLogger(PersistenceProviderHelper.class);

    public PersistenceUnitInfo parseXml(String filePath, String persistenceUnitName, Map<String, String> properties)
            throws Exception {
        File file = getClass().getResource(filePath) != null
                ? Paths.get(getClass().getResource(filePath).toURI()).toFile()
                : new File(filePath);
        if (file == null)
            throw new Exception("Persistence file '" + filePath + "' not found");

        LOG.info("parseXml: file={}", file);
        SAXParserFactory spf = SAXParserFactory.newInstance();
//		spf.setNamespaceAware(true);
        SAXParser saxParser = spf.newSAXParser();

        XMLReader xmlReader = saxParser.getXMLReader();
        PersistenceParser persistenceParser = new PersistenceParser();
        xmlReader.setContentHandler(persistenceParser);
        xmlReader.parse(file.getAbsolutePath());
        PersistenceMetaData persistenceMetaData = persistenceParser.getPersistenceMetaData();
        LOG.info("parseXml: persistenceMetaData={}", persistenceMetaData);
        if (persistenceMetaData == null) {
            LOG.error("'persistence' element not found, file path: {}", filePath);
            throw new IllegalArgumentException("'persistence' element not found, file path: " + filePath);
        }

        PersistenceUnitInfo persistenceUnitInfo = persistenceMetaData.getPersistenceUnitMetaData(persistenceUnitName);
        if (persistenceUnitInfo == null)
            return null;

        // overwrite properties
        if (properties != null) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                persistenceUnitInfo.getProperties().setProperty(entry.getKey(), entry.getValue());
            }
        }

        return persistenceUnitInfo;
    }

}
