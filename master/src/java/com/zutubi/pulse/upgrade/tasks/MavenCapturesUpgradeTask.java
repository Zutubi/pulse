/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeTask;
import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.upgrade.UpgradeException;
import com.zutubi.pulse.upgrade.DataSourceAware;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.pulse.util.logging.Logger;

import javax.sql.DataSource;
import java.util.List;
import java.util.LinkedList;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * <class-comment/>
 */
public class MavenCapturesUpgradeTask implements UpgradeTask, DataSourceAware
{
    private static final Logger LOG = Logger.getLogger(MavenCapturesUpgradeTask.class);

    private DataSource dataSource;

    private int buildNumber;

    private List<String> errors = new LinkedList<String>();

    public String getName()
    {
        return "Maven test report capturing";
    }

    public String getDescription()
    {
        return "This upgrade task adds artifacts to capture test reports for existing maven (and maven 2) projects";
    }

    public int getBuildNumber()
    {
        return buildNumber;
    }

    public void setBuildNumber(int no)
    {
        this.buildNumber = no;
    }

    public void execute(UpgradeContext context) throws UpgradeException
    {
        Connection con = null;
        try
        {
            con = dataSource.getConnection();

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
            long nextId = getNextId(con, rs);
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
        catch (SQLException e)
        {
            LOG.severe(e);
            errors.add("SQLException: " + e.getMessage() + ". Please see the log files for details.");
        }
        finally
        {
            JDBCUtils.close(con);
        }
    }

    private long getNextId(Connection con, ResultSet rs) throws SQLException
    {
        PreparedStatement ps = null;
        long nextId;

        try
        {
            ps = con.prepareStatement("select NEXT_HI from HIBERNATE_UNIQUE_KEY");
            rs = ps.executeQuery();
            rs.next();
            nextId = rs.getLong(1);
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(rs);
        }

        try
        {
            nextId++;
            ps = con.prepareStatement("insert into HIBERNATE_UNIQUE_KEY (next_hi) values (?)");
            JDBCUtils.setLong(ps, 1, nextId);
            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }

        // Emulate Hibernate's hilo algorithm
        return nextId * (Short.MAX_VALUE + 1) + 1;
    }

    public List<String> getErrors()
    {
        return errors;
    }

    /**
     * Required resource.
     *
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
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
