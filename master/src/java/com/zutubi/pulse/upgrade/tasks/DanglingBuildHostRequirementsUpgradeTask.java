package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 */
public class DanglingBuildHostRequirementsUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Dangling build host requirements";
    }

    public String getDescription()
    {
        return "Removes build host requirements entities that are no longer referenced.";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(Connection con) throws SQLException
    {
        Set<Long> referenced = getAllReferenced(con);
        List<Long> unreferenced = getUnreferenced(con, referenced);
        removeUnreferenced(con, unreferenced);
    }

    private Set<Long> getAllReferenced(Connection con) throws SQLException
    {
        Set<Long> result = new HashSet<Long>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            ps = con.prepareStatement("select BUILD_HOST_REQUIREMENTS_ID from BUILD_SPECIFICATION_NODE");

            rs = ps.executeQuery();
            while(rs.next())
            {
                result.add(rs.getLong("BUILD_HOST_REQUIREMENTS_ID"));
            }
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(rs);
        }

        return result;
    }

    private List<Long> getUnreferenced(Connection con, Set<Long> referenced) throws SQLException
    {
        List<Long> result = new LinkedList<Long>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            ps = con.prepareStatement("select ID from BUILD_HOST_REQUIREMENTS");

            rs = ps.executeQuery();
            while(rs.next())
            {
                long id = rs.getLong("ID");
                if(!referenced.contains(id))
                {
                    result.add(id);
                }
            }
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(rs);
        }

        return result;
    }

    private void removeUnreferenced(Connection con, List<Long> unreferenced) throws SQLException
    {
        PreparedStatement ps = null;

        try
        {
            ps = con.prepareStatement("delete from BUILD_HOST_REQUIREMENTS where ID = ?");
            for(Long id: unreferenced)
            {
                ps.setLong(1, id);
                ps.executeUpdate();
            }
        }
        finally
        {
            JDBCUtils.close(ps);
        }
    }

}
