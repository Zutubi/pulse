package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.NamedEntityComparator;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.security.AcegiUtils;

import java.util.*;

/**
 * Action allowing a user to configure which projects appear on their dashboard.
 */
public class ConfigureDashboardAction extends UserActionSupport
{
    private int buildCount;
    private Map<Long, String> allProjects;
    private List<Long> projects;
    private ProjectManager projectManager;
    private boolean showMyChanges = false;
    private boolean showProjectChanges = false;

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
            List<Project> all = projectManager.getAllProjects();
            Collections.sort(all, new NamedEntityComparator());
            allProjects = new LinkedHashMap<Long, String>();

            for(Project p: all)
            {
                allProjects.put(p.getId(), p.getName());
            }
        }

        return allProjects;
    }

    public List<Long> getProjects()
    {
        return projects;
    }

    public void setProjects(List<Long> projects)
    {
        this.projects = projects;
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

        List<Project> all = projectManager.getAllProjects();
        Set<Project> hidden = user.getHiddenProjects();
        all.removeAll(hidden);
        projects = new LinkedList<Long>();
        for(Project p: all)
        {
            projects.add(p.getId());
        }

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

        user.setDashboardBuildCount(buildCount);
        user.clearProjects();

        List<Project> all = projectManager.getAllProjects();
        for(Project p: all)
        {
            if(projects == null || !projects.contains(p.getId()))
            {
                user.addHiddenProject(p);
            }
        }

        user.setShowMyChanges(showMyChanges);
        user.setShowProjectChanges(showProjectChanges);
        
        getUserManager().save(user);

        return SUCCESS;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
