package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * An upgrade to fix CIB-927.
 */
public class PostBuildActionSpecificationsUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    private PreparedStatement insertStatement = null;

    public String getName()
    {
        return "Post build action specifications";
    }

    public String getDescription()
    {
        return "Upgrades post build action storage of related build specifications (CIB-927)";
    }

    protected void doRefactor(Connection con, SchemaRefactor refactor) throws SQLException
    {
        // All current specifications have exactly one post build action.  Move
        // it into the new POST_BUILD_ACTION_SPECIFICATIONS table, then drop
        // the ACTION_ID column from the BUILD_SPECIFICATION table.
        try
        {
            insertStatement = con.prepareStatement("INSERT INTO POST_BUILD_ACTION_SPECIFICATIONS (SPECIFICATION_ID, ACTION_ID) VALUES (?, ?)");
            addActions(con);
            refactor.dropColumn("BUILD_SPECIFICATION", "ACTION_ID");
        }
        finally
        {
            JDBCUtils.close(insertStatement);
        }
    }

    private void addActions(Connection con) throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareStatement("SELECT ID, ACTION_ID FROM BUILD_SPECIFICATION");
            rs = stmt.executeQuery();
            while(rs.next())
            {
                Long id = JDBCUtils.getLong(rs, "ID");
                Long action = JDBCUtils.getLong(rs, "ACTION_ID");

                if (action != null)
                {
                    addAction(id, action);
                }
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    private void addAction(Long spec, Long action) throws SQLException
    {
        insertStatement.setLong(1, spec);
        insertStatement.setLong(2, action);
        insertStatement.executeUpdate();
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
