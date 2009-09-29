package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Removes project triggers that refer to a non-existant project.
 */
public class DeleteOrphanedTriggersUpgradeTask extends DatabaseUpgradeTask
{
    public boolean haltOnFailure()
    {
        return false;
    }

    public void execute(Connection con) throws IOException, SQLException
    {
        PreparedStatement query = null;
        PreparedStatement delete = null;
        ResultSet rs = null;
        try
        {
            query = con.prepareStatement("select LOCAL_TRIGGER.ID from LOCAL_TRIGGER left outer join PROJECT on LOCAL_TRIGGER.PROJECT = PROJECT.ID where LOCAL_TRIGGER.PROJECT != 0 and PROJECT.ID is null");
            delete = con.prepareStatement("delete from LOCAL_TRIGGER where ID = ?");
            rs = query.executeQuery();
            while (rs.next())
            {
                delete.setLong(1, rs.getLong(1));
                delete.executeUpdate();
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(delete);
            JDBCUtils.close(query);
        }
    }
}