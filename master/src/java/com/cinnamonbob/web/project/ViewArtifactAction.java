package com.cinnamonbob.web.project;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.StoredArtifact;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * 
 *
 */
public class ViewArtifactAction extends ProjectActionSupport
{
    private long id;
    private long commandId;
    private CommandResult commandResult;
    private StoredArtifact artifact;
    private InputStream inputStream;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getCommandId()
    {
        return commandId;
    }

    public void setCommandId(long commandId)
    {
        this.commandId = commandId;
    }

    public StoredArtifact getArtifact()
    {
        return artifact;
    }

    public void validate()
    {
        commandResult = getBuildManager().getCommandResult(commandId);
        if (commandResult == null)
        {
            addActionError("Unknown command result '" + commandId + "'");
        }

        artifact = getBuildManager().getArtifact(id);
        if (artifact == null)
        {
            addActionError("Unknown artifact '" + id + "'");
        }
    }

    public String execute()
    {
        File artifactFile = new File(commandResult.getOutputDir(), artifact.getFile());

        try
        {
            inputStream = new FileInputStream(artifactFile.getAbsolutePath());
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            addActionError("Unable to open artifact file: " + e.getMessage());
            return ERROR;
        }
        return SUCCESS;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    /**
     * @return the content type of the artifact being viewed
     */
    public String getContentType()
    {
        return artifact.getType();
    }
}
