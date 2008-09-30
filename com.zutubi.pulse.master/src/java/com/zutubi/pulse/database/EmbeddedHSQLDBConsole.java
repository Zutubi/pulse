package com.zutubi.pulse.database;

import com.zutubi.pulse.hibernate.MutableConfiguration;
import com.zutubi.pulse.hibernate.SchemaRefactor;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.util.logging.Logger;
import org.apache.commons.dbcp.BasicDataSource;

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

    public void postRestoreHook(boolean restored)
    {
        // This full restart of the database is required as HSQL handling of
        // foreign key constraints seems to go awry after constraints are
        // added to an existing table and the same Database instance
        // continues to be used.  This could even happen after a single
        // unclean restart as in this case the constraints could be replayed
        // from the db.log file.  A full shutdown here refreshes the
        // Database instance for this run of Pulse, and the compaction
        // ensures that no replays occur from db.log.
        if(restored)
        {
            stop(true);
            try
            {
                ((BasicDataSource)dataSource).close();
            }
            catch (SQLException e)
            {
                LOG.severe(e);
            }
        }
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
