package com.cinnamonbob.web;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.cinnamonbob.core.Artifact;
import com.cinnamonbob.core.Bob;
import com.cinnamonbob.core.BuildResult;
import com.cinnamonbob.core.CommandResultCommon;
import com.cinnamonbob.core.Project;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionSupport;

/**
 * WW action for viewing info about a single project.
 * 
 * @author jsankey
 */
public class ViewArtifactAction extends ActionSupport
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
     * The name of the command that produced the artifact.
     */
    private String commandName;
    /**
     * The result of the commmand that produced the artifact.
     */
    private CommandResultCommon commandResult;
    /**
     * The name of the artifact being viewed.
     */
    private String artifactName;
    /**
     * The artifact being viewed.
     */
    private Artifact artifact;
    /**
     * Stream for reading the contents of the artifact.
     */
    private FileInputStream inputStream;
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
        
        commandName = ServletActionContext.getRequest().getParameter("command");
        if(commandName == null)
        {
            errors.add("Required parameter 'command' not provided");
            return ERROR;
        }
        
        commandResult = result.getCommandResult(commandName);
        if(commandResult == null)
        {
            errors.add("Could not find command '" + commandName + "' in the build result");
            return ERROR;
        }
        
        artifactName = ServletActionContext.getRequest().getParameter("artifact");
        if(artifactName == null)
        {
            errors.add("Required parameter '" + artifact + "' not provided");
            return ERROR;
        }
        
        artifact = commandResult.getArtifact(artifactName);
        if(commandResult == null)
        {
            errors.add("Could not find artifact '" + artifactName + "' in the command result");
            return ERROR;
        }
        
        try
        {
            inputStream = new FileInputStream(artifact.getFile());
        }
        catch(FileNotFoundException e)
        {
            errors.add(e.getMessage());
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
     * @return an input stream to read to get the content of the artifact
     */
    public InputStream getInputStream()
    {
        return inputStream;
    }
}
