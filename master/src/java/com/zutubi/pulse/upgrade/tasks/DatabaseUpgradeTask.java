package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.DataSourceAware;
import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.upgrade.UpgradeException;
import com.zutubi.pulse.upgrade.UpgradeTask;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.pulse.util.logging.Logger;

import javax.sql.DataSource;
import java.io.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public abstract class DatabaseUpgradeTask implements UpgradeTask, DataSourceAware
{
    private static final Logger LOG = Logger.getLogger(DatabaseUpgradeTask.class);

    protected DataSource dataSource;

    protected List<String> errors = new LinkedList<String>();

    protected int buildNumber;

    /**
     * Required resource.
     *
     * @param source
     */
    public void setDataSource(DataSource source)
    {
        this.dataSource = source;
    }

    public List<String> getErrors()
    {
        return errors;
    }

    public void setBuildNumber(int buildNumber)
    {
        this.buildNumber = buildNumber;
    }

    public int getBuildNumber()
    {
        return buildNumber;
    }

    public void execute(UpgradeContext context) throws UpgradeException
    {
        Connection connection = null;
        try
        {
            connection = dataSource.getConnection();
            execute(context, connection);
        }
        catch (IOException e)
        {
            LOG.error(e);
            errors.add("IOException: " + e.getMessage() + ". Please see the log files for details.");
        }
        catch (SQLException e)
        {
            LOG.error(e);
            errors.add("SQLException: " + e.getMessage() + ". Please see the log files for details.");
        }
        finally
        {
            JDBCUtils.close(connection);
        }
    }

    public boolean hasFailed()
    {
        return getErrors().size() > 0;
    }

    public abstract void execute(UpgradeContext context, Connection con) throws IOException, SQLException;

    protected List<Long> getAllProjects(Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        List<Long> projects = new LinkedList<Long>();

        try
        {
            stmt = con.prepareCall("SELECT id FROM project");
            rs = stmt.executeQuery();
            while(rs.next())
            {
                projects.add(rs.getLong("id"));
            }

            return projects;
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    protected byte[] upgradeBlob(ResultSet rs, String columnName, ObjectUpgrader objectUpgrader) throws SQLException, IOException, ClassNotFoundException
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
        finally
        {
            IOUtils.close(ois);
            IOUtils.close(oos);
        }
        return data;
    }
}
