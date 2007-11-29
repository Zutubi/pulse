package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Adds an index to the end time (finish) column for build results for faster
 * RSS queries.
 */
public class IndexBuildResultEndTimeUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Index build_result table.";
    }

    public String getDescription()
    {
        return "This upgrade tasks adds an index to the build_result table to improve RSS feed performance.";
    }

    public void execute(UpgradeContext context, Connection con) throws SQLException
    {
        PreparedStatement stmt = null;
        try
        {
            stmt = con.prepareStatement("create index idx_buildresult_finish on BUILD_RESULT (FINISH)");
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

