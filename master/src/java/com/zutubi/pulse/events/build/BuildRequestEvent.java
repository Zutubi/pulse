package com.zutubi.pulse.events.build;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.model.BuildReason;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.prototype.config.ProjectConfiguration;

/**
 */
public class BuildRequestEvent extends AbstractBuildRequestEvent
{
    private BuildReason reason;

    public BuildRequestEvent(Object source, BuildReason reason, ProjectConfiguration projectConfig, BuildRevision revision)
    {
        super(source, revision, projectConfig);
        this.reason = reason;
    }

    public Object getOwner()
    {
        return projectConfig;
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
