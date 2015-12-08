package com.zutubi.pulse.master.database;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.FactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

/**
 * A factory bean that provides access to the systems Data Source.
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
                    dataSource = databaseConfig.createDataSource(false);

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
            dataSource = databaseConfig.createDataSource(false);
        }
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
