package com.cinnamonbob.bootstrap;

import com.cinnamonbob.util.logging.Logger;
import com.cinnamonbob.util.jdbc.JDBCUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Hibernate specific bootstrap support that creates the database scheme
 * if it does not already exist in the configured database.
 */
public class DatabaseBootstrap
{
    public static final String DEFAULT_SCHEMA_TEST_TABLE = "RESOURCE";

    private static final Logger LOG = Logger.getLogger(DatabaseBootstrap.class);

    private DataSource dataSource;
    private String schemaTestTable = DEFAULT_SCHEMA_TEST_TABLE;

    public DatabaseBootstrap()
    {
    }

    private ApplicationContext getContext()
    {
        return ComponentContext.getContext();
    }

    public void initialiseDatabase()
    {
        if (!schemaExists())
        {
            LocalSessionFactoryBean factoryBean = (LocalSessionFactoryBean) getContext().getBean("&sessionFactory");
            factoryBean.createDatabaseSchema();
        }
    }

    public boolean schemaExists()
    {
        // does the schema exist? there should be a better way to do this... have a look at the hibernate source...
        try
        {
            Connection con = dataSource.getConnection();
            return JDBCUtils.tableExists(con, schemaTestTable);
        }
        catch (SQLException e)
        {
            return false;
        }
    }

    /**
     * Required resource. This database provides access to the database being bootstrapped.
     *
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    /**
     * Specify the schema test table, the name of the table used to check if the schema has
     * been setup.
     *
     * @param testTable
     */
    public void setSchemaTestTable(String testTable)
    {
        this.schemaTestTable = testTable;
    }

}
