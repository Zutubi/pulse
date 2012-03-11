package com.zutubi.pulse.master.util.jdbc;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Wraps a JDBC driver class.  We do this so that we can load the delegate
 * classes from a custom classloader.  Registering and deregistering those
 * drivers directly with the DriverManager fails as they are from a different
 * classloader, so we wrap them to 'trick' the manager.
 */
public class DriverWrapper implements Driver
{
    private Driver delegate;

    public DriverWrapper(Driver d)
    {
        this.delegate = d;
    }

    public boolean acceptsURL(String u) throws SQLException
    {
        return this.delegate.acceptsURL(u);
    }

    public Connection connect(String u, Properties p) throws SQLException
    {
        return this.delegate.connect(u, p);
    }

    public int getMajorVersion()
    {
        return this.delegate.getMajorVersion();
    }

    public int getMinorVersion()
    {
        return this.delegate.getMinorVersion();
    }

    public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException
    {
        return this.delegate.getPropertyInfo(u, p);
    }

    public boolean jdbcCompliant()
    {
        return this.delegate.jdbcCompliant();
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        throw new SQLFeatureNotSupportedException();
    }

    public Driver getDelegate()
    {
        return delegate;
    }
}
