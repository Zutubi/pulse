package com.zutubi.pulse.bootstrap;

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
            synchronized (this)
            {
                if (dataSource == null)
                {
                    dataSource = new BasicDataSource();
                    dataSource.setDriverClassName(getDriverClassName());
                    dataSource.setUrl(getUrl());
                    dataSource.setUsername(getUsername());
                    dataSource.setPassword(getPassword());
                }
            }
        }
        return dataSource;
    }

    public String getDriverClassName()
    {
        return "org.hsqldb.jdbcDriver";
    }

    public String getUrl()
    {
        MasterConfigurationManager configManager = (MasterConfigurationManager) context.getBean("configurationManager");
        File dbRoot = configManager.getUserPaths().getDatabaseRoot();
        return "jdbc:hsqldb:" + dbRoot.getAbsolutePath() + File.separator + "db";
    }

    public String getUsername()
    {
        return "sa";
    }

    public String getPassword()
    {
        return "";
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
        }
    }
}
