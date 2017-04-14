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
import java.sql.SQLException;

/**
 * Changes the status of SUCCESS results with warnings to the new WARNINGS status.
 */
public class WarningStatusUpgradeTask extends DatabaseUpgradeTask
{
    @Override
    public void execute(Connection con) throws Exception
    {
        updateTable(con, "BUILD_RESULT", "STATE");
        updateTable(con, "RECIPE_RESULT", "STATE");
        updateTable(con, "COMMAND_RESULT", "stateName");
    }

    private void updateTable(Connection con, String table, String column) throws SQLException
    {
        PreparedStatement statement = con.prepareStatement("UPDATE " + table + " SET " + column + " = 'WARNINGS' WHERE " + column + " = 'SUCCESS' AND WARNING_FEATURE_COUNT > 0");
        try
        {
            statement.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(statement);
        }
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
