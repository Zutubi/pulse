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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Removes the old tree structure formed with recipe result nodes by copying
 * them to a new STAGE_RESULT table linked directly to builds.
 */
public class RemoveBuildNodeHierarchyUpgradeTask extends DatabaseUpgradeTask
{
    private static final int MAX_BATCH_SIZE = 100;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(Connection con) throws IOException, SQLException
    {
        boolean originalAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try
        {
            copyStages(con);
            con.commit();
        }
        finally
        {
            con.setAutoCommit(originalAutoCommit);
        }
    }

    private void copyStages(Connection con) throws SQLException
    {
        PreparedStatement query = null;
        PreparedStatement insert = con.prepareStatement("insert into STAGE_RESULT values (?, ?, ?, ?, ?, ?, ?)");
        ResultSet rs = null;
        try
        {
            query = con.prepareStatement("select RECIPE_RESULT_NODE.ID, " +
                                                "RECIPE_RESULT_NODE.HOST, " +
                                                "RECIPE_RESULT_NODE.STAGENAME, " +
                                                "RECIPE_RESULT_NODE.STAGE_HANDLE, " +
                                                "RECIPE_RESULT_NODE.RECIPE_RESULT_ID, " +
                                                "BUILD_RESULT.ID, " +
                                                "RECIPE_RESULT_NODE.ORDINAL " +
                                         "from RECIPE_RESULT_NODE, BUILD_RESULT " +
                                         "where BUILD_RESULT.RECIPE_RESULT_ID = RECIPE_RESULT_NODE.PARENT_ID");
            rs = query.executeQuery();
            int i = 0;
            while (rs.next())
            {
                insert.setLong(1, rs.getLong(1));
                insert.setString(2, rs.getString(2));
                insert.setString(3, rs.getString(3));
                insert.setLong(4, rs.getLong(4));
                insert.setLong(5, rs.getLong(5));
                insert.setLong(6, rs.getLong(6));
                insert.setInt(7, rs.getInt(7));
                insert.addBatch();

                if (++i % MAX_BATCH_SIZE == 0)
                {
                    insert.executeBatch();
                }
            }

            if (i % MAX_BATCH_SIZE != 0)
            {
                insert.executeBatch();
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(query);
        }
    }
}