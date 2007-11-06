package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.util.logging.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class MavenCapturesUpgradeTask extends DatabaseUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(MavenCapturesUpgradeTask.class);

    public String getName()
    {
        return "Maven test report capturing";
    }

    public String getDescription()
    {
        return "This upgrade task adds artifacts to capture test reports for existing maven (and maven 2) projects";
    }

    public void execute(Connection con) throws SQLException
    {
        // start migrating the data.
        // a) up all maven projects
        List<PulseFileDetailsData> data = new LinkedList<PulseFileDetailsData>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ps = con.prepareCall("select * from BOB_FILE_DETAILS where SOURCE_TYPE like 'MAVEN%'");
            rs = ps.executeQuery();
            while (rs.next())
            {
                data.add(new PulseFileDetailsData(rs));
            }
        }
        catch(Exception e)
        {
            LOG.warning(e);
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(rs);
        }

        // b) add captures
        long nextId = HibernateUtils.getNextId(con);
        PreparedStatement processorsPs = null;

        try
        {
            ps = con.prepareStatement("insert into CAPTURE (id, name, includes, details_id, type) values (?, ?, ?, ?, 'DIRECTORY')");
            processorsPs = con.prepareStatement("insert into CAPTURE_PROCESSORS (capture_id, processor) values (?, ?)");

            for (PulseFileDetailsData upd : data)
            {
                nextId++;
                JDBCUtils.setLong(ps, 1, nextId);
                JDBCUtils.setString(ps, 2, "test reports");

                if(upd.isMaven())
                {
                    JDBCUtils.setString(ps, 3, "**/target/test-reports/TEST-*.xml");
                }
                else
                {
                    JDBCUtils.setString(ps, 3, "**/target/surefire-reports/TEST-*.xml");
                }

                JDBCUtils.setLong(ps, 4, upd.getId());

                ps.executeUpdate();

                JDBCUtils.setLong(processorsPs, 1, nextId);
                JDBCUtils.setString(processorsPs, 2, "junit");
                processorsPs.executeUpdate();
            }
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(processorsPs);
        }
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    private class PulseFileDetailsData
    {
        private long projectId;
        private boolean isMaven;

        public PulseFileDetailsData(ResultSet rs) throws SQLException
        {
            projectId = rs.getLong("id");
            isMaven = rs.getString("source_type").equalsIgnoreCase("maven");
        }

        public long getId()
        {
            return projectId;
        }

        public boolean isMaven()
        {
            return isMaven;
        }
    }
}
