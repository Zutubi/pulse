package com.cinnamonbob.web;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.velocity.app.VelocityEngine;

import com.cinnamonbob.core.Bob;
import com.cinnamonbob.core.BuildResult;
import com.cinnamonbob.core.Project;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.views.velocity.VelocityManager;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionSupport;

/**
 * WW action for viewing info about a single project.
 * 
 * @author jsankey
 */
public class ViewBuildAction extends ActionSupport
{
    /**
     * The name of the project requested.
     */
    private String name;
    /**
     * The identifier of the build requested.
     */
    private int id;
    /**
     * The project for the build being viewed.
     */
	private Project project;
    /**
     * The result of the build being viewed.
     */
    private BuildResult result;
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
        
        name = ServletActionContext.getRequest().getParameter("project");
        String idString = ServletActionContext.getRequest().getParameter("id");
        
        if(idString == null)
        {
            errors.add("Required parameter 'id' not provided");
            return ERROR;
        }
        
        try
        {
            id = Integer.parseInt(idString);
        }
        catch(NumberFormatException e)
        {
            errors.add("Parameter build ID must be an integer (got '" + idString + "')");
            return ERROR;
        }
        
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
        
        result = project.getBuildResult(id);
        if(result == null)
        {
            errors.add("Could not load result for build " + idString + " of project '" + name + "'");
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
     * @return the project for the build being viewed
	 */
	public Project getProject()
	{
		return project;
	}

    /**
     * @return the id of the build being viewed
     */
    public int getId()
    {
        return id;
    }
    
    /**
     * @return the result of the build being viewed
     */
    public BuildResult getResult()
    {
        return result;
    }
    
    /**
     * @return a list of errors detected during execution
     */
    public List<String> getGenericErrors()
    {
        return errors;
    }
    
    /**
     * @return the velocity engine used for rendering
     */
    public VelocityEngine getEngine()
    {
        return VelocityManager.getInstance().getVelocityEngine();
    }
}
