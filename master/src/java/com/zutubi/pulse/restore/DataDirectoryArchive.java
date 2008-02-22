package com.zutubi.pulse.restore;

import com.zutubi.pulse.bootstrap.MasterUserPaths;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 *
 *
 */
public class DataDirectoryArchive extends AbstractArchivableComponent
{
    private MasterUserPaths paths;

    public String getName()
    {
        return "data";
    }

    public void backup(File archive) throws ArchiveException
    {
        try
        {
            FileSystemUtils.copy(new File(archive, "config"), paths.getUserConfigRoot());
            FileSystemUtils.copy(archive, new File(paths.getData(), "pulse.config.properties"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new ArchiveException(e);
        }
    }

    public void restore(File archive) throws ArchiveException
    {
        // replace the existing files with the archived files.

        try
        {
            FileSystemUtils.delete(paths.getUserConfigRoot());
            FileSystemUtils.delete(new File(paths.getData(), "pulse.config.properties"));
            
            FileSystemUtils.copy(paths.getUserConfigRoot(), new File(archive, "config"));
            FileSystemUtils.copy(paths.getData(), new File(archive, "pulse.config.properties"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new ArchiveException(e);
        }

    }

    public void setUserPaths(MasterUserPaths paths)
    {
        this.paths = paths;
    }
}
