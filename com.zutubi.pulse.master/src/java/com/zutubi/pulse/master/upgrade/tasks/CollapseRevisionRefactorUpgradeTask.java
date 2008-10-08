package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.hibernate.SchemaRefactor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This upgrade task handles the refactoring required by the removal of the revision as a
 * persistent entity.  This was done as part of the scm api refactoring of the author/command/date
 * details out of the revision and into the changelist.
 */
public class CollapseRevisionRefactorUpgradeTask extends AbstractSchemaRefactorUpgradeTask
{
    public String getName()
    {
        return "Scm revision refactor";
    }

    public String getDescription()
    {
        return "Remove the 'revision' from being a standalone persistent entity.";
    }

    protected void doRefactor(Connection con, SchemaRefactor refactor) throws SQLException, IOException
    {
        // build result table
        refactor.patch("com/zutubi/pulse/master/upgrade/tasks/schema/Schema-2.0.13-patch-01.hbm.xml");
        migrateRevisionStringToBuildResult(con);
        refactor.dropColumn("BUILD_RESULT", "REVISION_ID");

        try
        {
            addIndex(con, "BUILD_RESULT", "idx_build_revision_string", "REVISION_STRING");
        }
        catch (SQLException e)
        {
            // Index could already be present.
            System.out.println("Warning: Unable to add index to BUILD_RESULT.REVISION_STRING: " + e.getMessage());
        }

        // revision table.
        refactor.dropTable("REVISION");

        // changelist table
        refactor.renameColumn("BUILD_CHANGELIST", "REVISION_COMMENT", "COMMENT");
        refactor.renameColumn("BUILD_CHANGELIST", "REVISION_AUTHOR", "AUTHOR");
        refactor.renameColumn("BUILD_CHANGELIST", "REVISION_DATE", "TIME");

        refactor.dropColumn("BUILD_CHANGELIST", "REVISION_BRANCH");

        try
        {
            addIndex(con, "BUILD_CHANGELIST", "idx_changelist_revision_string", "REVISION_STRING");
        }
        catch (SQLException e)
        {
            // Index could already be present.
            System.out.println("Warning: Unable to add index to BUILD_CHANGELIST.REVISION_STRING: " + e.getMessage());
        }
    }

    private void migrateRevisionStringToBuildResult(Connection con) throws SQLException
    {
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        ResultSet rs = null;
        try
        {
            ps1 = con.prepareStatement("select BUILD_RESULT.ID as ID, REVISION.REVISIONSTRING as REV from BUILD_RESULT, REVISION where BUILD_RESULT.REVISION_ID = REVISION.ID");
            rs = ps1.executeQuery();

            ps2 = con.prepareStatement("update BUILD_RESULT set REVISION_STRING = ? where ID = ?");

            while (rs.next())
            {
                JDBCUtils.setString(ps2, 1, JDBCUtils.getString(rs, "REV"));
                JDBCUtils.setLong(ps2, 2, JDBCUtils.getLong(rs, "ID"));
                
                int rcount = ps2.executeUpdate();
                if (rcount != 1)
                {
                    // this is unexpected.
                }
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(ps1);
            JDBCUtils.close(ps2);
        }
    }

}
