package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.web.project.ProjectFormHelper;
import com.zutubi.pulse.web.project.ProjectGroupFormHelper;

import java.util.List;
import java.util.Map;

/**
 * Action allowing a user to configure which projects appear on their dashboard.
 */
public class ConfigureDashboardAction extends UserActionSupport
{
    private int buildCount;
    private Map<Long, String> allProjects;
    private boolean showAllProjects;
    private List<Long> projects;
    private Map<Long, String> allGroups;
    private List<Long> shownGroups;
    private boolean showMyChanges = false;
    private boolean showProjectChanges = false;
    private BuildManager buildManager;
    private ProjectFormHelper projectHelper;
    private ProjectGroupFormHelper groupHelper;

    public int getBuildCount()
    {
        return buildCount;
    }

    public void setBuildCount(int buildCount)
    {
        this.buildCount = buildCount;
    }

    public boolean isShowMyChanges()
    {
        return showMyChanges;
    }

    public void setShowMyChanges(boolean showMyChanges)
    {
        this.showMyChanges = showMyChanges;
    }

    public boolean isShowProjectChanges()
    {
        return showProjectChanges;
    }

    public void setShowProjectChanges(boolean showProjectChanges)
    {
        this.showProjectChanges = showProjectChanges;
    }

    public Map<Long, String> getAllProjects()
    {
        if(allProjects == null)
        {
            allProjects = projectHelper.getAllEntities();
        }

        return allProjects;
    }

    public boolean isShowAllProjects()
    {
        return showAllProjects;
    }

    public void setShowAllProjects(boolean showAllProjects)
    {
        this.showAllProjects = showAllProjects;
    }

    public List<Long> getProjects()
    {
        return projects;
    }

    public void setProjects(List<Long> projects)
    {
        this.projects = projects;
    }

    public Map<Long, String> getAllGroups()
    {
        if(allGroups == null)
        {
            allGroups = groupHelper.getAllEntities();
        }
        return allGroups;
    }

    public List<Long> getShownGroups()
    {
        return shownGroups;
    }

    public void setShownGroups(List<Long> shownGroups)
    {
        this.shownGroups = shownGroups;
    }

    public String doInput() throws Exception
    {
        String login = AcegiUtils.getLoggedInUser();
        if (login == null)
        {
            return ERROR;
        }

        setUserLogin(login);

        // load the user from the db.
        User user = getUser();
        if (user == null)
        {
            addUnknownUserActionError();
            return ERROR;
        }

        buildCount = user.getDashboardBuildCount();

        showAllProjects = user.getShowAllProjects();
        projects = projectHelper.convertToIds(user.getShownProjects());
        shownGroups = groupHelper.convertToIds(user.getShownGroups());

        showMyChanges = user.getShowMyChanges();
        showProjectChanges = user.getShowProjectChanges();

        return super.doInput();
    }

    public String execute() throws Exception
    {
        String login = AcegiUtils.getLoggedInUser();
        if (login == null)
        {
            return ERROR;
        }

        setUserLogin(login);

        User user = getUser();

        buildManager.cleanupBuilds(user);

        user.setDashboardBuildCount(buildCount);

        user.setShowAllProjects(showAllProjects);
        if(showAllProjects)
        {
            user.getShownProjects().clear();
        }
        else
        {
            projectHelper.convertFromIds(projects, user.getShownProjects());
        }

        groupHelper.convertFromIds(shownGroups, user.getShownGroups());

        user.setShowMyChanges(showMyChanges);
        user.setShowProjectChanges(showProjectChanges);
        
        getUserManager().save(user);

        return SUCCESS;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        projectHelper = new ProjectFormHelper(projectManager);
        groupHelper = new ProjectGroupFormHelper(projectManager);
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
