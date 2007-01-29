package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.pulse.util.logging.Logger;

import javax.sql.DataSource;
import java.sql.SQLException;

import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.beans.BeansException;

/**
 *
 *
 */
public class EmbeddedHSQLDBConsole implements DatabaseConsole, ApplicationContextAware
{
    private static final Logger LOG = Logger.getLogger(EmbeddedHSQLDBConsole.class);

    private DatabaseConfig config;
    private DataSource dataSource;
    private ApplicationContext context;

    public EmbeddedHSQLDBConsole(DatabaseConfig config)
    {
        this.config = config;
    }

    public boolean isEmbedded()
    {
        return true;
    }

    public boolean schemaExists()
    {
        return JDBCUtils.tableExists(dataSource, "RESOURCE");
    }

    public void createSchema()
    {
        try
        {
            JDBCUtils.execute(dataSource, "SET PROPERTY \"hsqldb.default_table_type\" 'cached'");
            JDBCUtils.execute(dataSource, "SET PROPERTY \"hsqldb.cache_file_scale\" 8");
        }
        catch (SQLException e)
        {
            LOG.error(e);
        }

        LocalSessionFactoryBean factoryBean = (LocalSessionFactoryBean) context.getBean("&sessionFactory");
        factoryBean.createDatabaseSchema();

        // add custom configuration of the hsql database here.
        try
        {
            // the delay between data being written to the database, and it being flushed
            // to disk. Default is 20.
            JDBCUtils.execute(dataSource, "SET WRITE_DELAY 5");
        }
        catch (SQLException e)
        {
            LOG.error(e);
        }
    }

    public DatabaseConfig getConfig()
    {
        return config;
    }

    public double getDatabaseUsagePercent()
    {
        return HSQLDBUtils.getDatabaseUsagePercent(dataSource);
    }

    public void stop(boolean force)
    {
        try
        {
            JDBCUtils.execute(dataSource, "SHUTDOWN COMPACT");
        }
        catch (SQLException e)
        {
            LOG.error(e);
        }
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
