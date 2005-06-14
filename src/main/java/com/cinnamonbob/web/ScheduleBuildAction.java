package com.cinnamonbob.web;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.cinnamonbob.BobServer;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionSupport;

/**
 * WW action for viewing the current server status.
 * 
 * @author jsankey
 */
public class ScheduleBuildAction extends ActionSupport
{
    /**
     * The project to schedule a build for.
     */
    private String project;
    /**
     * A list of errors detected during execution.
     */
    private List<String> errors;

    /**
     * Populates the action.
	 */
	public String execute()
	{
        Map       app    = (Map)ActionContext.getContext().get("application");
        //BobServer server = (BobServer)app.get("server");
        
        errors = new LinkedList<String>();

        project = ServletActionContext.getRequest().getParameter("project");
        
        if(project == null)
        {
            errors.add("Required parameter 'project' not provided");
            return ERROR;
        }

        // TODO: verify success...
        BobServer.build(project);
        return SUCCESS;
	}
	
    /**
     * @return the name of the project scheduled
     */
    public String getProject()
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
