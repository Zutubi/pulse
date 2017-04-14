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
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.io.IOException;

/**
 * Update the result state string in the database to keep it in line with the
 * rename of the INITIAL state to PENDING
 */
public class RenameInitialResultStateToPendingUpgradeTask  extends DatabaseUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(Connection con) throws IOException, SQLException
    {
        executeUpdate(con, "update BUILD_RESULT set STATE = 'PENDING' where STATE = 'INITIAL'");
        executeUpdate(con, "update RECIPE_RESULT set STATE = 'PENDING' where STATE = 'INITIAL'");
        executeUpdate(con, "update COMMAND_RESULT set stateName = 'PENDING' where stateName = 'INITIAL'");
    }

    private void executeUpdate(Connection con, String sql) throws SQLException
    {
        PreparedStatement statement = null;
        try
        {
            statement = con.prepareStatement(sql);
            statement.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(statement);
        }
    }
}
