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
package org.minijpa.jpa.db;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.persistence.spi.PersistenceUnitInfo;

import org.minijpa.jdbc.ConnectionProvider;
import org.minijpa.jdbc.ScriptRunner;
import org.minijpa.jpa.PersistenceUnitContextManager;
import org.minijpa.metadata.PersistenceUnitContext;
import org.minijpa.sql.model.SqlDDLStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceUnitPropertyActions {

    private Logger LOG = LoggerFactory.getLogger(PersistenceUnitPropertyActions.class);

    private void runScript(String scriptPath, PersistenceUnitInfo persistenceUnitInfo,
            ConnectionProvider connectionProvider) throws IOException, SQLException, URISyntaxException {
        String filePath = scriptPath;
        if (!scriptPath.startsWith("/"))
            filePath = "/" + filePath;

        File file = Paths.get(getClass().getResource(filePath).toURI()).toFile();
        ScriptRunner scriptRunner = new ScriptRunner();
        List<String> statements = scriptRunner.readStatements(file);
        runScript(statements, persistenceUnitInfo, connectionProvider);
    }

    public void runScript(List<String> statements, PersistenceUnitInfo persistenceUnitInfo,
            ConnectionProvider connectionProvider) throws SQLException {
        Connection connection = null;
        try {
            connection = connectionProvider.getConnection();
            ScriptRunner scriptRunner = new ScriptRunner();
            scriptRunner.runDDLStatements(statements, connection);
        } finally {
            if (connection != null)
                try {
                    connection.close();
                } catch (Exception e2) {
                    LOG.error(e2.getMessage());
                }
        }
    }

    public List<String> generateScriptFromMetadata(PersistenceUnitInfo persistenceUnitInfo) {
        try {
            PersistenceUnitContext persistenceUnitContext = PersistenceUnitContextManager.getInstance()
                    .get(persistenceUnitInfo);
            DbConfiguration dbConfiguration = DbConfigurationList.getInstance()
                    .getDbConfiguration(persistenceUnitContext.getPersistenceUnitName());
            List<SqlDDLStatement> sqlStatements = dbConfiguration.getDbJdbc()
                    .buildDDLStatements(persistenceUnitContext.getEntities());
            return dbConfiguration.getSqlStatementGenerator().export(sqlStatements);
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    public void analyzeCreateScripts(PersistenceUnitInfo persistenceUnitInfo, ConnectionProvider connectionProvider)
            throws IOException, SQLException, URISyntaxException {
        Properties properties = persistenceUnitInfo.getProperties();
        LOG.debug("properties={}", properties);
        String action = (String) properties.get("javax.persistence.schema-generation.database.action");
        String createSource = (String) properties.get("javax.persistence.schema-generation.create-source");
        String createScriptSource = (String) properties.get("javax.persistence.schema-generation.create-script-source");
        String dropSource = (String) properties.get("javax.persistence.schema-generation.drop-source");
        String dropScriptSource = (String) properties.get("javax.persistence.schema-generation.drop-script-source");
        LOG.debug("action={}", action);
        if (action == null || action.isEmpty())
            return;

        if (action.equals("create")) {
            if (createSource != null) {
                if (createSource.equals("script")) {
                    if (createScriptSource != null)
                        runScript(createScriptSource, persistenceUnitInfo, connectionProvider);
                } else if (createSource.equals("metadata")) {
                    List<String> script = generateScriptFromMetadata(persistenceUnitInfo);
                    LOG.debug("script={}", script);
                    runScript(script, persistenceUnitInfo, connectionProvider);
                }
            }
        } else if (action.equals("drop-and-create")) {
            if (dropSource != null && dropSource.equals("script")) {
                if (dropScriptSource != null) {
                    runScript(dropScriptSource, persistenceUnitInfo, connectionProvider);
                }
            }

            if (createSource != null && createSource.equals("script")) {
                if (createScriptSource != null) {
                    runScript(createScriptSource, persistenceUnitInfo, connectionProvider);
                }
            }
        }
    }

}
