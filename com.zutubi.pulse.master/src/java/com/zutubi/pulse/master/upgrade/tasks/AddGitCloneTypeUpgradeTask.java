package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Adds a new cloneType field to git configuration based on the current
 * trackSelectedBranch value.
 */
public class AddGitCloneTypeUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_SCM = "scm";
    private static final String PROPERTY_CLONE_TYPE = "cloneType";
    private static final String PROPERTY_TRACK_SELECTED_BRANCH = "trackSelectedBranch";
    private static final String TYPE_GIT = "zutubi.gitConfig";
    private static final String CLONE_TYPE_NORMAL = "NORMAL";
    private static final String CLONE_TYPE_SELECTED_BRANCH = "SELECTED_BRANCH_ONLY";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_PROJECTS, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_SCM)),
                TYPE_GIT
        );
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        RecordUpgrader upgrader = new RecordUpgrader()
        {
            public void upgrade(String path, MutableRecord record)
            {
                if (record.containsKey(PROPERTY_CLONE_TYPE))
                {
                    return;
                }

                String trackSelected = (String) record.get(PROPERTY_TRACK_SELECTED_BRANCH);
                String value = CLONE_TYPE_NORMAL;
                if (trackSelected != null && Boolean.parseBoolean(trackSelected))
                {
                    value = CLONE_TYPE_SELECTED_BRANCH;
                }

                record.put(PROPERTY_CLONE_TYPE, value);
            }
        };

        return asList(upgrader, new DeletePropertyRecordUpgrader(PROPERTY_TRACK_SELECTED_BRANCH));
    }
}
