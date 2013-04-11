package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.pulse.master.events.build.BuildEvent;
import com.zutubi.pulse.master.events.build.PreStageEvent;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A pre-stage hook is executed just after a build stage is assigned to an agent
 * - before it has been dispatched.  The hook can be easily applied to multiple
 * stages.
 */
@SymbolicName("zutubi.preStageHookConfig")
@Form(fieldOrder = {"name", "applyToAllStages", "stages", "failOnError", "runTaskOnAgents", "runForPersonal", "allowManualTrigger"})
public class PreStageHookConfiguration extends AbstractStageHookConfiguration
{
    public boolean triggeredBy(BuildEvent event)
    {
        if (event instanceof PreStageEvent)
        {
            PreStageEvent pse = (PreStageEvent) event;
            long stage = pse.getStageNode().getStageHandle();
            return triggeredByBuildType(pse.getBuildResult()) && stageMatches(stage);
        }

        return false;
    }
}
