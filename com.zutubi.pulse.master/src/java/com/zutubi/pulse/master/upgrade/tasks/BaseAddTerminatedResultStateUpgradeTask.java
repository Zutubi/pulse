package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;
import com.zutubi.pulse.core.engine.api.ResultState;
import static java.util.Arrays.asList;

import java.util.LinkedList;
import java.util.List;

/**
 * A base upgrade task class for adding the terminated result state to any configurations that
 * currently contain the error result state.
 */
public abstract class BaseAddTerminatedResultStateUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    protected abstract String getPropertyName();

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return asList(RecordUpgraders.newEditProperty(getPropertyName(), new Function<Object, Object>()
        {
            public Object apply(Object o)
            {
                if (o != null && o instanceof String[])
                {
                    String[] results = (String[]) o;

                    List<String> updatedResults = new LinkedList<String>();
                    for (String result : results)
                    {
                        updatedResults.add(result);
                        if (result.equals(ResultState.ERROR.toString()))
                        {
                            updatedResults.add(ResultState.TERMINATED.toString());
                        }
                    }
                    return updatedResults.toArray(new String[updatedResults.size()]);
                }
                return o;
            }
        }));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}