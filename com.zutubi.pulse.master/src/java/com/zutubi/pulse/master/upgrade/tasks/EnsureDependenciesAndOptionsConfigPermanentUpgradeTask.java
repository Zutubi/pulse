package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.config.api.Configuration;
import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;
import static com.zutubi.tove.type.record.PathUtils.getPath;

import java.util.Arrays;
import java.util.List;

/**
 * Adds the permanent flag to all of the projects dependencies and options configurations
 * if it is not already present.
 */
public class EnsureDependenciesAndOptionsConfigPermanentUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    
    private static final String PROPERTY_DEPENDENCIES = "dependencies";
    private static final String PROPERTY_OPTIONS = "options";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(
                getPath(SCOPE_PROJECTS, WILDCARD_ANY_ELEMENT, PROPERTY_DEPENDENCIES),
                getPath(SCOPE_PROJECTS, WILDCARD_ANY_ELEMENT, PROPERTY_OPTIONS)
        );
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddMetaProperty(Configuration.PERMANENT_KEY, Boolean.toString(true)));
    }
}