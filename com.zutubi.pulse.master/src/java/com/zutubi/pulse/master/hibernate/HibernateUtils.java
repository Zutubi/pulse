/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.hibernate;

import com.zutubi.pulse.core.util.JDBCUtils;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * A set of utility methods used when interacting with Hibernate. 
 */
public class HibernateUtils
{

    public static void createSchema(DataSource dataSource, MutableConfiguration configuration) throws SQLException
    {
        Dialect dialect = Dialect.getDialect(configuration.getProperties());
        String[] sqlCreate = configuration.generateSchemaCreationScript(dialect);
        for (String sql : sqlCreate)
        {
            if (sql.startsWith("create"))
            {
                JDBCUtils.execute(dataSource, sql);
            }
        }
    }

    public static void createSchemaConstraints(DataSource dataSource, MutableConfiguration configuration) throws SQLException
    {
        Dialect dialect = Dialect.getDialect(configuration.getProperties());
        String[] sqlAlter = configuration.generateSchemaCreationScript(dialect);
        for (String sql : sqlAlter)
        {
            if (sql.startsWith("alter"))
            {
                JDBCUtils.execute(dataSource, sql);
            }
        }
    }

    public static MutableConfiguration createHibernateConfiguration(List<String> mappings, Properties jdbcProperties) throws IOException
    {
        MutableConfiguration configuration = new MutableConfiguration();
        configuration.addClassPathMappings(mappings);

        Properties hibernateProperties = new Properties();
        hibernateProperties.put(Environment.DIALECT, inferHibernateDialect(jdbcProperties));
        configuration.setProperties(hibernateProperties);

        configuration.buildMappings();

        return configuration;
    }

    public static String inferHibernateDialect(Properties jdbcProperties)
    {
        Map<String, String> databaseTypeToDialectMapping = new HashMap<String, String>();
        databaseTypeToDialectMapping.put("hsqldb", "org.hibernate.dialect.HSQLDialect");
        databaseTypeToDialectMapping.put("mysql", "org.hibernate.dialect.MySQLDialect");
        databaseTypeToDialectMapping.put("postgresql", "org.hibernate.dialect.PostgreSQLDialect");

        String jdbcUrl = jdbcProperties.getProperty("jdbc.url");

        // format jdbc:<databaseType>:<databaseConfig>.  We are after the database type.

        if (!jdbcUrl.startsWith("jdbc:"))
        {
            throw new RuntimeException("Unable to infer hibernate dialect from jdbc url: " + jdbcUrl + ".  Expecting 'jdbc:' prefix.");
        }

        String databaseType = jdbcUrl.substring(5, jdbcUrl.indexOf(':', 5));

        if (!databaseTypeToDialectMapping.containsKey(databaseType))
        {
            throw new RuntimeException("Unsupported database type: " + databaseType + ".  No hibernate dialect available for this database type.");
        }

        return databaseTypeToDialectMapping.get(databaseType);
    }

}
