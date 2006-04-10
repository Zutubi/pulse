package com.zutubi.pulse.model;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.scm.SCMException;

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

    public String getBobFile(long id, Project project, Revision revision)
    {
        Scm scm = project.getScm();

        try
        {
            return scm.createServer().checkout(id, revision, bobFileName);
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
