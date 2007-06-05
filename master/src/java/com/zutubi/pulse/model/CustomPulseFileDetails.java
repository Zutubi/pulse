package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;

import java.util.Properties;

/**
 */
public class CustomPulseFileDetails extends PulseFileDetails
{
    private String pulseFile;

    public CustomPulseFileDetails()
    {

    }

    public CustomPulseFileDetails(String pulseFile)
    {
        this.pulseFile = pulseFile;
    }

    public CustomPulseFileDetails copy()
    {
        return new CustomPulseFileDetails(pulseFile);
    }

    public boolean isBuiltIn()
    {
        return false;
    }

    public String getType()
    {
        return "custom";
    }

    public Properties getProperties()
    {
        return new Properties();
    }

    public String getPulseFile(long id, ProjectConfiguration projectConfig, Project project, Revision revision, PatchArchive patch)
    {
        return getPulseFile();
    }

    public String getPulseFile()
    {
        return pulseFile;
    }

    public void setPulseFile(String pulseFile)
    {
        this.pulseFile = pulseFile;
    }
}
