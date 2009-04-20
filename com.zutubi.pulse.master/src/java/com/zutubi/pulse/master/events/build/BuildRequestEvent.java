package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.master.model.*;

/**
 * A request for a project build.
 */
public class BuildRequestEvent extends AbstractBuildRequestEvent
{
    private Project owner;

    public BuildRequestEvent(Object source, Project project, BuildRevision buildRevision, TriggerOptions options)
    {
        super(source, buildRevision, project.getConfig(), options);
        this.owner = project;
    }

    public Entity getOwner()
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
        BuildResult result = new BuildResult(options.getReason(), project, projectManager.getNextBuildNumber(project), getRevision().isUser());

        String status = getProjectConfig().getDependencies().getStatus();
        if (getOptions().hasStatus())
        {
            status = options.getStatus();
        }
        result.setStatus(status);
        
        return result;
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Build Request Event");
        if (getProjectConfig() != null)
        {
            // should never be null, but then again, toString must never fail either.
            buff.append(": name: ").append(getProjectConfig().getName());
        }
        if (options.getReason() != null)
        {
            // should never be null, but then again, toString must never fail either.
            buff.append(": summary: ").append(options.getReason().getSummary());
        }
        if (options.getSource() != null)
        {
            buff.append(": source: ").append(options.getSource());
        }
        if(options.isReplaceable())
        {
            buff.append(" (replaceable)");
        }
        return buff.toString();
    }
}
