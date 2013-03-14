package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;

/**
 * Adds a new includeUpstreamCommitters option to the send email hook tasks.
 */
public class SendEmailUpstreamCommittersUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_HOOKS = "buildHooks";
    private static final String PROPERTY_TASK = "task";

    private static final String PROPERTY_INCLUDE_UPSTREAM = "includeUpstreamCommitters";

    private static final String TYPE_SEND_EMAIL = "zutubi.sendEmailTaskConfig";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        RecordLocator triggerLocator = RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_PROJECTS, WILDCARD_ANY_ELEMENT, PROPERTY_HOOKS, WILDCARD_ANY_ELEMENT, PROPERTY_TASK));
        return RecordLocators.newTypeFilter(triggerLocator, TYPE_SEND_EMAIL);
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_INCLUDE_UPSTREAM, "false"));
    }
}