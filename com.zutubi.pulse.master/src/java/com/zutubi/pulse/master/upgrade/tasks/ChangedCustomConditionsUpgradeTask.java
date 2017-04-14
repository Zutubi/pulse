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
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newEditProperty("customCondition", new Function<Object, Object>()
        {
            public Object apply(Object o)
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
