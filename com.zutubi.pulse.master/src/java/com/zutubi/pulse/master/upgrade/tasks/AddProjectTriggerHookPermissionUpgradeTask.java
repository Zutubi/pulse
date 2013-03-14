package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;
import com.zutubi.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds a separate permission for project hook triggering, updating ACLs that have the existing
 * write permission.
 */
public class AddProjectTriggerHookPermissionUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern("projects/*/permissions/*");
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newEditProperty("allowedActions", new Function<Object, Object>()
        {
            public Object apply(Object o)
            {
                if (o != null && o instanceof String[])
                {
                    String[] allowedActions = (String[]) o;
                    if (CollectionUtils.contains(allowedActions, "write"))
                    {
                        String[] editedActions = new String[allowedActions.length + 1];
                        System.arraycopy(allowedActions, 0, editedActions, 0, allowedActions.length);
                        editedActions[editedActions.length - 1] = "triggerHook";
                        o = editedActions;
                    }
                }

                return o;
            }
        }));
    }
}
