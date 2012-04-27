package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.pulse.master.events.build.BuildEvent;
import com.zutubi.pulse.master.events.build.TerminateStageEvent;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A terminate-stage hook is executed just after a request to terminate a
 * recipe/build stage is sent to an agent.
 */
@SymbolicName("zutubi.terminateStageHookConfig")
@Form(fieldOrder = {"name", "applyToAllStages", "stages", "failOnError", "runForPersonal", "allowManualTrigger"})
public class TerminateStageHookConfiguration extends AbstractStageHookConfiguration
{
    public boolean triggeredBy(BuildEvent event)
    {
        if (event instanceof TerminateStageEvent)
        {
            TerminateStageEvent tse = (TerminateStageEvent) event;
            long stage = tse.getStageNode().getStageHandle();
            return triggeredByBuildType(tse.getBuildResult()) && stageMatches(stage);
        }

        return false;
    }
}
