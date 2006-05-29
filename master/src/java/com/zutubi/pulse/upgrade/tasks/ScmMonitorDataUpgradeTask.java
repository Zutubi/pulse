/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeTask;
import com.zutubi.pulse.upgrade.DataSourceAware;
import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.upgrade.UpgradeException;
import com.zutubi.pulse.util.JDBCUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.LinkedList;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 * <class-comment/>
 */
public class ScmMonitorDataUpgradeTask implements UpgradeTask, DataSourceAware
{
    private int buildNumber;

    private DataSource dataSource;

    private List<String> errors = new LinkedList<String>();

    /**
     * @see com.zutubi.pulse.upgrade.UpgradeTask#getName()
     */
    public String getName()
    {
        return "Scm monitor field initialisation.";
    }

    /**
     * @see com.zutubi.pulse.upgrade.UpgradeTask#getDescription()
     */
    public String getDescription()
    {
        return "This upgrade task initialises the new scm monitor field, setting its value to true.";
    }

    /**
     * @see com.zutubi.pulse.upgrade.UpgradeTask#getBuildNumber()
     */
    public int getBuildNumber()
    {
        return buildNumber;
    }

    public void setBuildNumber(int buildNumber)
    {
        this.buildNumber = buildNumber;
    }

    /**
     * @see com.zutubi.pulse.upgrade.UpgradeTask#execute(com.zutubi.pulse.upgrade.UpgradeContext)
     */
    public void execute(UpgradeContext context) throws UpgradeException
    {
        Connection con = null;
        try
        {
            con = dataSource.getConnection();
            PreparedStatement ps = null;
            try
            {
                ps = con.prepareStatement("UPDATE scm SET monitor = ?");
                JDBCUtils.setBool(ps, 1, true);
                ps.executeUpdate();
            }
            finally
            {
                JDBCUtils.close(ps);
            }
        }
        catch (SQLException e)
        {
            errors.add("SQLException: " + e.getMessage());
        }
        finally
        {
            JDBCUtils.close(con);
        }
    }

    /**
     * @see com.zutubi.pulse.upgrade.UpgradeTask#getErrors()
     */
    public List<String> getErrors()
    {
        return errors;
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

    /**
     * Required resource.
     *
     * @param source
     */
    public void setDataSource(DataSource source)
    {
        this.dataSource = source;
    }
}
