package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds the SSL option to agents.
 */
public class AddAgentSslUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return false;
    }

    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(MasterConfigurationRegistry.AGENTS_SCOPE, PathUtils.WILDCARD_ANY_ELEMENT));
    }

    @Override
    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty("ssl", Boolean.toString(false)));
    }
}
