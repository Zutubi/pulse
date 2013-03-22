package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.tove.type.record.PathUtils;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Adds the concurrentPersonalBuilds field to group configuration.
 */
public class AddGroupConcurrentPersonalBuildsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return false;
    }

    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(MasterConfigurationRegistry.GROUPS_SCOPE, PathUtils.WILDCARD_ANY_ELEMENT));
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return asList(RecordUpgraders.newAddProperty("concurrentPersonalBuilds", "1"));
    }
}
