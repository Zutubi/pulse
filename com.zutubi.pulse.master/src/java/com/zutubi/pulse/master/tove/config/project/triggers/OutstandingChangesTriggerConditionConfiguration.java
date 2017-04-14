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

package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.master.trigger.OutstandingChangesTriggerCondition;
import com.zutubi.pulse.master.trigger.TriggerCondition;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for {@link com.zutubi.pulse.master.trigger.OutstandingChangesTriggerCondition}.
 */
@SymbolicName("zutubi.outstandingChangesTriggerConditionConfig")
@Form(fieldOrder = {"checkQueued"})
public class OutstandingChangesTriggerConditionConfiguration extends TriggerConditionConfiguration
{
    private boolean checkQueued = true;

    @Override
    public Class<? extends TriggerCondition> conditionType()
    {
        return OutstandingChangesTriggerCondition.class;
    }

    public boolean isCheckQueued()
    {
        return checkQueued;
    }

    public void setCheckQueued(boolean checkQueued)
    {
        this.checkQueued = checkQueued;
    }
}
