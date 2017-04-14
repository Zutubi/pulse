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
 * Update the existing dependency triggers, changing the trigger type from event to noop.
 * This is because the dependency triggering is now handled by the build scheduling system
 * rather than being triggered by a build completed event.  All new dependent build triggers
 * now use the noop trigger.
 */
public class DependentBuildTriggerUpgradeTask extends DatabaseUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(Connection con) throws IOException, SQLException
    {
        PreparedStatement ps = null;
        try
        {
            ps = con.prepareStatement("update LOCAL_TRIGGER set FILTER_CLASS = ?, TRIGGER_TYPE = ?, TRIGGER_EVENT = ? where FILTER_CLASS = ?");

            JDBCUtils.setString(ps, 1, null);
            JDBCUtils.setString(ps, 2, "NOOP");
            JDBCUtils.setString(ps, 3, null);
            JDBCUtils.setString(ps, 4, "com.zutubi.pulse.master.DependentBuildEventFilter");

            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }
    }
}

