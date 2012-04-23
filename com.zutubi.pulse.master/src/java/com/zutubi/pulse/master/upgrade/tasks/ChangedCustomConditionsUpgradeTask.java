package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.util.UnaryFunction;

import java.util.Arrays;
import java.util.List;

/**
 * Upgrades custom subscription conditions for new changed(...) syntax.
 */
public class ChangedCustomConditionsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
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
                    o = condition.replaceAll("changed\\.by\\.me\\.since\\.healthy", "changed(by.me, since.healthy)")
                            .replaceAll("changed\\.by\\.me\\.since\\.success", "changed(by.me, since.success)")
                            .replaceAll("changed\\.by\\.me", "changed(by.me)");
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
