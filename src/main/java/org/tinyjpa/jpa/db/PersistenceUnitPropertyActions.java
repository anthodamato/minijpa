package org.tinyjpa.jpa.db;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Properties;

import javax.persistence.spi.PersistenceUnitInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.ConnectionProviderImpl;
import org.tinyjpa.jdbc.ScriptRunner;

public class PersistenceUnitPropertyActions {
	private Logger LOG = LoggerFactory.getLogger(PersistenceUnitPropertyActions.class);

	public void analyze(PersistenceUnitInfo persistenceUnitInfo) throws URISyntaxException, IOException,
			InstantiationException, IllegalAccessException, ClassNotFoundException {
		Properties properties = persistenceUnitInfo.getProperties();
		LOG.info("properties=" + properties);
		String action = (String) properties.get("javax.persistence.schema-generation.database.action");
		LOG.info("action=" + action);
		if (action != null && action.equals("create")) {
			String createSource = (String) properties.get("javax.persistence.schema-generation.create-source");
			if (createSource != null && createSource.equals("script")) {
				String createScriptSource = (String) properties
						.get("javax.persistence.schema-generation.create-script-source");
				if (createScriptSource != null) {
					String filePath = createScriptSource;
					if (!createScriptSource.startsWith("/"))
						filePath = "/" + createScriptSource;

					File file = Paths.get(getClass().getResource(filePath).toURI()).toFile();
					Connection connection = null;
					try {
						connection = new ConnectionProviderImpl(persistenceUnitInfo).getConnection();
						new ScriptRunner().run(file, connection);
					} catch (Exception e) {
						LOG.error(e.getMessage());
					} finally {
						if (connection != null)
							try {
								connection.close();
							} catch (Exception e2) {
								LOG.error(e2.getMessage());
							}
					}
				}
			}
		}
	}
}
