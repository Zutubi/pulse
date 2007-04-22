package com.zutubi.pulse.scheduling;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.scm.SCMChangeEvent;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.prototype.config.ProjectConfiguration;

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
        return project != null && project.getName().equals(changeEvent.getSource().getName());
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
