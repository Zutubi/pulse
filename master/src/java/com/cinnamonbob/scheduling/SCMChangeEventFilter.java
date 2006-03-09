package com.cinnamonbob.scheduling;

import com.cinnamonbob.events.Event;
import com.cinnamonbob.scm.SCMChangeEvent;
import com.cinnamonbob.model.ProjectManager;
import com.cinnamonbob.model.Project;

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

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
