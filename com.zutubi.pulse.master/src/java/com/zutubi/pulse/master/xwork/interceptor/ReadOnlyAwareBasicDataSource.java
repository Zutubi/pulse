package com.zutubi.pulse.master.xwork.interceptor;

import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * An extension of the basic data source that works with the {@link ReadOnlyInterceptor}
 * to mark web requests as read only.
 *
 * If the read only interceptor is present in the requests interceptor stack, it will add a
 * boolean flag to the a thread local.  The presence of this boolean will indicate whether
 * or not any database connections used during the transaction should be marked as
 * read only, allowing the underlying drivers to apply appropriate optimisations,
 * improving the performance of the request handling.
 */
public class ReadOnlyAwareBasicDataSource extends BasicDataSource
{
    @Override
    public Connection getConnection() throws SQLException
    {
        return markReadOnlyIfNecessary(super.getConnection());
    }

    @Override
    public Connection getConnection(String user, String pass) throws SQLException
    {
        return markReadOnlyIfNecessary(super.getConnection(user, pass));
    }

    private Connection markReadOnlyIfNecessary(Connection con) throws SQLException
    {
        Boolean readOnly = ReadOnlyInterceptor.READONLY.get();
        if (readOnly != null && readOnly)
        {
            con.setReadOnly(readOnly);
        }
        return con;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        return null;
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return false;
    }

}

