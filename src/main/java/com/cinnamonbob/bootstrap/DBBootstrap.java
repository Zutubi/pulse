package com.cinnamonbob.bootstrap;

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
public class DBBootstrap
{
    public static final String SCHEMA_TEST_TABLE = "User";
    
    private ApplicationContext context;

    public DBBootstrap(ApplicationContext context)
    {
        this.context = context;
    }

    public void initialiseDatabase()
    {
        if (!schemaExists())
        {
            System.out.println("creating database.");
            LocalSessionFactoryBean factoryBean = (LocalSessionFactoryBean) context.getBean("&sessionFactory");
            factoryBean.createDatabaseSchema();
        }
    }

    
    private boolean schemaExists()
    {
        // does the schema exist?
        try 
        {
            DataSource dataSource = (DataSource) context.getBean("dataSource");
            Connection con = dataSource.getConnection();
            CallableStatement stmt = con.prepareCall("SELECT COUNT(*) FROM " + SCHEMA_TEST_TABLE);
            stmt.execute();
            return true;
        } 
        catch (SQLException e)
        {
            return false;
        }        
    }
    
/*
    private boolean schemaExists() throws SQLException
    {
        // does the schema exist?
        DataSource dataSource = (DataSource) context.getBean("dataSource");
        Connection con = dataSource.getConnection();
        DatabaseMetaData metaData = con.getMetaData();
        ResultSet tableNames = metaData.getTables(null, "%", "%", new String[]{"TABLE"});

        String tableName = null;
        if (tableNames.next())
        {
            tableName = tableNames.getString("TABLE_NAME");
        }

        return (tableName != null);
    }
*/
}
