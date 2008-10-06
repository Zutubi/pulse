package com.zutubi.pulse.master.scheduling;

import com.zutubi.events.Event;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.scm.ScmChangeEvent;

/**
 * A filter to ensure a trigger only fires for the SCM of the matching
 * project on an SCMChangeEvent.
 */
public class ScmChangeEventFilter implements EventTriggerFilter
{
    private ProjectManager projectManager;

    public boolean accept(Trigger trigger, Event event, TaskExecutionContext context)
    {
        ScmChangeEvent changeEvent = (ScmChangeEvent) event;
        ProjectConfiguration project = projectManager.getProjectConfig(trigger.getProject(), false);
        return project != null && project.getName().equals(changeEvent.getProjectConfiguration().getName());
    }

    public boolean dependsOnProject(Trigger trigger, long projectId)
    {
        return false;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
