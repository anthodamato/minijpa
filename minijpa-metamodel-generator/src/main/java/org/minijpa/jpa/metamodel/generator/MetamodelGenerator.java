package org.minijpa.jpa.metamodel.generator;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetamodelGenerator {
    private static Logger LOG = LoggerFactory.getLogger(MetamodelGenerator.class);
    private MetamodelExporter metamodelExporter = new MetamodelExporterImpl();
// java -cp minijpa-metamodel-generator/target/minijpa-metamodel-generator-0.0.1-SNAPSHOT.jar:"/Users/adamato/workspace-minijpa/minijpa/minijpa-core/target/test-classes" org.minijpa.jpa.metamodel.generator.MetamodelGenerator manytoone_bid /Users/adamato/workspace-minijpa/minijpa/minijpa-core/src/test/resources/META-INF/persistence.xml /Users/adamato/workspace-minijpa/minijpa/minijpa-core/src/test/java -Dlogback.configurationFile=/Users/adamato/workspace-minijpa/minijpa/minijpa-core/src/test/resources/logback-test.xml

    /**
     * @param args
     *             <ul>
     *             <li>1st param: persistence unit name</li>
     *             <li>2nd param: persistence xml file path</li>
     *             <li>3rd param: entity source file base path</li>
     *             </ul>
     */
    public static void main(String[] args) {
        LOG.info("Persistence Unit Name: {}", args[0]);
        LOG.info("Persistence Xml file path: {}", args[1]);
        LOG.info("Persistence Source Path: {}", args[2]);
        try {
            new MetamodelGenerator().generate(args[0], args[1], args[2]);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public void generate(String persistenceUnitName, String persistenceXmlPath, String sourceBasePath)
            throws Exception {
        PersistenceUnitData persistenceUnitInfo = findPersistenceUnitInfo(persistenceUnitName, persistenceXmlPath);
        LOG.info("Parsing entity classes...");
        EntityMetadataProvider entityMetadataProvider = new EntityMetadataProviderImpl();
        List<EntityMetadata> entityMetadatas = entityMetadataProvider.build(persistenceUnitInfo.getManagedClassNames());

        LOG.info("Exporting metamodel...");
        for (EntityMetadata em : entityMetadatas) {
            metamodelExporter.export(em, sourceBasePath);
        }
    }

    private PersistenceUnitData findPersistenceUnitInfo(String persistenceUnitName, String persistenceXmlPath)
            throws Exception {
        PersistenceUnitData persistenceUnitData = null;
        LOG.info("findPersistenceUnitInfo: emName={}, path={}", persistenceUnitName, persistenceXmlPath);
        persistenceUnitData = new PersistenceProviderHelper().parseXml(persistenceXmlPath, persistenceUnitName, null);
        if (persistenceUnitData == null) {
            LOG.error("Persistence Unit '{}' not found", persistenceUnitName);
            return null;
        }

        return persistenceUnitData;
    }
}
