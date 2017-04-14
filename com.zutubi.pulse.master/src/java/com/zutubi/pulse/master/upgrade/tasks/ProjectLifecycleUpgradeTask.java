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
import java.sql.SQLException;

/**
 * This upgrade task puts all projects into the initial state to make sure they
 * fit in with new project lifecycle changes.
 */
public class ProjectLifecycleUpgradeTask extends DatabaseUpgradeTask
{
    public boolean haltOnFailure()
    {
        return false;
    }

    public void execute(Connection con) throws IOException, SQLException
    {
        PreparedStatement statement = null;
        try
        {
            // This takes the chance that no Git project (ones requiring init
            // at this point) is paused.  There is a workaround of manual
            // re-init.  It seems better than unpausing everyone's paused
            // projects unnecessarily.
            statement = con.prepareStatement("update PROJECT set STATE = 'INITIAL' where STATE != 'PAUSED'");
            statement.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(statement);
        }
    }
}
