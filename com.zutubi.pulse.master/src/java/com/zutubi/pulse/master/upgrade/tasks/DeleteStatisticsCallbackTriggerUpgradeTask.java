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

/**
 * Deletes the agent statistics trigger that was missed by
 * {@link DeleteOldCallbackTriggersUpgradeTask} because it had its name and
 * group muddled.
 */
public class DeleteStatisticsCallbackTriggerUpgradeTask extends DatabaseUpgradeTask
{
    public boolean haltOnFailure()
    {
        return false;
    }

    public void execute(Connection con) throws Exception
    {
        PreparedStatement delete = null;
        try
        {
            delete = con.prepareStatement("DELETE FROM LOCAL_TRIGGER WHERE TRIGGER_NAME = 'services' AND TRIGGER_GROUP = 'statistics'");
            delete.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(delete);
        }
    }
}