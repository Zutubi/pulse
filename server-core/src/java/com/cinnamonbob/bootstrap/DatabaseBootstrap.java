package com.cinnamonbob.bootstrap;

import com.cinnamonbob.core.Stoppable;
import com.cinnamonbob.util.JDBCUtils;
import com.cinnamonbob.util.logging.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

import javax.sql.DataSource;
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

    private boolean schemaExists()
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
}
