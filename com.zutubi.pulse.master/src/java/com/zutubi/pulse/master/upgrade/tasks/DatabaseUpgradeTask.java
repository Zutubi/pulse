package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.upgrade.DataSourceAware;
import com.zutubi.pulse.master.upgrade.UpgradeException;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 */
public abstract class DatabaseUpgradeTask extends AbstractUpgradeTask implements DataSourceAware
{
    private static final Logger LOG = Logger.getLogger(DatabaseUpgradeTask.class);

    protected DataSource dataSource;

    public void execute() throws UpgradeException
    {
        Connection connection = null;
        try
        {
            connection = dataSource.getConnection();
            execute(connection);
        }
        catch (Exception e)
        {
            LOG.error(e);
            addError("Exception: " + e.getMessage() + ". Please see the log files for details.");
        }
        finally
        {
            JDBCUtils.close(connection);
        }
    }

    public abstract void execute(Connection con) throws Exception;

    protected byte[] upgradeBlob(ResultSet rs, String columnName, ObjectUpgrader objectUpgrader) throws SQLException, IOException
    {
        byte[] data = rs.getBytes(columnName);

        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;

        try
        {
            ois = new ObjectInputStream(new ByteArrayInputStream(data));
            Object object = ois.readObject();
            objectUpgrader.upgrade(object);

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(byteStream);
            oos.writeObject(object);
            data = byteStream.toByteArray();
        }
        catch (ClassNotFoundException e)
        {
            throw new IOException(e.getMessage());
        }
        finally
        {
            IOUtils.close(ois);
            IOUtils.close(oos);
        }
        return data;
    }

    protected void addIndex(Connection con, String table, String indexName, String... columns) throws SQLException
    {
        JDBCUtils.executeUpdate(con, JDBCUtils.sqlAddIndex(con, table, indexName, columns));
    }

    protected void addIndex(Connection con, String table, String indexName, String column, int prefixLength) throws SQLException
    {
        JDBCUtils.executeUpdate(con, JDBCUtils.sqlAddIndex(con, table, indexName, column, prefixLength));
    }
    
    protected void dropIndex(Connection con, String table, String indexName) throws SQLException
    {
        JDBCUtils.executeUpdate(con, JDBCUtils.sqlDropIndex(con, table, indexName));
    }

    protected void runUpdate(Connection con, String sql) throws SQLException
    {
        JDBCUtils.executeUpdate(con, sql);
    }

    public void setDataSource(DataSource source)
    {
        this.dataSource = source;
    }
}
