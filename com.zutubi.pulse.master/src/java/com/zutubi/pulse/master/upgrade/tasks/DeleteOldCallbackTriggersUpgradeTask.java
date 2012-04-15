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
