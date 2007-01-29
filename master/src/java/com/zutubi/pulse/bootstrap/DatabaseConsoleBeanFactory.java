package com.zutubi.pulse.bootstrap;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;

/**
 *
 *
 */
public class DatabaseConsoleBeanFactory implements FactoryBean, ApplicationContextAware
{
    private MasterConfigurationManager configurationManager;

    private DatabaseConsole instance;
    private ApplicationContext context;

    public Object getObject() throws Exception
    {
        if (instance == null)
        {
            DatabaseConfig config = configurationManager.getDatabaseConfig();
            DataSource dataSource = (DataSource) context.getBean("dataSource");
            if (config.isEmbedded())
            {
                EmbeddedHSQLDBConsole console = new EmbeddedHSQLDBConsole(config);
                console.setDataSource(dataSource);
                console.setApplicationContext(context);
                instance = console;
            }
            else
            {
                RemoteDatabaseConsole console = new RemoteDatabaseConsole(config);
                console.setDataSource(dataSource);
                console.setApplicationContext(context);
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

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        context = applicationContext;
    }
}
