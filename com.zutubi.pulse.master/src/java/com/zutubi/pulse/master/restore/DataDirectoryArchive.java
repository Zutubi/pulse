package com.zutubi.pulse.master.restore;

import com.zutubi.util.FileSystemUtils;
import com.zutubi.pulse.servercore.bootstrap.MasterUserPaths;

import java.io.File;
import java.io.IOException;

/**
 * This implementation of the archiveable component interface takes a snapshot of the
 * configuration contents of the PULSE_DATA directory.
 * <ul>
 * <li>PULSE_DATA/config/* -> base/config</li>
 * <li>PULSE_DATA/pulse.config.properties -> base/pulse.config.properties</li>
 * </ul>
 */
public class DataDirectoryArchive extends AbstractArchiveableComponent
{
    private MasterUserPaths paths;

    public String getName()
    {
        return "config";
    }

    public String getDescription()
    {
        return "The configuration restoration takes the appropriate 1.2.x system files and restores " +
                "them to their 2.0 locations.  Note, the one system file excluded from this process is the" +
                "database.properties file.  The database configured for your 2.0 installation will be retained.";
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
            throw new ArchiveException(e);
        }
    }

    public void restore(File archive) throws ArchiveException
    {
        // replace the existing files with the archived files.
        try
        {
            cleanup(paths.getUserConfigRoot());

            FileSystemUtils.delete(new File(paths.getData(), "pulse.config.properties"));

            // can not use the fsu.copy since it expects the destination directory to be empty, which
            // will not always be the case.
            File configRoot = paths.getUserConfigRoot();
            if (!configRoot.isDirectory() && !configRoot.mkdirs())
            {
                throw new IOException("Failed to create directory: " + configRoot.getCanonicalPath());
            }
            copy(paths.getUserConfigRoot(), new File(archive, "config").listFiles());

            FileSystemUtils.copy(paths.getData(), new File(archive, "pulse.config.properties"));
        }
        catch (IOException e)
        {
            throw new ArchiveException(e);
        }
    }

    private void copy(File dest, File[] files) throws IOException
    {
        for (File file : files)
        {
            if (file.isFile())
            {
                // does the file already exist? and if so, is it a database file?. If so, do not overwrite it.
                File target = new File(dest, file.getName());
                if (!target.exists())
                {
                    FileSystemUtils.copy(dest, file);
                }
                else
                {
                    if (!file.getName().startsWith("database"))
                    {
                        FileSystemUtils.copy(dest, file);
                    }
                }
            }
            else if (file.isDirectory())
            {
                File nestedDestination = new File(dest, file.getName());
                if (!nestedDestination.isDirectory() && !nestedDestination.mkdirs())
                {
                    throw new IOException("Failed to create destination directory: " + nestedDestination);
                }
                copy(nestedDestination, file.listFiles());
            }
        }
    }

    private void cleanup(File base) throws IOException
    {
        if (base.exists())
        {
            for (File file : base.listFiles())
            {
                if (file.isDirectory())
                {
                    cleanup(file);
                }
                if (!file.getName().startsWith("database"))
                {
                    FileSystemUtils.delete(file);
                }
            }
        }
    }

    public void setUserPaths(MasterUserPaths paths)
    {
        this.paths = paths;
    }
}
