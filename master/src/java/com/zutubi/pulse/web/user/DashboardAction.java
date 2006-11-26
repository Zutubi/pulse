package com.zutubi.pulse.web.user;

import com.zutubi.pulse.committransformers.CommitMessageTransformerManager;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.ChangelistComparator;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.web.project.CommitMessageHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Action to view the user's dashboard: their own Pulse "homepage".
 */
public class DashboardAction extends ActionSupport
{
    private User user;
    private List<BuildResult> myBuilds;
    private List<Project> shownProjects;
    private List<ProjectGroup> shownGroups;
    private List<Changelist> changelists;
    private List<Changelist> projectChangelists = null;

    private BuildManager buildManager;
    private UserManager userManager;
    private CommitMessageTransformerManager commitMessageTransformerManager;
    private CommitMessageHelper commitMessageHelper;
    private boolean contactError = false;

    public User getUser()
    {
        return user;
    }

    public List<BuildResult> getMyBuilds()
    {
        return myBuilds;
    }

    public List<Project> getShownProjects()
    {
        return shownProjects;
    }

    public List<ProjectGroup> getShownGroups()
    {
        return shownGroups;
    }

    public BuildColumns getColumns()
    {
        return new BuildColumns(user.getMyProjectsColumns(), projectManager);
    }
    
    public List<BuildResult> getLatestBuilds(Project p)
    {
        return buildManager.getLatestBuildResultsForProject(p, user.getDashboardBuildCount());
    }

    public List<Changelist> getChangelists()
    {
        return changelists;
    }

    public List<Changelist> getProjectChangelists()
    {
        return projectChangelists;
    }

    public boolean isContactError()
    {
        return contactError;
    }

    public String execute() throws Exception
    {
        String login = AcegiUtils.getLoggedInUser();
        if (login == null)
        {
            return "guest";
        }
        user = userManager.getUser(login);
        if (user == null)
        {
            return ERROR;
        }

        myBuilds = buildManager.getPersonalBuilds(user);

        if(user.getShowAllProjects())
        {
            shownProjects = projectManager.getAllProjects();
        }
        else
        {
            shownProjects = new ArrayList<Project>(user.getShownProjects());
        }
        
        Collections.sort(shownProjects, new NamedEntityComparator());

        shownGroups = new ArrayList<ProjectGroup>(user.getShownGroups());
        Collections.sort(shownGroups, new NamedEntityComparator());

        changelists = buildManager.getLatestChangesForUser(user, user.getMyChangesCount());
        Collections.sort(changelists, new ChangelistComparator());

        Set<Project> projects = userManager.getUserProjects(user, projectManager);
        if(projects.size() > 0 && user.getShowProjectChanges())
        {
            projectChangelists = buildManager.getLatestChangesForProjects(projects.toArray(new Project[]{}), user.getProjectChangesCount());
        }

        for(ContactPoint contact: user.getContactPoints())
        {
            if(contact.hasError())
            {
                contactError = true;
            }
        }

        return SUCCESS;
    }

    /**
     * Allow the template access to a specific project instance.
     *
     * @param id uniquely identifies a project.
     *
     * @return the project associated with the id, or null if it does
     * not exist.
     */
    public Project getProject(long id)
    {
        return projectManager.getProject(id);
    }

    /**
     * Allow the template access to a specific build result instance.
     *
     * @param id uniquely identifies a project.
     *
     * @return the build result associated with the id, or null if it does
     * not exist.
     */
    public BuildResult getResult(long id)
    {
        return buildManager.getBuildResult(id);
    }

    public String transformComment(Changelist changelist)
    {
        return getHelper().applyTransforms(changelist);
    }

    public String transformComment(Changelist changelist, int maxChars)
    {
        return getHelper().applyTransforms(changelist, maxChars);
    }

    protected CommitMessageHelper getHelper()
    {
        if(commitMessageHelper == null)
        {
            commitMessageHelper = new CommitMessageHelper(commitMessageTransformerManager.getCommitMessageTransformers());
        }
        return commitMessageHelper;
    }

    public boolean canWrite(Project project)
    {
        try
        {
            projectManager.checkWrite(project);
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }

    /**
     * Required resource.
     *
     * @param buildManager instance
     */
    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    /**
     * Required resource.
     *
     * @param userManager instance
     */
    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    /**
     * Required resource.
     *
     * @param transformerManager instance
     */
    public void setCommitMessageTransformerManager(CommitMessageTransformerManager transformerManager)
    {
        this.commitMessageTransformerManager = transformerManager;
    }
}
