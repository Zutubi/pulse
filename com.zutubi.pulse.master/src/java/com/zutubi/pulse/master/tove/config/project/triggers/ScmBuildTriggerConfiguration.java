package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.master.scheduling.EventTrigger;
import com.zutubi.pulse.master.scheduling.ScmChangeEventFilter;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.pulse.master.scm.ScmChangeEvent;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A trigger that fires when a code change is detected in the project's SCM.
 */
@SymbolicName("zutubi.scmTriggerConfig")
public class ScmBuildTriggerConfiguration extends TriggerConfiguration
{
    public Trigger newTrigger()
    {
        return new EventTrigger(ScmChangeEvent.class, getName(), ScmChangeEventFilter.class);
    }
}
