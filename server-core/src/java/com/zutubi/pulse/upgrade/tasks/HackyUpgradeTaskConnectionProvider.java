package com.zutubi.pulse.upgrade.tasks;

import org.hibernate.connection.ConnectionProvider;
import org.hibernate.HibernateException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * The purpose of this ConnectionProvider is to provide a programmatic specify
 * the database connection to be used by hibernate.
 *
 * Hibernate uses two methods to lookup its datasource / jdbc connection.
 * <ul>
 * <li>a)</li> via a datasource bound to JNDI
 * <li>b)</li> via a connection provider.
 * <ul>
 * Since we do not have a jndi bound datasource handy all of the time, option
 * 'b' is what is used. The only downside with option 'b' is that hibernate does
 * not allow applications any control over the creation of the ConnectionProvider
 * class.
 * The result is this hacky connection provider that contains a public static
 * DataSource field that allows us to programmatically specify the dataSource that
 * will be used.
 * 
 */
public class HackyUpgradeTaskConnectionProvider implements ConnectionProvider
{
    public static DataSource dataSource;

    public void configure(Properties props) throws HibernateException
    {
        if (dataSource == null)
        {
            throw new HibernateException("DataSource must be set.");
        }
    }

    public Connection getConnection() throws SQLException
    {
        return dataSource.getConnection();
    }

    public void closeConnection(Connection conn) throws SQLException
    {
        conn.close();
    }

    public void close()
    {
    }

    public boolean supportsAggressiveRelease()
    {
        return true;
    }
}
