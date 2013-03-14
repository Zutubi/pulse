package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds new useTemplateClient and view fields to PerforceConfiguration.
 */
public class AddPerforceViewUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String TYPE_PERFORCE = "zutubi.perforceConfig";
    private static final String PROPERTY_SCM = "scm";
    private static final String PROPERTY_USE_TEMPLATE_CLIENT = "useTemplateClient";
    private static final String PROPERTY_VIEW = "view";
    private static final String DEFAULT_VIEW = "//depot/... //pulse/...";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_PROJECTS, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_SCM)),
                TYPE_PERFORCE
        );
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(
                RecordUpgraders.newAddProperty(PROPERTY_USE_TEMPLATE_CLIENT, Boolean.toString(true)),
                RecordUpgraders.newAddProperty(PROPERTY_VIEW, DEFAULT_VIEW)
        );
    }
}