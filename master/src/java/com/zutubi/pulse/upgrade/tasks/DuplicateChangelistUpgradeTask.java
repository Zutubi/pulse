package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Upgrade task to detect and combine duplicate changelists.
 */
public class DuplicateChangelistUpgradeTask extends DatabaseUpgradeTask
{
    // Maps <uid>:<revision> to chnagelist id
    private Map<String, Long> foundChangelists = new HashMap<String, Long>();

    private PreparedStatement selectChangelists;
    private PreparedStatement updateChangelistProjects;
    private PreparedStatement updateChangelistResults;
    private PreparedStatement selectFileChangeByChangelistId;
    private PreparedStatement deleteFileChangeByChangelistId;
    private PreparedStatement deleteFileRevisionById;
    private PreparedStatement selectRevisionByChangelistId;
    private PreparedStatement deleteChangelistById;
    private PreparedStatement deleteRevisionById;

    public String getName()
    {
        return "Duplicate changelists";
    }

    public String getDescription()
    {
        return "Detects and combines duplicate changelists";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(UpgradeContext context, Connection con) throws IOException, SQLException
    {
        try
        {
            prepareStatements(con);
            processChangelists(con);
        }
        finally
        {
            closePreparedStatements();
        }
    }

    private void prepareStatements(Connection con) throws SQLException
    {
        selectChangelists = con.prepareStatement("SELECT CHANGELIST.ID AS ID, CHANGELIST.SERVER_UID AS SERVER_UID, REVISION.REVISIONSTRING AS REVISIONSTRING FROM CHANGELIST, REVISION WHERE CHANGELIST.REVISION_ID = REVISION.ID");
        updateChangelistProjects = con.prepareStatement("UPDATE CHANGELIST_PROJECTS SET CHANGELIST_ID = ? WHERE CHANGELIST_ID = ?");
        updateChangelistResults = con.prepareStatement("UPDATE CHANGELIST_RESULTS SET CHANGELIST_ID = ? WHERE CHANGELIST_ID = ?");
        selectFileChangeByChangelistId = con.prepareStatement("SELECT REVISION_ID FROM FILE_CHANGE WHERE CHANGELIST_ID = ?");
        deleteFileRevisionById = con.prepareStatement("DELETE FROM FILE_REVISION WHERE ID = ?");
        deleteFileChangeByChangelistId = con.prepareStatement("DELETE FROM FILE_CHANGE WHERE CHANGELIST_ID = ?");
        deleteChangelistById = con.prepareStatement("DELETE FROM CHANGELIST WHERE ID = ?");
        selectRevisionByChangelistId = con.prepareStatement("SELECT REVISION_ID FROM CHANGELIST WHERE ID = ?");
        deleteRevisionById = con.prepareStatement("DELETE FROM REVISION where ID = ?");
    }

    private void closePreparedStatements()
    {
        JDBCUtils.close(selectChangelists);
        JDBCUtils.close(updateChangelistProjects);
        JDBCUtils.close(updateChangelistResults);
        JDBCUtils.close(selectFileChangeByChangelistId);
        JDBCUtils.close(deleteFileChangeByChangelistId);
        JDBCUtils.close(deleteFileRevisionById);
        JDBCUtils.close(deleteChangelistById);
        JDBCUtils.close(selectRevisionByChangelistId);
        JDBCUtils.close(deleteRevisionById);
    }

    private void processChangelists(Connection con) throws SQLException, IOException
    {
        ResultSet rs = null;
        try
        {
            rs = selectChangelists.executeQuery();
            while (rs.next())
            {
                processChangelist(con, JDBCUtils.getLong(rs, "ID"), JDBCUtils.getString(rs, "SERVER_UID"), JDBCUtils.getString(rs, "REVISIONSTRING"));
            }
        }
        finally
        {
            JDBCUtils.close(rs);
        }
    }

    private void processChangelist(Connection con, Long id, String uid, String rev) throws SQLException
    {
        String key = uid + ":" + rev;
        Long existing = foundChangelists.get(key);
        if(existing == null)
        {
            foundChangelists.put(key, id);
        }
        else
        {
            combineChangelists(id, existing);
        }
    }

    private void combineChangelists(Long id, Long existing) throws SQLException
    {
        updateChangelistProjects.setLong(1, existing);
        updateChangelistProjects.setLong(2, id);
        updateChangelistProjects.executeUpdate();

        updateChangelistResults.setLong(1, existing);
        updateChangelistResults.setLong(2, id);
        updateChangelistResults.executeUpdate();

        removeFiles(id);
        removeChangelist(id);
    }

    private void removeFiles(Long id) throws SQLException
    {
        selectFileChangeByChangelistId.setLong(1, id);
        ResultSet rs = null;

        try
        {
            rs = selectFileChangeByChangelistId.executeQuery();
            deleteFileChangeByChangelistId.setLong(1, id);
            deleteFileChangeByChangelistId.executeUpdate();

            while(rs.next())
            {
                Long revisionId = rs.getLong("REVISION_ID");
                deleteFileRevisionById.setLong(1, revisionId);
                deleteFileRevisionById.executeUpdate();
            }
        }
        finally
        {
            JDBCUtils.close(rs);
        }
    }

    private void removeChangelist(Long id) throws SQLException
    {
        selectRevisionByChangelistId.setLong(1, id);
        ResultSet rs = null;

        try
        {
            rs = selectRevisionByChangelistId.executeQuery();
            deleteChangelistById.setLong(1, id);
            deleteChangelistById.executeUpdate();

            while(rs.next())
            {
                Long revisionId = rs.getLong("REVISION_ID");
                deleteRevisionById.setLong(1, revisionId);
                deleteRevisionById.executeUpdate();
            }
        }
        finally
        {
            JDBCUtils.close(rs);
        }
    }
}