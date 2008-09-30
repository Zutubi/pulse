package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds the new allowPersonalBuilds flag to agent configurations.
 */
public class AddAllowPersonalBuildsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_AGENTS = "agents";
    private static final String PROPERTY_NAME = "allowPersonalBuilds";

    public String getName()
    {
        return "Add Allow Personal Builds";
    }

    public String getDescription()
    {
        return "Adds a new flag for allowing personal builds to each agent configuration.";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_AGENTS, PathUtils.WILDCARD_ANY_ELEMENT));
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_NAME, Boolean.toString(true)));
    }
}
