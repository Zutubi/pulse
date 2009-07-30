package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.BuildRevision;
import static com.zutubi.pulse.core.dependency.ivy.IvyManager.STATUS_INTEGRATION;
import com.zutubi.pulse.core.model.NamedEntity;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

import java.io.File;

/**
 * A request for a personal build.
 */
public class PersonalBuildRequestEvent extends AbstractBuildRequestEvent
{
    private long number;
    private User user;
    private File patch;
    private String patchFormat;

    public PersonalBuildRequestEvent(Object source, long number, BuildRevision revision, User user, File patch, String patchFormat, ProjectConfiguration projectConfig)
    {
        super(source, revision, projectConfig, new TriggerOptions(new PersonalBuildReason(user.getLogin()), null));
        this.number = number;
        this.user = user;
        this.patch = patch;
        this.patchFormat = patchFormat;
    }

    public NamedEntity getOwner()
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
        result.setStatus(STATUS_INTEGRATION);
        return result;
    }

    public File getPatch()
    {
        return patch;
    }

    public String getPatchFormat()
    {
        return patchFormat;
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
