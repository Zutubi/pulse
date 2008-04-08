package com.zutubi.pulse.restore;

import com.zutubi.pulse.bootstrap.MasterUserPaths;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 *
 *
 */
public class ArtifactArchive extends AbstractArchivableComponent
{
    private MasterUserPaths paths;

    public String getName()
    {
        return "artifacts";
    }

    public String getDescription()
    {
        return "The artifacts restoration consists of moving around the directories located within the " +
                "PULSE_DATA/projects directory to match the restructured project hierarchy.  This step is " +
                "only necessary if the projects directory has been manually transfered from the 1.2.x " +
                "PULSE_DATA directory into the 2.0 PULSE_DATA directory.";
    }

    public void backup(File archive) throws ArchiveException
    {

    }

    public void restore(File archive) throws ArchiveException
    {
        try
        {
            FileSystemUtils.copy(paths.getProjectRoot(), archive);
        }
        catch (IOException e)
        {
            throw new ArchiveException(e);
        }
    }

    public void setUserPaths(MasterUserPaths paths)
    {
        this.paths = paths;
    }
}
