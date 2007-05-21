package com.zutubi.pulse.events.build;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.prototype.config.ProjectConfiguration;

/**
 */
public class BuildRequestEvent extends AbstractBuildRequestEvent
{
    private BuildReason reason;
    private Project project;

    public BuildRequestEvent(Object source, BuildReason reason, ProjectConfiguration projectConfig, Project project, BuildRevision revision)
    {
        super(source, revision, projectConfig);
        this.reason = reason;
        this.project = project;
    }

    public Entity getOwner()
    {
        return project;
    }

    public boolean isPersonal()
    {
        return false;
    }

    public BuildReason getReason()
    {
        return reason;
    }

    public BuildResult createResult(ProjectManager projectManager, UserManager userManager)
    {
        Project project = projectManager.getProject(getProjectConfig().getProjectId());
        return new BuildResult(reason, project, projectManager.getNextBuildNumber(project), getRevision().isUser());
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Build Request Event");
        if (getProjectConfig() != null)
        {
            // should never be null, but then again, toString must never fail either.
            buff.append(": ").append(getProjectConfig().getName());
        }
        if (getReason() != null)
        {
            // should never be null, but then again, toString must never fail either.
            buff.append(": ").append(getReason().getSummary());
        }
        return buff.toString();
    }
}
