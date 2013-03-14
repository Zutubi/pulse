package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;

/**
 * Adds a new property to resource requirements allowing them to be marked as
 * optional.
 */
public class ResourceRequirementOptionalUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    // Scope and path to the records we are interested in
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_STAGES = "stages";
    private static final String PROPERTY_REQUIREMENTS = "requirements";

    // New property
    private static final String PROPERTY_OPTIONAL = "optional";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newUnion(
                RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_PROJECTS, WILDCARD_ANY_ELEMENT, PROPERTY_REQUIREMENTS, WILDCARD_ANY_ELEMENT)),
                RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_PROJECTS, WILDCARD_ANY_ELEMENT, PROPERTY_STAGES, WILDCARD_ANY_ELEMENT, PROPERTY_REQUIREMENTS, WILDCARD_ANY_ELEMENT))
        );
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(
                RecordUpgraders.newAddProperty(PROPERTY_OPTIONAL, Boolean.toString(false))
        );
    }
}
