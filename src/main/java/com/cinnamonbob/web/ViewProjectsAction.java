package com.cinnamonbob.web;

import java.util.Collection;
import java.util.Map;

import com.cinnamonbob.core.Bob;
import com.cinnamonbob.core.Project;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionSupport;

/**
 * WW action for viewing the full list of projects.
 * 
 * @author jsankey
 */
public class ViewProjectsAction extends ActionSupport
{
    /**
     * Collection of all projects in the server.
     */
	private Collection<Project> projects;

	/**
     * Populates the action.
	 */
	public String execute()
	{
		Map app = (Map)ActionContext.getContext().get("application");
		Bob theBuilder = (Bob)app.get("bob");

		projects = theBuilder.getProjects();
		return SUCCESS;
	}
	
	/**
     * @return a collection of all projects in the server
	 */
	public Collection<Project> getProjects()
	{
		return projects;
	}
}
