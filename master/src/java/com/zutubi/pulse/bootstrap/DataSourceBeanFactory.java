package com.zutubi.pulse.bootstrap;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 *
 */
public class DataSourceBeanFactory implements FactoryBean, ApplicationContextAware
{
    private ApplicationContext context;

    private BasicDataSource dataSource;

    private MasterConfigurationManager configurationManager;

    public Object getObject() throws Exception
    {
        if (dataSource == null)
        {
            synchronized (this)
            {
                if (dataSource == null)
                {
                    dataSource = createDataSource();

                    // handle some custom processing for embedded databases.
                    DatabaseConfig config = configurationManager.getDatabaseConfig();
                    if (config.isEmbedded())
                    {
                        checkEmbeddedSizeRequirements();
                    }
                }
            }
        }
        return dataSource;
    }

    private void checkEmbeddedSizeRequirements() throws IOException, SQLException
    {
        if (HSQLDBUtils.updateMaxSizeRequired(dataSource))
        {
            HSQLDBUtils.shutdown(dataSource);
            close();
            HSQLDBUtils.updateMaxSize(getDbRoot());
            dataSource = createDataSource();
        }
    }

    private BasicDataSource createDataSource() throws IOException
    {
        DatabaseConfig databaseConfig = configurationManager.getDatabaseConfig();

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(databaseConfig.getDriverClassName());

        //TODO: move this into the database config instance.
        String url = databaseConfig.getUrl();
        url = url.replace("DB_ROOT", getDbRoot().getAbsolutePath());
        dataSource.setUrl(url);

        dataSource.setUsername(databaseConfig.getUsername());
        dataSource.setPassword(databaseConfig.getPassword());

        Map<String, String> connectionProperties = databaseConfig.getConnectionProperties();
        for (String key : connectionProperties.keySet())
        {
            dataSource.addConnectionProperty(key, connectionProperties.get(key));
        }
        return dataSource;
    }

    private File getDbRoot()
    {
        return getConfigurationManager().getUserPaths().getDatabaseRoot();
    }

    public Class getObjectType()
    {
        return DataSource.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.context = applicationContext;
    }

    public void close() throws SQLException
    {
        if (dataSource != null)
        {
            dataSource.close();
            dataSource = null;
        }
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public MasterConfigurationManager getConfigurationManager()
    {
        if (configurationManager == null)
        {
            // look it up manually
            configurationManager = (MasterConfigurationManager) context.getBean("configurationManager");
        }
        return configurationManager;
    }
}
