package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Converts existing email committers hook tasks to new send emails tasks.
 */
public class ConvertEmailCommittersTasksUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";

    private static final String TYPE_OLD = "zutubi.emailCommittersTaskConfig";
    private static final String TYPE_NEW = "zutubi.sendEmailTaskConfig";

    private static final String PROPERTY_HOOKS = "buildHooks";
    private static final String PROPERTY_TASK = "task";
    private static final String PROPERTY_CONTACTS = "emailContacts";
    private static final String PROPERTY_COMMITTERS = "emailCommitters";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        RecordLocator allHookTasks = RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_PROJECTS, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_HOOKS, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_TASK));
        return RecordLocators.newTypeFilter(allHookTasks, TYPE_OLD);
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(
                RecordUpgraders.newChangeSymbolicName(TYPE_OLD, TYPE_NEW),
                RecordUpgraders.newAddProperty(PROPERTY_CONTACTS, "false"),
                RecordUpgraders.newAddProperty(PROPERTY_COMMITTERS, "true")
        );
    }
}