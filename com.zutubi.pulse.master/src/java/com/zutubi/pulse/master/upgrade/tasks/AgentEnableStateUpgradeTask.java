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
 * Sets agents in the UPGRADING and FAILED_UPGRADE states to DISABLED as hosts
 * now hold persistent upgrade information.
 */
public class AgentEnableStateUpgradeTask extends DatabaseUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(Connection con) throws IOException, SQLException
    {
        PreparedStatement update = null;
        try
        {
            update = con.prepareStatement("update AGENT_STATE set ENABLE_STATE = 'DISABLED' where ENABLE_STATE = 'FAILED_UPGRADE' or ENABLE_STATE = 'UPGRADING'");
            update.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(update);
        }
    }
}
