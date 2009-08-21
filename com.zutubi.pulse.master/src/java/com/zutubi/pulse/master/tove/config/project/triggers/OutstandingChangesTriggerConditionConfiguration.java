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
