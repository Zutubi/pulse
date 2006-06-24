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
public class ConfigureDashboardProjectsAction extends UserActionSupport
{
    private boolean enableSelection = false;
    private Map<Long, String> allProjects;
    private List<Long> projects;
    private ProjectManager projectManager;

    public boolean isEnableSelection()
    {
        return enableSelection;
    }

    public void setEnableSelection(boolean enableSelection)
    {
        this.enableSelection = enableSelection;
    }

    public Map<Long, String> getAllProjects()
    {
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

        enableSelection = !user.getShowAllProjects();
        List<Project> all = projectManager.getAllProjects();
        Collections.sort(all, new NamedEntityComparator());
        allProjects = new LinkedHashMap<Long, String>();

        for(Project p: all)
        {
            allProjects.put(p.getId(), p.getName());
        }

        projects = new LinkedList<Long>();
        for(Project p: getUserManager().getDashboardProjects(user))
        {
            projects.add(p.getId());
        }

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
        user.setShowAllProjects(!enableSelection);
        user.clearProjects();
        if(enableSelection)
        {
            if(projects != null)
            {
                for(Long id: projects)
                {
                    Project p = projectManager.getProject(id);
                    if(p != null)
                    {
                        user.addProject(p);
                    }
                }
            }
        }
        else
        {
            user.setShowAllProjects(true);
        }

        getUserManager().save(user);

        return SUCCESS;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
