package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.zutubi.tove.type.record.Record;

import java.util.Arrays;
import java.util.List;

/**
 * Updates test post-processor's resolveConflicts property to reflect the branching of OFF into multiple options.
 */
public class UpdateNameConflictResolutionUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY = "resolveConflicts";

    public boolean haltOnFailure()
    {
        return true;
    }

    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPredicateFilter(RecordLocators.newPathPattern("projects/*/postProcessors/*"), new Predicate<Record>()
        {
            public boolean apply(Record input)
            {
                return input.containsKey(PROPERTY);
            }
        });
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newEditProperty(PROPERTY, new Function<Object, Object>()
        {
            public Object apply(Object input)
            {
                if (input != null && input instanceof String)
                {
                    String value = (String) input;
                    if (value.equals("OFF"))
                    {
                        return "WORST_RESULT";
                    }
                }

                return input;
            }
        }));
    }
}
