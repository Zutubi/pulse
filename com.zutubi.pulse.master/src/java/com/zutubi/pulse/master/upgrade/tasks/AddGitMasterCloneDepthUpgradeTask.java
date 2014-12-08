package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Adds a new master clone depth field to git configuration.
 */
public class AddGitMasterCloneDepthUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_SCM = "scm";
    private static final String TYPE_GIT = "zutubi.gitConfig";

    public boolean haltOnFailure()
    {
        return false;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_PROJECTS, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_SCM)),
                TYPE_GIT
        );
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return asList(new AddPropertyRecordUpgrader("masterCloneDepth", "0"));
    }
}

