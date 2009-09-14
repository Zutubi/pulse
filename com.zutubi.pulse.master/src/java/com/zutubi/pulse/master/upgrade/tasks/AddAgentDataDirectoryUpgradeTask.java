package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Upgrade task to add the dataDirectory property to instances of AgentConfiguration.
 */
public class AddAgentDataDirectoryUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE = "agents";
    private static final String PROPERTY = "dataDirectory";

    public boolean haltOnFailure()
    {
        return false;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(SCOPE, PathUtils.WILDCARD_ANY_ELEMENT));
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY, "${data.dir}/agents/${agent}"));
    }
}
