package com.cinnamonbob.web.project;

import java.util.List;

import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.Schedule;
import com.cinnamonbob.model.Scm;

/**
 * 
 *
 */
public class ManageScheduleAction extends ProjectActionSupport
{
    private long projectId;
    private String name;
    private Schedule schedule;
    private Project project;
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public void setProjectId(long id)
    {
        projectId = id;
    }

    public Project getProject()
    {
        return project;
    }
    
    public Schedule getSchedule()
    {
        return schedule;
    }
    
    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        // ensure that the name is unique to the project.
        project = getProjectManager().getProject(projectId);
        schedule = project.getSchedule(name);
        if(schedule == null)
        {
            addActionError("Schedule with name '" + name + "' does not exist in this project.");
        }        
    }

    public String execute()
    {
        return SUCCESS;
    }

}
