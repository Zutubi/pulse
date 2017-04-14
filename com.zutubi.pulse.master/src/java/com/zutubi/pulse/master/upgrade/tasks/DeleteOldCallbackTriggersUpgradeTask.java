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
import com.zutubi.util.adt.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.LinkedList;
import java.util.List;

import static com.zutubi.util.CollectionUtils.asPair;

/**
 * Delete the triggers from the local trigger table that have been migrated
 * to the new non-persistent callback mechanism within the scheduler.
 */
public class DeleteOldCallbackTriggersUpgradeTask extends DatabaseUpgradeTask
{
    public void execute(Connection con) throws Exception
    {
        List<Pair<String, String>> triggersToDelete = new LinkedList<Pair<String, String>>();
        triggersToDelete.add(asPair("poll", "scm"));
        triggersToDelete.add(asPair("ping", "services"));
        triggersToDelete.add(asPair("statistics", "services"));
        triggersToDelete.add(asPair("cleanup", "services"));
        triggersToDelete.add(asPair("lastAccess", "services"));

        PreparedStatement delete = null;
        try
        {
            delete = con.prepareStatement("DELETE FROM LOCAL_TRIGGER WHERE TRIGGER_NAME = ? AND TRIGGER_GROUP = ? ");
            for (Pair<String, String> triggerToDelete : triggersToDelete)
            {
                delete.setString(1, triggerToDelete.getFirst());
                delete.setString(2, triggerToDelete.getSecond());
                delete.executeUpdate();
            }
        }
        finally
        {
            JDBCUtils.close(delete);
        }
    }

    public boolean haltOnFailure()
    {
        return false;
    }
}
