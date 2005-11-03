package com.cinnamonbob.web.project;

import com.cinnamonbob.core.model.StoredArtifact;

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

    public StoredArtifact getArtifact()
    {
        return artifact;
    }

    public void validate()
    {

    }

    public String execute()
    {
        artifact = getBuildManager().getArtifact(id);
        try
        {
            inputStream = new FileInputStream(artifact.getFile());
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
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
