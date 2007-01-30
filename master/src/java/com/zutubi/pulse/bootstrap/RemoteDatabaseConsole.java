package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.upgrade.tasks.MutableConfiguration;
import com.zutubi.pulse.upgrade.tasks.SchemaRefactor;
import com.zutubi.pulse.util.JDBCUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 *
 */
public class RemoteDatabaseConsole implements DatabaseConsole, ApplicationContextAware
{
    private DatabaseConfig config;
    private ApplicationContext context;
    private DataSource dataSource;
    private MutableConfiguration hibernateConfig;
    private Properties hibernateProps;

    public RemoteDatabaseConsole(DatabaseConfig config)
    {
        this.config = config;
    }

    public boolean schemaExists()
    {
        return JDBCUtils.tableExists(dataSource, "RESOURCE");
    }

    public void createSchema() throws SQLException
    {
        SchemaRefactor refactor = new SchemaRefactor(hibernateConfig, hibernateProps);
        refactor.createSchema();
    }

    public void dropSchema() throws SQLException
    {
        SchemaRefactor refactor = new SchemaRefactor(hibernateConfig, hibernateProps);
        refactor.dropSchema();
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

    public void setHibernateConfig(MutableConfiguration config)
    {
        this.hibernateConfig = config;
    }

    public void setHibernateProperties(Properties props)
    {
        this.hibernateProps = props;
    }
}
