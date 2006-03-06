package com.cinnamonbob.bootstrap;

import com.cinnamonbob.util.logging.Logger;
import com.cinnamonbob.util.JDBCUtils;
import com.cinnamonbob.core.Stoppable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.beans.BeansException;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
            LocalSessionFactoryBean factoryBean = (LocalSessionFactoryBean) context.getBean("&sessionFactory");
            factoryBean.createDatabaseSchema();
        }
    }

    private boolean schemaExists()
    {
        // does the schema exist? there should be a better way to do this... have a look at the hibernate source...
        Connection con = null;
        CallableStatement stmt = null;
        try
        {
            con = dataSource.getConnection();
            stmt = con.prepareCall("SELECT COUNT(*) FROM " + schemaTestTable);
            stmt.execute();
            return true;
        }
        catch (SQLException e)
        {
            return false;
        }
        finally
        {
            JDBCUtils.close(stmt);
            JDBCUtils.close(con);
        }
    }

    public void stop(boolean force)
    {
        Connection con = null;
        Statement stmt = null;
        try
        {
            con = dataSource.getConnection();
            stmt = con.createStatement();
            stmt.execute("SHUTDOWN");
        }
        catch (SQLException e)
        {
            LOG.error(e);
        }
        finally
        {
            JDBCUtils.close(stmt);
            JDBCUtils.close(con);
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
}
