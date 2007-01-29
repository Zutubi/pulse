package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.util.JDBCUtils;

import javax.sql.DataSource;

import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.BeansException;

/**
 *
 *
 */
public class RemoteDatabaseConsole implements DatabaseConsole, ApplicationContextAware
{
    private DatabaseConfig config;
    private ApplicationContext context;
    private DataSource dataSource;

    public RemoteDatabaseConsole(DatabaseConfig config)
    {
        this.config = config;
    }

    public boolean schemaExists()
    {
        return JDBCUtils.tableExists(dataSource, "RESOURCE");
    }

    public void createSchema()
    {
        LocalSessionFactoryBean factoryBean = (LocalSessionFactoryBean) context.getBean("&sessionFactory");
        factoryBean.createDatabaseSchema();
    }

    public boolean isEmbedded()
    {
        return false;
    }

    public DatabaseConfig getConfig()
    {
        return config;
    }

    public double getDatabaseUsagePercent()
    {
        return -1.0;
    }

    public void stop(boolean force)
    {
        // stop of remote db not supported.
    }

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        context = applicationContext;
    }
}
