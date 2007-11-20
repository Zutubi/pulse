package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.util.TextUtils;

import java.sql.*;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

/**
 */
public class ChangeViewerUpgradeTask extends DatabaseUpgradeTask
{
    private static final String CHANGE_VIEWER_URL = "change.viewer.url";

    private long nextId;

    public String getName()
    {
        return "Change viewer data";
    }

    public String getDescription()
    {
        return "Data upgrades required for improved change viewer support.";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(Connection con) throws IOException, SQLException
    {
        // Two tasks:
        //   - detect any SCMs with a change.viewer.url property and convert
        //     it to a new custom change viewer.
        //   - convert the existing REVISION string column in the CHANGE
        //     table to a FILE_REVISION object with either a CVS revision
        //     or a numerical revision based on whether the current value is
        //     in CVS dotted-decimal format.  The revision column should then
        //     be dropped.
        nextId = HibernateUtils.getNextId(con);

        updateScms(con);
        updateFileRevisions(con);
        dropColumn(con);
        
        HibernateUtils.ensureNextId(con, nextId);
    }

    private void updateScms(Connection con) throws SQLException, IOException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            ps = con.prepareStatement("SELECT id, properties FROM scm");
            rs = ps.executeQuery();

            while(rs.next())
            {
                String propString = rs.getString(2);
                Properties p = new Properties();
                p.load(new ByteArrayInputStream(propString.getBytes()));
                updateScm(con, rs.getLong(1), p);
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(ps);
        }
    }

    private void updateScm(Connection con, long id, Properties properties) throws SQLException, IOException
    {
        String changeViewer = properties.getProperty(CHANGE_VIEWER_URL);
        if(TextUtils.stringSet(changeViewer))
        {
            // Create the new change viewer
            long viewerId = createChangeViewer(con, changeViewer);

            // Hook it into the project
            setProjectViewer(con, id, viewerId);

            // Remove it from the properties
            properties.remove(CHANGE_VIEWER_URL);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            properties.store(bos, null);
            
            PreparedStatement ps = null;

            try
            {
                ps = con.prepareStatement("UPDATE scm SET properties = ? WHERE id = ?");
                ps.setString(1, bos.toString());
                ps.setLong(2, id);
                ps.executeUpdate();
            }
            finally
            {
                JDBCUtils.close(ps);
            }
        }
    }

    private long createChangeViewer(Connection con, String changesetURL) throws SQLException
    {
        long id = nextId++;
        PreparedStatement ps = null;

        try
        {
            ps = con.prepareStatement("INSERT INTO change_viewer VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setLong(1, id);
            ps.setString(2, "CUSTOM");
            ps.setString(3, changesetURL);
            ps.setNull(4, Types.VARCHAR);
            ps.setNull(5, Types.VARCHAR);
            ps.setNull(6, Types.VARCHAR);
            ps.setNull(7, Types.VARCHAR);
            ps.setNull(8, Types.VARCHAR);
            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }

        return id;
    }

    private void setProjectViewer(Connection con, long scmId, long viewerId) throws SQLException
    {
        PreparedStatement ps = null;

        try
        {
            ps = con.prepareStatement("UPDATE project SET change_viewer = ? WHERE scm = ?");
            ps.setLong(1, viewerId);
            ps.setLong(2, scmId);
            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }
    }

    private void updateFileRevisions(Connection con) throws SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            ps = con.prepareStatement("SELECT id, revision FROM change");
            rs = ps.executeQuery();

            while(rs.next())
            {
                updateChange(con, rs.getLong(1), rs.getString(2));
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(ps);
        }
    }

    private void updateChange(Connection con, long id, String revision) throws SQLException
    {
        long revisionId = nextId++;

        if(revision.contains("."))
        {
            insertCVSRevision(con, revisionId, revision);
        }
        else
        {
            insertNumericalRevision(con, revisionId, Long.parseLong(revision));
        }

        PreparedStatement ps = null;

        try
        {
            ps = con.prepareStatement("UPDATE change SET revision_id = ? WHERE id = ?");
            ps.setLong(1, revisionId);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }

    }

    private void insertCVSRevision(Connection con, long revisionId, String revision) throws SQLException
    {
        PreparedStatement ps = null;

        try
        {
            ps = con.prepareStatement("INSERT INTO file_revision VALUES (?, ?, ?, ?)");
            ps.setLong(1, revisionId);
            ps.setString(2, "CVS");
            ps.setString(3, revision);
            ps.setNull(4, Types.BIGINT);
            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }
    }

    private void insertNumericalRevision(Connection con, long revisionId, long revision) throws SQLException
    {
        PreparedStatement ps = null;

        try
        {
            ps = con.prepareStatement("INSERT INTO file_revision VALUES (?, ?, ?, ?)");
            ps.setLong(1, revisionId);
            ps.setString(2, "NUMERICAL");
            ps.setNull(3, Types.VARCHAR);
            ps.setLong(4, revision);
            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }
    }

    private void dropColumn(Connection con) throws SQLException
    {
        PreparedStatement ps = null;

        try
        {
            ps = con.prepareStatement("ALTER TABLE change DROP COLUMN revision");
            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }
    }
}
