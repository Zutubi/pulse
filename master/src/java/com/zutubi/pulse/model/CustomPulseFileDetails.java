package com.zutubi.pulse.model;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.scm.SCMException;

import java.util.Properties;

/**
 */
public class CustomPulseFileDetails extends PulseFileDetails
{
    private String pulseFileName;

    public CustomPulseFileDetails()
    {

    }

    public CustomPulseFileDetails(String pulseFileName)
    {
        this.pulseFileName = pulseFileName;
    }

    public String getType()
    {
        return "custom";
    }

    public Properties getProperties()
    {
        // TODO i18n
        Properties result = new Properties();
        result.put("pulse file", pulseFileName);
        return result;
    }

    public String getPulseFile(long id, Project project, Revision revision)
    {
        Scm scm = project.getScm();

        try
        {
            return scm.createServer().checkout(id, revision, pulseFileName);
        }
        catch (SCMException e)
        {
            throw new BuildException("Unable to retrieve pulse file from SCM: " + e.getMessage());
        }
    }

    public String getPulseFileName()
    {
        return pulseFileName;
    }

    public void setPulseFileName(String pulseFileName)
    {
        this.pulseFileName = pulseFileName;
    }

}
