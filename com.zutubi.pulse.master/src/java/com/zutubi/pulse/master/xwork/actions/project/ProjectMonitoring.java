package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.scm.config.api.PollableScmConfiguration;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.triggers.ScmBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerConfiguration;

import java.util.Map;

/**
 * A concept used to indicate if and how a project is being monitored for
 * changes.
 */
public enum ProjectMonitoring
{
    /**
     * The project cannot build because it is not initialised.
     */
    STOPPED,

    /**
     * The project is paused and thus ignoring all triggers.
     */
    PAUSED,

    /**
     * The project will build on trigger, but is not building on SCM changes.
     */
    NONE,

    /**
     * The project is monitoring the SCM and will build on change.
     */
    POLLING;

    /**
     * Determine the monitoring state for the specified project.
     *
     * @param project the project whose state is being determined
     * @return the projects monitoring state
     */
    public static ProjectMonitoring getMonitoring(Project project)
    {
        switch (project.getState())
        {
            case CLEANING:
            case INITIAL:
            case INITIALISATION_FAILED:
            case INITIALISING:
                return STOPPED;
            case PAUSE_ON_IDLE:
            case PAUSED:
                return PAUSED;
            default:
                if (isPolling(project))
                {
                    return POLLING;
                }
                else
                {
                    return NONE;
                }
        }
    }

    private static boolean isPolling(Project project)
    {
        ScmConfiguration scm = project.getConfig().getScm();
        if (scm != null && scm instanceof PollableScmConfiguration)
        {
            PollableScmConfiguration pollable = (PollableScmConfiguration) scm;
            if (pollable.isMonitor())
            {
                return hasScmTrigger(project);
            }
        }

        return false;
    }

    private static boolean hasScmTrigger(Project project)
    {
        @SuppressWarnings("unchecked")
        Map<String, TriggerConfiguration> triggers = (Map<String, TriggerConfiguration>) project.getConfig().getExtensions().get("triggers");
        for (TriggerConfiguration trigger : triggers.values())
        {
            if (trigger.getClass() == ScmBuildTriggerConfiguration.class)
            {
                return true;
            }
        }

        return false;
    }
}
