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
