package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class IncrementalBuildUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Incremental builds";
    }

    public String getDescription()
    {
        return "Upgrade to support new checkout schemes for incremental building";
    }

    public void execute(UpgradeContext context, Connection con) throws SQLException
    {
        // First, copy checkout schemes from the project to all specs, then
        // delete the column from the project table.  Care must be taken as
        // the scheme names have changed and existing projects may have null
        // checkout schemes.
        updateSpecs(con);
        dropColumn(con);
    }

    private void updateSpecs(Connection con) throws SQLException
    {
        Map<Long, String> schemes = lookupSchemes(con);

        CallableStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT id, project_id FROM build_specification");
            rs = stmt.executeQuery();
            while(rs.next())
            {
                setCheckoutScheme(con, JDBCUtils.getLong(rs, "id"), schemes.get(JDBCUtils.getLong(rs, "project_id")));
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    private Map<Long,String> lookupSchemes(Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        Map<Long, String> schemes = new TreeMap<Long, String>();
        try
        {
            stmt = con.prepareCall("SELECT id, checkout_scheme FROM project");
            rs = stmt.executeQuery();

            while (rs.next())
            {
                schemes.put(JDBCUtils.getLong(rs, "id"), JDBCUtils.getString(rs, "checkout_scheme"));
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }

        return schemes;
    }

    private void setCheckoutScheme(Connection con, Long id, String scheme) throws SQLException
    {
        BuildSpecification.CheckoutScheme newScheme = convertScheme(scheme);

        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("UPDATE build_specification SET checkout_scheme = ?, force_clean = false WHERE id = ?");
            stmt.setString(1, newScheme.toString());
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private BuildSpecification.CheckoutScheme convertScheme(String scheme)
    {
        if(scheme == null)
        {
            return BuildSpecification.CheckoutScheme.CLEAN_CHECKOUT;
        }
        else if(scheme.equals("CHECKOUT_AND_UPDATE"))
        {
            return BuildSpecification.CheckoutScheme.CLEAN_UPDATE;
        }
        else
        {
            return BuildSpecification.CheckoutScheme.CLEAN_CHECKOUT;
        }
    }

    private void dropColumn(Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("ALTER TABLE project DROP COLUMN checkout_scheme");
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
