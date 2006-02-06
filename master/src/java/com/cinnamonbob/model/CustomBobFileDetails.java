package com.cinnamonbob.model;

import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.scm.SCMException;

import java.util.Properties;

/**
 */
public class CustomBobFileDetails extends BobFileDetails
{
    private String bobFileName;

    public CustomBobFileDetails()
    {

    }

    public CustomBobFileDetails(String bobFileName)
    {
        this.bobFileName = bobFileName;
    }

    public String getType()
    {
        return "custom";
    }

    public Properties getProperties()
    {
        // TODO i18n
        Properties result = new Properties();
        result.put("cinnabo file", bobFileName);
        return result;
    }

    public String getBobFile(Project project, Revision revision)
    {
        Scm scm = project.getScm();

        try
        {
            return scm.createServer().checkout(revision, bobFileName);
        }
        catch (SCMException e)
        {
            throw new BuildException("Unable to retrieve bob file from SCM: " + e.getMessage());
        }
    }

    public String getBobFileName()
    {
        return bobFileName;
    }

    public void setBobFileName(String bobFileName)
    {
        this.bobFileName = bobFileName;
    }

}
