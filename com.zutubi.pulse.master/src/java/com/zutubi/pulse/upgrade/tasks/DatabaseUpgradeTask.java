package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.DataSourceAware;
import com.zutubi.pulse.upgrade.UpgradeException;
import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import javax.sql.DataSource;
import java.io.*;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

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
        catch (IOException e)
        {
            LOG.error(e);
            addError("IOException: " + e.getMessage() + ". Please see the log files for details.");
        }
        catch (SQLException e)
        {
            LOG.error(e);
            addError("SQLException: " + e.getMessage() + ". Please see the log files for details.");
        }
        finally
        {
            JDBCUtils.close(connection);
        }
    }

    public abstract void execute(Connection con) throws IOException, SQLException;

    protected List<Long> getAllProjects(Connection con) throws SQLException
    {
        return getAllIds(con, "project");
    }

    protected List<Long> getAllUsers(Connection con) throws SQLException
    {
        return getAllIds(con, "user");
    }

    private List<Long> getAllIds(Connection con, String table) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        List<Long> all = new LinkedList<Long>();

        try
        {
            stmt = con.prepareCall("SELECT id FROM " + table);
            rs = stmt.executeQuery();
            while (rs.next())
            {
                all.add(rs.getLong("id"));
            }

            return all;
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

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
        String sql = "CREATE INDEX " + indexName + " ON " + table + " (";
        boolean first = true;
        for (String column : columns)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                sql += ",";
            }

            sql += column;
        }

        sql += ")";

        runUpdate(con, sql);
    }

    protected void dropIndex(Connection con, String table, String indexName) throws SQLException
    {
        String sql;
        String databaseProductName = con.getMetaData().getDatabaseProductName().toLowerCase();
        if (databaseProductName.contains("postgres"))
        {
            sql = "DROP INDEX IF EXISTS " + indexName;
        }
        else if (databaseProductName.contains("mysql"))
        {
            sql = "DROP INDEX " + indexName + " ON " + table;
        }
        else
        {
            sql = "DROP INDEX " + indexName + " IF EXISTS";
        }

        runUpdate(con, sql);
    }

    protected void runUpdate(Connection con, String sql) throws SQLException
    {
        PreparedStatement stmt = null;
        try
        {
            stmt = con.prepareStatement(sql);
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    protected Long runQueryForLong(Connection con, String sql) throws SQLException
    {
        PreparedStatement query = null;
        ResultSet rs = null;
        try
        {
            query = con.prepareStatement(sql);
            rs = query.executeQuery();
            if (rs.next())
            {
                return rs.getLong(1);
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(query);
        }

        return null;
    }

    public void setDataSource(DataSource source)
    {
        this.dataSource = source;
    }
}
