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

package com.zutubi.pulse.master.database;

import com.zutubi.pulse.master.hibernate.HackyConnectionProvider;
import com.zutubi.pulse.master.hibernate.MutableConfiguration;
import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.FactoryBean;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

/**
 *
 *
 */
public class DatabaseConsoleBeanFactory implements FactoryBean
{
    private DatabaseConsole instance;

    private DatabaseConfig databaseConfig;

    private DataSource dataSource;
    
    // awkard... having the console know about the hibernate mappings means we can not use it
    // as easily for tasks that do not require mappings.
    private List<String> mappings;

    public Object getObject() throws Exception
    {
        if (instance == null)
        {
            Properties props = new Properties();
            props.putAll(databaseConfig.getProperties());
            props.put(Environment.CONNECTION_PROVIDER, "com.zutubi.pulse.master.hibernate.HackyConnectionProvider");

            // a) retrieve hibernate mappings for schema generation.
            MutableConfiguration config = new MutableConfiguration();
            config.addClassPathMappings(mappings);

            // slight hack to provide hibernate with access to the configured datasource.
            HackyConnectionProvider.dataSource = dataSource;

            // awkard... having the console know about the hibernate mappings means we can not use it 
            // as easily for tasks that do not require mappings.

            if (databaseConfig.getUrl().contains(":hsqldb:"))
            {
                EmbeddedHSQLDBConsole console = new EmbeddedHSQLDBConsole(databaseConfig);
                console.setDataSource(dataSource);
                console.setHibernateConfig(config);
                console.setHibernateProperties(props);
                instance = console;
            }
            else
            {
                RemoteDatabaseConsole console = new RemoteDatabaseConsole(databaseConfig);
                console.setDataSource(dataSource);
                console.setHibernateConfig(config);
                console.setHibernateProperties(props);
                instance = console;
            }
        }
        return instance;
    }

    public Class getObjectType()
    {
        return DatabaseConsole.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void setDatabaseConfig(DatabaseConfig databaseConfig)
    {
        this.databaseConfig = databaseConfig;
    }

    public void setHibernateMappings(List<String> mappings)
    {
        this.mappings = mappings;
    }
}
