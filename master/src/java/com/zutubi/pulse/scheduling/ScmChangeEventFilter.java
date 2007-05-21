package com.zutubi.pulse.scheduling;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.prototype.config.ProjectConfiguration;
import com.zutubi.pulse.scm.ScmChangeEvent;

/**
 * A filter to ensure a trigger only fires for the SCM of the matching
 * project on an SCMChangeEvent.
 */
public class ScmChangeEventFilter implements EventTriggerFilter
{
    private ProjectManager projectManager;

    public boolean accept(Trigger trigger, Event event)
    {
        ScmChangeEvent changeEvent = (ScmChangeEvent) event;
        ProjectConfiguration project = projectManager.getProjectConfig(trigger.getProject());
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
