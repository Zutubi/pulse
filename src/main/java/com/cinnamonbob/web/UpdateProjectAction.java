package com.cinnamonbob.web;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.cinnamonbob.core.Bob;
import com.cinnamonbob.core.Project;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionSupport;

/**
 * WW action for viewing the current server status.
 * 
 * @author jsankey
 */
public class UpdateProjectAction extends ActionSupport
{
    /**
     * The project to update.
     */
    private Project project;
    /**
     * A list of errors detected during execution.
     */
    private List<String> errors;

    /**
     * Populates the action.
	 */
	public String execute()
	{
        Map app        = (Map)ActionContext.getContext().get("application");
        Bob theBuilder = (Bob)app.get("bob");
        
        errors = new LinkedList<String>();

        String name = ServletActionContext.getRequest().getParameter("project");        
        if(name == null)
        {
            errors.add("Required parameter 'project' not provided");
            return ERROR;
        }
        
        project = theBuilder.getProject(name);
        if(project == null)
        {
            errors.add("Unknown project '" + name + "'");
            return ERROR;
        }

        String actionString = ServletActionContext.getRequest().getParameter("action");
        if(actionString == null)
        {
            errors.add("Required parameter 'action' not provided");
            return ERROR;
        }

        if(actionString.equals("pause"))
        {
            project.pause();
        }
        else if(actionString.equals("resume"))
        {
            project.resume();
        }
        else
        {
            errors.add("Invalid action '" + actionString + "'");
        }
        
        return SUCCESS;
	}
	
    /**
     * @return the project being updated
     */
    public Project getProject()
    {
        return project;
    }
    
    /**
     * @return a list of errors detected during execution
     */
    public List<String> getGenericErrors()
    {
        return errors;
    }
}
