package com.zutubi.pulse.master.database;

import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.hibernate.MutableConfiguration;
import com.zutubi.pulse.master.hibernate.SchemaRefactor;
import com.zutubi.util.logging.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 *
 */
public class RemoteDatabaseConsole implements DatabaseConsole, ApplicationContextAware
{
    private static final Logger LOG = Logger.getLogger(RemoteDatabaseConsole.class);

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
        return JDBCUtils.tableExists(dataSource, "PROJECT");
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

    public void postSchemaHook()
    {
        checkPostgres();
    }

    public void postUpgradeHook(boolean changes)
    {
        if(changes)
        {
            checkPostgres();
        }
    }

    private void checkPostgres()
    {
        try
        {
            Connection connection = dataSource.getConnection();

            // For PostgreSQL, we manually force indexes for all foreign key
            // constraints.
            if (JDBCUtils.getDBType(connection) == JDBCUtils.DbType.POSTGRESQL)
            {
                checkIndices(connection.getMetaData());
            }
        }
        catch (SQLException e)
        {
            LOG.warning(e);
        }
    }

    private void checkIndices(DatabaseMetaData meta) throws SQLException
    {
        ResultSet rs = null;
        try
        {
            rs = meta.getTables(null, null, null, null);
            while(rs.next())
            {
                checkTable(meta, rs.getString("TABLE_NAME"));
            }
        }
        finally
        {
            JDBCUtils.close(rs);
        }
    }

    private void checkTable(DatabaseMetaData meta, String table) throws SQLException
    {
        Map<String, String> expectedConstraints = new HashMap<String, String>();
        ResultSet rs = null;

        try
        {
            rs = meta.getImportedKeys(null, null, table);
            while(rs.next())
            {
                expectedConstraints.put("fki_" + rs.getString("FK_NAME"), rs.getString("FKCOLUMN_NAME"));
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            rs = null;
        }

        try
        {
            rs = meta.getIndexInfo(null, null, table, false, false);
            while(rs.next())
            {
                expectedConstraints.remove(rs.getString("INDEX_NAME"));
            }
        }
        finally
        {
            JDBCUtils.close(rs);
        }

        for(Map.Entry<String, String> toCreate: expectedConstraints.entrySet())
        {
            createIndex(meta.getConnection(), table, toCreate.getKey(), toCreate.getValue());
        }
    }

    private void createIndex(Connection connection, String table, String name, String column) throws SQLException
    {
        PreparedStatement statement = null;
        try
        {
            LOG.info("Adding index '" + name + "' to table '" + table + "' for foreign key column '" + column + "'");
            statement = connection.prepareStatement("CREATE INDEX " + name + " ON " + table + " USING btree (" + column + ")");
            statement.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(statement);
        }
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
