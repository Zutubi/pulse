/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.hibernate.SchemaRefactor;
import com.zutubi.util.logging.Logger;

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
    private static final Logger LOG = Logger.getLogger(CollapseRevisionRefactorUpgradeTask.class);

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
            LOG.warning("Unable to add index to BUILD_RESULT.REVISION_STRING: " + e.getMessage());
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
            LOG.warning("Unable to add index to BUILD_CHANGELIST.REVISION_STRING: " + e.getMessage());
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
                String revision = JDBCUtils.getString(rs, "REV");
                JDBCUtils.setString(ps2, 1, revision);
                
                Long resultId = JDBCUtils.getLong(rs, "ID");
                JDBCUtils.setLong(ps2, 2, resultId);
                
                int rcount = ps2.executeUpdate();
                if (rcount != 1)
                {
                    LOG.warning("Failed to execute: update BUILD_RESULT set REVISION_STRING = " +
                            revision + " where ID = " + resultId);
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
