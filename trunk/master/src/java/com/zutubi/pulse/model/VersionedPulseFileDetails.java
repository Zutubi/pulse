package com.zutubi.pulse.model;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * Pulse file details for versioned projects: where the pulse file is stored
 * in the SCM.
 */
public class VersionedPulseFileDetails extends PulseFileDetails
{
    private String pulseFileName;

    public VersionedPulseFileDetails()
    {

    }

    public VersionedPulseFileDetails(String pulseFileName)
    {
        this.pulseFileName = pulseFileName;
    }

    public VersionedPulseFileDetails copy()
    {
        return new VersionedPulseFileDetails(pulseFileName);
    }

    public boolean isBuiltIn()
    {
        return false;
    }

    public String getType()
    {
        return "versioned";
    }

    public Properties getProperties()
    {
        // TODO i18n
        Properties result = new Properties();
        result.put("pulse file", pulseFileName);
        return result;
    }

    public String getPulseFile(long id, Project project, Revision revision, PatchArchive patch)
    {
        String normalisedPath = FileSystemUtils.normaliseSeparators(pulseFileName);
        if (patch == null || !patch.containsPath(normalisedPath))
        {
            Scm scm = project.getScm();

            try
            {
                return scm.createServer().checkout(revision, pulseFileName);
            }
            catch (SCMException e)
            {
                throw new BuildException("Unable to retrieve pulse file from SCM: " + e.getMessage());
            }
        }
        else
        {
            try
            {
                return patch.retrieveFile(normalisedPath);
            }
            catch (IOException e)
            {
                throw new BuildException("Unable to retrieve pulse file from patch: " + e.getMessage());
            }
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
