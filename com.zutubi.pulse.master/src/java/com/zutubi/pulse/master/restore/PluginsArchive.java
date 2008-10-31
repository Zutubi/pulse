package com.zutubi.pulse.master.restore;

import com.zutubi.pulse.core.plugins.PluginPaths;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 *
 *
 */
public class PluginsArchive extends AbstractArchiveableComponent
{
    private PluginPaths pluginPaths;

    public String getName()
    {
        return "plugins";
    }

    public String getDescription()
    {
        return "";
    }

    public void backup(File base) throws ArchiveException
    {
        try
        {
            // this is a simple directory backup.
            if (!base.exists() && !base.mkdirs())
            {
                throw new IOException("Failed to create directory: " + base.getCanonicalPath());
            }
            FileSystemUtils.copy(base, pluginPaths.getPluginStorageDir());
        }
        catch (IOException e)
        {
            throw new ArchiveException(e);
        }
    }

    public void restore(File base) throws ArchiveException
    {
        try
        {
            // clean out the target directory prior to the restore.  Any old plugins will be picked
            // up on restart and potentially started.  This could become confusing.  We are restoring
            // a backup, so the user should expect existing data to 'go away'.
            File storageDir = pluginPaths.getPluginStorageDir();
            if (!storageDir.exists() && !storageDir.mkdirs())
            {
                throw new IOException("Failed to create directory: " + storageDir.getCanonicalPath());
            }
            
            for (File f : storageDir.listFiles())
            {
                // we do not expect any directories here.  But, rather than running an rm -rf, lets
                // be  alittle careful.
                if (f.isFile())
                {
                    FileSystemUtils.delete(f);
                }
            }

            // this is a simple directory backup.
            FileSystemUtils.copy(storageDir, base);
        }
        catch (IOException e)
        {
            throw new ArchiveException(e);
        }
    }
    
    public void setPluginPaths(PluginPaths pluginPaths)
    {
        this.pluginPaths = pluginPaths;
    }
}
