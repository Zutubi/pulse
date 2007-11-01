package com.zutubi.pulse.prototype.config.project.hooks;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.BuildEvent;
import com.zutubi.pulse.model.BuildResult;

/**
 * Manages the execution of build hooks at various points in the build
 * process.
 */
public class BuildHookManager implements EventListener
{
    public void handleEvent(Event event)
    {
        BuildEvent be = (BuildEvent) event;
        BuildResult buildResult = be.getBuildResult();
        if (!buildResult.isPersonal())
        {
            for(BuildHookConfiguration hook: buildResult.getProject().getConfig().getBuildHooks().values())
            {
                if(hook.triggeredBy(be))
                {
                    executeTask(hook, buildResult);
                }
            }
        }
    }

    public void executeTask(BuildHookConfiguration hook, BuildResult buildResult)
    {
        // Manual trigger:
        //   - build hook: run over result
        //   - stage hook: for each stage, if applicable, run over stage
        //   - no fail on error behaviour?
        // Event trigger:
        //   - build hook: run over result
        //   - stage hook: run over stage
        BuildHookTaskConfiguration task = hook.getTask();
        // FIXME hooks
        BuildHookContext context = new BuildHookContext();
        task.execute(context);
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{BuildEvent.class};
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }
}
