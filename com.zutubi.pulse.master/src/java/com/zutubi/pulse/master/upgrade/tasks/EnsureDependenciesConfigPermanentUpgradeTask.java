package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;

import java.util.Arrays;
import java.util.List;

/**
 * Adds the permanent flag to all of the projects dependencies configurations if
 * it is not already present.
 */
public class EnsureDependenciesConfigPermanentUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_DEPENDENCIES = "dependencies";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPath(PathUtils.getPath(SCOPE_PROJECTS, WILDCARD_ANY_ELEMENT, PROPERTY_DEPENDENCIES));
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddMetaProperty(Configuration.PERMANENT_KEY, Boolean.toString(true)));
    }
}