package com.cinnamonbob.web;

import com.cinnamonbob.BuildQueue;
import com.opensymphony.xwork.ActionSupport;

/**
 * WW action for viewing the current server status.
 * 
 * @author jsankey
 */
public class ServerStatusAction extends ActionSupport
{
    /**
     * 
     */
    private BuildQueue buildQueue;

	/**
     * Populates the action.
	 */
	public String execute()
	{
		return SUCCESS;
	}

    public BuildQueue getBuildQueue()
    {
        return buildQueue;
    }

    public void setBuildQueue(BuildQueue buildQueue)
    {
        this.buildQueue = buildQueue;
    }
}
