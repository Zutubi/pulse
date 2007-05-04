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

    public BuildRequestEvent(Object source, BuildReason reason, ProjectConfiguration projectConfig, Project project, BuildRevision revision)
    {
        super(source, revision, projectConfig, project);
        this.reason = reason;
    }

    public Entity getOwner()
    {
        return getProject();
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
        return new BuildResult(reason, getProject(), projectManager.getNextBuildNumber(getProject()), getRevision().isUser());
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Build Request Event");
        if (getProject() != null)
        {
            buff.append(": ").append(getProject().getName());
        }
        if (getReason() != null)
        {
            buff.append(": ").append(getReason().getSummary());
        }
        return buff.toString();
    }
}
