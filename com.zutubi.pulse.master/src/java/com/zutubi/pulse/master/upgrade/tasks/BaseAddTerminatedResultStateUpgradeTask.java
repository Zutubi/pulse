/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;
import com.zutubi.pulse.core.engine.api.ResultState;

import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * A base upgrade task class for adding the terminated result state to any configurations that
 * currently contain the error result state.
 */
public abstract class BaseAddTerminatedResultStateUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    protected abstract String getPropertyName();

    protected List<? extends RecordUpgrader> getRecordUpgraders()
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