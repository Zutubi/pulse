package com.zutubi.pulse.restore;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.plugins.PluginPaths;

import java.io.File;
import java.io.IOException;

/**
 *
 *
 */
public class RestorablePlugins implements Restorable
{
    private PluginPaths pluginPaths;

    public String getName()
    {
        return "plugins";
    }

    public void backup(File base) throws RestoreException
    {
        try
        {
            // this is a simple directory backup.
            FileSystemUtils.copy(base, pluginPaths.getPluginStorageDir());
        }
        catch (IOException e)
        {
            throw new RestoreException(e);
        }
    }

    public void restore(File base) throws RestoreException
    {
        try
        {
            // clean out the target directory prior to the restore.

            // this is a simple directory backup.
            FileSystemUtils.copy(pluginPaths.getPluginStorageDir(), base);

            // what about the other plugin path directories?  - the registry directory for instance.. maybe we should
            // just merge some of these to simplify the backup restore process.
        }
        catch (IOException e)
        {
            throw new RestoreException(e);
        }
    }

    public void setPluginPaths(PluginPaths pluginPaths)
    {
        this.pluginPaths = pluginPaths;
    }
}
