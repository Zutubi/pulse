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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class CollapseRevisionRefactorUpgradeTaskTest extends BaseUpgradeTaskTestCase
{
    private CollapseRevisionRefactorUpgradeTask task;

    protected void setUp() throws Exception
    {
        super.setUp();

        task = new CollapseRevisionRefactorUpgradeTask();
        task.setDataSource(dataSource);
        task.setHibernateProperties(databaseConfig.getHibernateProperties());
        task.setMappings(getMappings());
    }

    public void testEmptyDatabaseUpgrade() throws Exception
    {
        assertTableExists("REVISION");
        assertColumnExists("BUILD_CHANGELIST", "REVISION_AUTHOR");
        assertColumnExists("BUILD_CHANGELIST", "REVISION_COMMENT");
        assertColumnExists("BUILD_CHANGELIST", "REVISION_DATE");
        assertColumnExists("BUILD_CHANGELIST", "REVISION_BRANCH");
        assertColumnExists("BUILD_CHANGELIST", "HASH");
        assertColumnExists("BUILD_RESULT", "REVISION_ID");
        assertColumnNotExists("BUILD_RESULT", "REVISION_STRING");

        runUpgrade();

        assertTableNotExists("REVISION");
        assertColumnExists("BUILD_CHANGELIST", "COMMENT");
        assertColumnExists("BUILD_CHANGELIST", "AUTHOR");
        assertColumnExists("BUILD_CHANGELIST", "TIME");
        assertColumnNotExists("BUILD_CHANGELIST", "REVISION_AUTHOR");
        assertColumnNotExists("BUILD_CHANGELIST", "REVISION_COMMENT");
        assertColumnNotExists("BUILD_CHANGELIST", "REVISION_DATE");
        assertColumnNotExists("BUILD_CHANGELIST", "REVISION_BRANCH");
        assertColumnExists("BUILD_RESULT", "REVISION_STRING");
        assertColumnNotExists("BUILD_RESULT", "REVISION_ID");
    }

    public void testDataTransferFromRevisionToBuildResult() throws Exception
    {
        insertBuildRevision(1, 1, "rev1");
        insertBuildRevision(2, 2, "rev2");
        insertBuildRevision(3, 3, "rev3");

        runUpgrade();

        assertBuildResultRow(1, "rev1");
        assertBuildResultRow(2, "rev2");
        assertBuildResultRow(3, "rev3");
    }

    public void testUnexpectedNoRevisionForBuild() throws Exception
    {
        insertBuildResult(1);
        runUpgrade();
        assertBuildResultRow(1, null);
    }

    private void assertBuildResultRow(long buildId, String revisionString) throws SQLException
    {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            con = dataSource.getConnection();
            ps = con.prepareStatement("SELECT revision_string FROM build_result WHERE id = ?");
            JDBCUtils.setLong(ps, 1, buildId);
            rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals(revisionString, JDBCUtils.getString(rs, "revision_string"));
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(ps);
            JDBCUtils.close(con);
        }
    }

    private void insertBuildResult(long buildId) throws SQLException
    {
        Connection con = null;
        PreparedStatement ps = null;
        try
        {
            con = dataSource.getConnection();
            ps = con.prepareStatement("INSERT INTO build_result (id, number) values (?, 1)");

            JDBCUtils.setLong(ps, 1, buildId);
            assertEquals(1, ps.executeUpdate());
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(con);
        }

    }

    private void insertBuildRevision(long buildId, long revisionId, String revisionString) throws SQLException
    {
        Connection con = null;
        PreparedStatement ps = null;
        try
        {
            con = dataSource.getConnection();
            ps = con.prepareStatement("INSERT INTO revision (id, revisionstring, revisiontype) values (?, ?, 'type')");

            JDBCUtils.setLong(ps, 1, revisionId);
            JDBCUtils.setString(ps, 2, revisionString);
            assertEquals(1, ps.executeUpdate());

            JDBCUtils.close(ps);

            ps = con.prepareStatement("INSERT INTO build_result (id, revision_id, number) values (?, ?, 1)");
            JDBCUtils.setLong(ps, 1, buildId);
            JDBCUtils.setLong(ps, 2, revisionId);
            assertEquals(1, ps.executeUpdate());
            
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(con);
        }

    }

    private void runUpgrade() throws Exception
    {
        Connection con = null;
        try
        {
            con = dataSource.getConnection();
            task.execute(con);
            assertFalse(task.hasFailed());
        }
        finally
        {
            JDBCUtils.close(con);
        }
    }

    public void assertTableExists(String tableName)
    {
        assertTrue(JDBCUtils.tableExists(dataSource, tableName));
    }

    public void assertTableNotExists(String tableName)
    {
        assertFalse(JDBCUtils.tableExists(dataSource, tableName));
    }

    public void assertColumnExists(String tableName, String columnName)
    {
        assertTrue(JDBCUtils.columnExists(dataSource, tableName, columnName));
    }

    public void assertColumnNotExists(String tableName, String columnName)
    {
        assertFalse(JDBCUtils.columnExists(dataSource, tableName, columnName));
    }

    protected List<String> getMappings()
    {
        return Arrays.asList("com/zutubi/pulse/master/upgrade/tasks/schema/Schema-2.0.13-mappings.hbm.xml");
    }

}
