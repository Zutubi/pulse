package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.MutableRecord;

import java.util.Arrays;
import java.util.List;

/**
 * Upgrade task to add the ability to explicitly subscribe to all projects
 * and/or subscribe by label.
 */
public class SubscribeByLabelUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_ALL_PROJECTS = "allProjects";
    private static final String PROPERTY_PROJECTS = "projects";
    private static final String PROPERTY_LABELS = "labels";

    public boolean haltOnFailure()
    {
        return false;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(RecordLocators.newPathPattern("users/*/preferences/subscriptions/*"), "zutubi.projectSubscriptionConfig");
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.<RecordUpgrader>asList(new RecordUpgrader()
        {
            public void upgrade(String path, MutableRecord record)
            {
                if (record.containsKey(PROPERTY_ALL_PROJECTS))
                {
                    return;
                }

                String[] projects = (String[]) record.get(PROPERTY_PROJECTS);
                boolean allProjectsValue = projects.length == 0;
                record.put(PROPERTY_ALL_PROJECTS, Boolean.toString(allProjectsValue));
                record.put(PROPERTY_LABELS, new String[0]);
            }
        });
    }
}