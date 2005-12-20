package com.cinnamonbob.bootstrap;

import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import com.cinnamonbob.util.logging.Logger;

/**
 * Hibernate specific bootstrap support that creates the database scheme
 * if it does not already exist in the configured database.
 */
public class DatabaseBootstrap
{
    public static final String DEFAULT_SCHEMA_TEST_TABLE = "USER";

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

    private boolean schemaExists()
    {
        // does the schema exist? there should be a better way to do this... have a look at the hibernate source...
        try
        {
            Connection con = dataSource.getConnection();
            CallableStatement stmt = con.prepareCall("SELECT COUNT(*) FROM " + schemaTestTable);
            stmt.execute();
            return true;
        }
        catch (SQLException e)
        {
            return false;
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

}
