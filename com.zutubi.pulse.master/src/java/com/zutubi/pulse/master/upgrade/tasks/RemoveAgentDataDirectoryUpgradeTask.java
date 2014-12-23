package com.zutubi.pulse.master.upgrade.tasks;

import java.util.Arrays;
import java.util.List;

/**
 * Removes the old dataDirectory field from AgentConfiguration (it has moved to AgentStorageConfiguration).
 */
public class RemoveAgentDataDirectoryUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return false;
    }

    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern("agents/*");
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newDeleteProperty("dataDirectory"));
    }
}
