package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 */
public class DefaultSpecificationUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Default specification";
    }

    public String getDescription()
    {
        return "Marks one build specification as the default for each project";
    }

    public void execute(Connection con) throws SQLException
    {
        List<Long> projects = getAllProjects(con);
        for(Long id: projects)
        {
            updateProject(con, id);
        }
    }

    private void updateProject(Connection con, Long id) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;
        Long defaultId = null;

        try
        {
            stmt = con.prepareCall("SELECT id, name FROM build_specification WHERE project_id = ?");
            stmt.setLong(1, id);

            rs = stmt.executeQuery();
            while(rs.next())
            {
                if(defaultId == null)
                {
                    defaultId = rs.getLong("id");
                }
                else if(rs.getString("name").equals("default"))
                {
                    defaultId = rs.getLong("id");
                }
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }

        if(defaultId != null)
        {
            setDefault(con, id, defaultId);
        }
    }

    private void setDefault(Connection con, Long id, Long defaultId) throws SQLException
    {
        CallableStatement stmt = null;

        try
        {
            stmt = con.prepareCall("UPDATE project SET default_specification = ? WHERE id = ?");
            stmt.setLong(1, defaultId);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
