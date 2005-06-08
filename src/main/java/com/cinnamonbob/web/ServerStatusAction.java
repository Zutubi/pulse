package com.cinnamonbob.web;

import java.util.Map;

import com.cinnamonbob.BobServer;
import com.opensymphony.xwork.ActionContext;
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
	private BobServer server;

	/**
     * Populates the action.
	 */
	public String execute()
	{
		Map app = (Map)ActionContext.getContext().get("application");
		server = (BobServer)app.get("server");
		return SUCCESS;
	}
	
	/**
     * @return a reference to the server
	 */
	public BobServer getServer()
	{
		return server;
	}
}
