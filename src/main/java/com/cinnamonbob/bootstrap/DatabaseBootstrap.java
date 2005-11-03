package com.cinnamonbob.bootstrap;

import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.util.JDBCUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hibernate specific bootstrap support that creates the database scheme
 * if it does not already exist in the configured database.
 */
public class DatabaseBootstrap
{
    public static final String SCHEMA_TEST_TABLE = "USER";

    private static final Logger LOG = Logger.getLogger(DatabaseBootstrap.class.getName());

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
//            createQuartzSchema(factoryBean);
        }
    }

    private void createQuartzSchema(LocalSessionFactoryBean factoryBean)
    {
        SessionFactory sessionFactory = (SessionFactory) factoryBean.getObject();

        HibernateTemplate hibernateTemplate = new HibernateTemplate(sessionFactory);
        hibernateTemplate.execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                Connection con = session.connection();

                InputStream in = null;
                try
                {
                    in = getClass().getResourceAsStream("/com/cinnamonbob/quartz/schema/tables_hsqldb.sql");
                    if (in == null)
                    {
                        throw new IOException("Quartz schema creation script not found.");
                    }

                    String[] sql = JDBCUtils.loadScript(in);
                    JDBCUtils.executeSchemaScript(con, sql);
                    return null;
                }
                catch (IOException e)
                {
                    LOG.log(Level.WARNING, e.getMessage(), e);
                    return null;
                }
                finally
                {
                    IOUtils.close(in);
                }
            }
        });
    }

    private boolean schemaExists()
    {
        // does the schema exist?
        try
        {
            DataSource dataSource = (DataSource) getContext().getBean("dataSource");
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
