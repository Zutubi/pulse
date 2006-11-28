package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.ProjectGroup;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.xwork.interceptor.Preparable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 */
public class ProjectGroupAction extends ActionSupport implements Preparable
{
    private long groupId;
    private String name;
    private List<Long> projects;
    private ProjectManager projectManager;
    private ProjectFormHelper helper;
    private ProjectGroup group = new ProjectGroup();

    public long getGroupId()
    {
        return groupId;
    }

    public void setGroupId(long groupId)
    {
        this.groupId = groupId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Long> getProjects()
    {
        return projects;
    }

    public void setProjects(List<Long> projects)
    {
        this.projects = projects;
    }

    public Map<Long, String> getAllProjects()
    {
        return helper.getAllEntities();
    }

    public List<String> getPrepareParameterNames()
    {
        return Arrays.asList("groupId");
    }


    public void prepare() throws Exception
    {
        if(groupId != 0)
        {
            group = projectManager.getProjectGroup(groupId);
            if(group == null)
            {
                groupId = 0;
            }
        }
    }

    public String doInput() throws Exception
    {
        if(group != null)
        {
            name = group.getName();
            projects = helper.convertToIds(group.getProjects());
        }

        return INPUT;
    }

    public void validate()
    {
        if(hasErrors())
        {
            return;
        }

        ProjectGroup g = projectManager.getProjectGroup(name);
        if(g != null && g.getId() != groupId)
        {
            addFieldError("name", getText("project.group.duplicate", Arrays.asList(name)));
        }
    }

    public String execute() throws Exception
    {
        group.setName(name);
        helper.convertFromIds(projects, group.getProjects());
        projectManager.save(group);
        return SUCCESS;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
        this.helper = new ProjectFormHelper(projectManager);
    }
}
