package com.zutubi.pulse.master.database;

import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.hibernate.MutableConfiguration;
import com.zutubi.pulse.master.hibernate.SchemaRefactor;
import com.zutubi.util.logging.Logger;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 *
 */
public class EmbeddedHSQLDBConsole implements DatabaseConsole
{
    private static final Logger LOG = Logger.getLogger(EmbeddedHSQLDBConsole.class);

    private DatabaseConfig databaseConfig;
    private DataSource dataSource;
    private MutableConfiguration hibernateConfig;
    private Properties hibernateProps;

    public EmbeddedHSQLDBConsole(DatabaseConfig config)
    {
        this.databaseConfig = config;
    }

    public boolean isEmbedded()
    {
        return true;
    }

    public boolean schemaExists()
    {
        return JDBCUtils.tableExists(dataSource, "PROJECT");
    }

    public void createSchema() throws SQLException
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

        SchemaRefactor refactor = new SchemaRefactor(hibernateConfig, hibernateProps);
        refactor.createSchema();

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

    public void dropSchema() throws SQLException
    {
        SchemaRefactor refactor = new SchemaRefactor(hibernateConfig, hibernateProps);
        refactor.dropSchema();
    }

    public DatabaseConfig getConfig()
    {
        return databaseConfig;
    }

    public double getDatabaseUsagePercent()
    {
        return HSQLDBUtils.getDatabaseUsagePercent(dataSource);
    }

    public void postSchemaHook()
    {
    }

    public void postUpgradeHook(boolean changes)
    {
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

    public void setHibernateConfig(MutableConfiguration config)
    {
        this.hibernateConfig = config;
    }

    public void setHibernateProperties(Properties props)
    {
        this.hibernateProps = props;
    }
}
