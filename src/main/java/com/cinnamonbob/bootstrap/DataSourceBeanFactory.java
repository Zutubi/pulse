package com.cinnamonbob.bootstrap;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;

/**
 * 
 *
 */
public class DataSourceBeanFactory implements FactoryBean, ApplicationContextAware
{
    private ApplicationContext context;

    private BasicDataSource dataSource;

    public Object getObject() throws Exception
    {
        if (dataSource == null)
        {
            synchronized(this)
            {
                if (dataSource == null)
                {
                    ConfigurationManager configManager = (ConfigurationManager) context.getBean("configurationManager");
                    File dbRoot = configManager.getApplicationPaths().getDatabaseRoot();

                    dataSource = new BasicDataSource();
                    dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
                    dataSource.setUrl("jdbc:hsqldb:" + dbRoot.getAbsolutePath() + File.separator + "db");
                    dataSource.setUsername("sa");
                    dataSource.setPassword("");
                }
            }
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

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.context = applicationContext;
    }

    public void close() throws SQLException
    {
        if (dataSource != null)
            dataSource.close();
    }
}
