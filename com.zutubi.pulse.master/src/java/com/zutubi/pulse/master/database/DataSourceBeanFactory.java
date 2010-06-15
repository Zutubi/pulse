package com.zutubi.pulse.master.database;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.FactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import com.zutubi.pulse.master.xwork.interceptor.ReadOnlyAwareBasicDataSource;

/**
 *
 */
public class DataSourceBeanFactory implements FactoryBean
{
    private BasicDataSource dataSource;

    private DatabaseConfig databaseConfig;

    public Object getObject() throws Exception
    {
        if (dataSource == null)
        {
            synchronized (this)
            {
                if (dataSource == null)
                {
                    dataSource = createDataSource();

                    // handle some custom processing for embedded hsql databases.
                    if (isHsqldb())
                    {
                        checkEmbeddedSizeRequirements();
                    }
                }
            }
        }
        return dataSource;
    }

    private boolean isHsqldb()
    {
        return databaseConfig.getUrl().contains(":hsqldb:");
    }

    private boolean isInMemoryHsqldb()
    {
        return databaseConfig.getUrl().contains(":hsqldb:mem:");
    }

    private void checkEmbeddedSizeRequirements() throws IOException, SQLException
    {
        // are we dealing with the in memory version of hsql? If so, no changes to the
        // properties file are required.
        if (isInMemoryHsqldb())
        {
            return;
        }
        
        if (HSQLDBUtils.updateMaxSizeRequired(dataSource))
        {
            HSQLDBUtils.shutdown(dataSource);
            dataSource.close();
            HSQLDBUtils.updateMaxSize(databaseConfig.getUrl());
            dataSource = createDataSource();
        }
    }

    private BasicDataSource createDataSource() throws IOException
    {
        BasicDataSource dataSource = new ReadOnlyAwareBasicDataSource();

        // We don't set the driver class as it is loaded earlier (potentially
        // from a dynamic classpath).
        dataSource.setUrl(databaseConfig.getUrl());
        dataSource.setUsername(databaseConfig.getUsername());
        dataSource.setPassword(databaseConfig.getPassword());

        dataSource.setInitialSize(databaseConfig.getPoolInitialSize());
        dataSource.setMaxActive(databaseConfig.getPoolMaxActive());
        dataSource.setMaxIdle(databaseConfig.getPoolMaxIdle());
        dataSource.setMinIdle(databaseConfig.getPoolMinIdle());
        dataSource.setMaxWait(databaseConfig.getPoolMaxWait());

        // configure the dataSource using the custom connection properties.
        Properties connectionProperties = databaseConfig.getConnectionProperties();
        for (Object o : connectionProperties.keySet())
        {
            String propertyName = (String) o;
            dataSource.addConnectionProperty(propertyName, connectionProperties.getProperty(propertyName));
        }

        return dataSource;
    }

    public Class getObjectType()
    {
        return DataSource.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

    public void close() throws SQLException
    {
        if (dataSource != null)
        {
            dataSource.close();
            dataSource = null;
        }
    }

    public void setDatabaseConfig(DatabaseConfig databaseConfig)
    {
        this.databaseConfig = databaseConfig;
    }
}
