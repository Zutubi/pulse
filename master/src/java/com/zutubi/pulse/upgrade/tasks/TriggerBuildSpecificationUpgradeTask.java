package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * <class-comment/>
 */
public class TriggerBuildSpecificationUpgradeTask extends DatabaseUpgradeTask
{
    public static final String PARAM_SPEC = "spec";

    public String getName()
    {
        return "Trigger build specification";
    }

    public String getDescription()
    {
        return "This upgrade task changes the way triggers refer to build specifications";
    }

    public void execute(Connection con) throws SQLException, IOException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            ps = con.prepareStatement("SELECT id, project, data FROM trigger WHERE task_class = 'com.zutubi.pulse.scheduling.tasks.BuildProjectTask'");
            rs = ps.executeQuery();

            while(rs.next())
            {
                if(updateTrigger(con, JDBCUtils.getLong(rs, "id"), JDBCUtils.getLong(rs, "project"), rs.getBytes("data")))
                {
                    return;
                }
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(ps);
        }
    }

    private boolean updateTrigger(Connection con, Long id, Long projectId, byte[] data) throws IOException, SQLException
    {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;

        try
        {
            ois = new ObjectInputStream(new ByteArrayInputStream(data));
            Map<Serializable, Serializable> dataMap = (Map<Serializable, Serializable>)ois.readObject();

            Serializable serializable = dataMap.get(PARAM_SPEC);
            if(serializable instanceof Long)
            {
                // We have been run before!
                return true;
            }

            String specName = (String) serializable;
            Long specId = getSpecId(con, projectId, specName);
            dataMap.put(PARAM_SPEC, specId);

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(byteStream);
            oos.writeObject(dataMap);
            data = byteStream.toByteArray();

            updateDataMap(con, id, data);
            return false;
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

    }

    private Long getSpecId(Connection con, Long projectId, String specName) throws SQLException, IOException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            ps = con.prepareStatement("SELECT id FROM build_specification WHERE project_id = ? and name = ?");
            ps.setLong(1, projectId);
            ps.setString(2, specName);
            rs = ps.executeQuery();

            if(rs.next())
            {
                return rs.getLong("id");
            }
            else
            {
                throw new IOException("No build specification '" + specName + "' found for project " + projectId);
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(ps);
        }
    }

    private void updateDataMap(Connection con, Long id, byte[] data) throws SQLException
    {
        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("UPDATE trigger SET data = ? WHERE id = ?");
            stmt.setBytes(1, data);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    /**
     * Failure in this upgrade is non-fatal, just annoying.
     *
     * @return false
     */
    public boolean haltOnFailure()
    {
        return false;
    }
}
