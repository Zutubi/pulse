package com.zutubi.pulse.scheduling;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.scm.SCMChangeEvent;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.Project;

/**
 * A filter to ensure a trigger only fires for the SCM of the matching
 * project on an SCMChangeEvent.
 */
public class SCMChangeEventFilter implements EventTriggerFilter
{
    private ProjectManager projectManager;

    public boolean accept(Trigger trigger, Event event)
    {
        SCMChangeEvent changeEvent = (SCMChangeEvent) event;
        Project project = projectManager.getProject(trigger.getProject());
        return project != null && changeEvent.getScm().getId() == project.getScm().getId();
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
