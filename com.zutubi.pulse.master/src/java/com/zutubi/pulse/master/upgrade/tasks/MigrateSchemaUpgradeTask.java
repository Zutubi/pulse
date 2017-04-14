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

package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.database.DatabaseConfig;
import com.zutubi.pulse.master.hibernate.HackyConnectionProvider;
import com.zutubi.pulse.master.hibernate.MutableConfiguration;
import com.zutubi.pulse.master.hibernate.SchemaRefactor;
import com.zutubi.pulse.master.upgrade.DataSourceAware;
import com.zutubi.pulse.master.upgrade.UpgradeException;
import org.hibernate.cfg.Environment;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * <class-comment/>
 */
public class MigrateSchemaUpgradeTask extends AbstractUpgradeTask implements DataSourceAware
{
    private List<String> mappings = new LinkedList<String>();

    protected DataSource dataSource;

    private DatabaseConfig databaseConfig;

    public String getName()
    {
        return super.getName() + " (" + getBuildNumber() + ")";
    }

    public String getDescription()
    {
        return super.getDescription() + " " + getBuildNumber();
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws UpgradeException
    {
        try
        {
            // manually setup the hibernate configuration
            MutableConfiguration config = new MutableConfiguration();

            // load these properties from the context, same place that all the other
            // properties are defined.
            Properties props = databaseConfig.getHibernateProperties();
            props.put(Environment.CONNECTION_PROVIDER, "com.zutubi.pulse.master.hibernate.HackyConnectionProvider");

            // slight hack to provide hibernate with access to the configured datasource.
            HackyConnectionProvider.dataSource = dataSource;

            // use spring to help load the classpath resources. Rather useful actually.
            config.addClassPathMappings(mappings);

            // run the schema update.
            SchemaRefactor refactor = new SchemaRefactor(config, props);
            refactor.sync();

            List<Exception> exceptions = refactor.getExceptions();
            for (Exception e : exceptions)
            {
                addError(e.getClass().getName() + ": Cause: " + e.getMessage());
            }
        }
        catch (IOException e)
        {
            throw new UpgradeException(e);
        }
    }

    public void setMapping(String mapping)
    {
        this.mappings.clear();
        this.mappings.add(mapping);
    }

    public void setMappings(List<String> mappings)
    {
        this.mappings = mappings;
    }

    /**
     * Required resource.
     *
     * @param dataSource reference
     */
    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void setDatabaseConfig(DatabaseConfig config)
    {
        this.databaseConfig = config;
    }
}
