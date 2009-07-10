package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.model.NamedEntity;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.project.ResourcePropertyConfiguration;

import java.util.Collection;

/**
 * A request for a project build.
 */
public class BuildRequestEvent extends AbstractBuildRequestEvent
{
    private Project owner;

    public BuildRequestEvent(Object source, BuildReason reason, Project project, Collection<ResourcePropertyConfiguration> properties, BuildRevision revision, String requestSource, boolean replaceable)
    {
        super(source, revision, project.getConfig(), properties, reason, requestSource, replaceable);
        this.owner = project;
    }

    public NamedEntity getOwner()
    {
        return owner;
    }

    public boolean isPersonal()
    {
        return false;
    }

    public BuildResult createResult(ProjectManager projectManager, UserManager userManager)
    {
        Project project = projectManager.getProject(getProjectConfig().getProjectId(), false);
        return new BuildResult(getReason(), project, projectManager.getNextBuildNumber(project, true), getRevision().isUser());
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
        if (getRequestSource() != null)
        {
            buff.append(": ").append(getRequestSource());
        }
        if(isReplaceable())
        {
            buff.append(" (replaceable)");
        }
        return buff.toString();
    }
}
