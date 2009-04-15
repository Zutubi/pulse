package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.personal.PatchArchive;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * A request for a personal build.
 */
public class PersonalBuildRequestEvent extends AbstractBuildRequestEvent
{
    private long number;
    private User user;
    private PatchArchive patch;

    public PersonalBuildRequestEvent(Object source, long number, BuildRevision revision, User user, PatchArchive patch, ProjectConfiguration projectConfig)
    {
        super(source, revision, projectConfig, new TriggerOptions(new PersonalBuildReason(user.getLogin()), revision.getRevision(), null));
        this.number = number;
        this.user = user;
        this.patch = patch;
    }

    public Entity getOwner()
    {
        return user;
    }

    public boolean isPersonal()
    {
        return true;
    }

    public BuildResult createResult(ProjectManager projectManager, UserManager userManager)
    {
        Project project = projectManager.getProject(getProjectConfig().getProjectId(), false);
        BuildResult result = new BuildResult(options.getReason(), user, project, number);
        // although a personal build doesn't have it's ivy file published, it still
        // requires a status.
        result.setStatus("integration");
        return result;
    }

    public PatchArchive getPatch()
    {
        return patch;
    }

    public long getNumber()
    {
        return number;
    }

    public User getUser()
    {
        return user;
    }

    public String toString()
    {
        String result = "Personal Build Request Event: " + number;
        if(user != null)
        {
            result += ": " + user.getLogin();
        }
        return result;
    }
}
