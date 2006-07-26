package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.pulse.util.logging.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Hibernate specific bootstrap support that creates the database scheme
 * if it does not already exist in the configured database.
 */
public class DatabaseBootstrap implements ApplicationContextAware, Stoppable
{
    public static final String DEFAULT_SCHEMA_TEST_TABLE = "RESOURCE";

    private static final Logger LOG = Logger.getLogger(DatabaseBootstrap.class);

    private DataSource dataSource;
    private String schemaTestTable = DEFAULT_SCHEMA_TEST_TABLE;

    private ApplicationContext context;
    private static final long TOTAL_DB_SPACE = (long)Integer.MAX_VALUE * 8;

    public DatabaseBootstrap()
    {
    }

    public void initialiseDatabase()
    {
        if (!schemaExists())
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
    }

    public boolean schemaExists()
    {
        return JDBCUtils.tableExists(dataSource, schemaTestTable);
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

    public void setSchemaTestTable(String testTable)
    {
        this.schemaTestTable = testTable;
    }

    public void setApplicationContext(ApplicationContext context) throws BeansException
    {
        this.context = context;
    }

    public void setReferentialIntegrity(boolean b)
    {
        try
        {
            JDBCUtils.execute(dataSource, "SET REFERENTIAL INTEGRITY " + ((b) ? "TRUE" : "FALSE"));
        }
        catch (SQLException e)
        {
            LOG.error(e);
        }
    }

    public void compactDatabase()
    {
        try
        {
            JDBCUtils.execute(dataSource, "CHECKPOINT DEFRAG");
        }
        catch (SQLException e)
        {
            LOG.error(e);
        }
    }

    public double getDatabaseUsagePercent()
    {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            con = dataSource.getConnection();
            stmt = con.prepareCall("SELECT file_free_pos FROM information_schema.system_cacheinfo");
            rs = stmt.executeQuery();
            if(rs.next())
            {
                Long freePos = JDBCUtils.getLong(rs, "file_free_pos");
                if(freePos != null)
                {
                    return freePos * 100.0 / TOTAL_DB_SPACE;
                }
            }
        }
        catch (SQLException e)
        {
            LOG.severe(e);
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
            JDBCUtils.close(con);
        }

        return -1.0;
    }
}
