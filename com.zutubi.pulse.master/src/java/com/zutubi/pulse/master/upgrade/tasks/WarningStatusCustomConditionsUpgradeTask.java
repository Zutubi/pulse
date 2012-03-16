package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.util.UnaryFunction;

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
    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newEditProperty("customCondition", new UnaryFunction<Object, Object>()
        {
            public Object process(Object o)
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
