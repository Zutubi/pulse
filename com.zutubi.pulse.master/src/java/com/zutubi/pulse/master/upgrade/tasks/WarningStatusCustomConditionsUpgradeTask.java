package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;

import java.util.Arrays;
import java.util.List;

/**
 * Upgrades custom subscription conditions for changes for the new warning status.
 */
public class WarningStatusCustomConditionsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern("users/*/preferences/subscriptions/*/condition"),
                "zutubi.customConditionConfig"
        );
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newEditProperty("customCondition", new Function<Object, Object>()
        {
            public Object apply(Object o)
            {
                if (o != null && o instanceof String)
                {
                    String condition = (String) o;
                    o = condition.replaceAll("unsuccessful", "broken").replaceAll("success", "healthy");
                }

                return o;
            }
        }));
    }

    public boolean haltOnFailure()
    {
        return false;
    }
}
