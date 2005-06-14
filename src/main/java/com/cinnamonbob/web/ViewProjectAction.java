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
 * WW action for viewing info about a single project.
 * 
 * @author jsankey
 */
public class ViewProjectAction extends ActionSupport
{
    /**
     * The name of the project requested (null if not set).
     */
    private String name;
    /**
     * The project being viewed (null if not found).
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
		Map app = (Map)ActionContext.getContext().get("application");
        Bob theBuilder = (Bob)app.get("bob");
        errors = new LinkedList<String>();

        name = ServletActionContext.getRequest().getParameter("name");
        
        if(name == null)
        {
            errors.add("Required parameter 'name' not provided");
            return ERROR;
        }

        project = theBuilder.getProject(name);
        if(project == null)
        {
            errors.add("Unknown project '" + name + "'");
            return ERROR;
        }

        return SUCCESS;
	}
	
    /**
     * @return name of the project being viewed
     */
    public String getName()
    {
        return name;
    }
    
	/**
     * @return the project being viewed
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
