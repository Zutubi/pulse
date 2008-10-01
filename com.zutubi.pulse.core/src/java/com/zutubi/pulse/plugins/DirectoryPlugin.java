package com.zutubi.pulse.plugins;

import com.zutubi.pulse.core.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import org.eclipse.osgi.framework.util.Headers;
import org.osgi.framework.BundleException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 *
 */
public class DirectoryPlugin extends LocalPlugin
{
    public DirectoryPlugin(File source)
    {
        super(source);
    }

    protected Headers loadPluginManifest(File pluginFile)
    {
        try
        {
            InputStream manifestIn = new FileInputStream(new File(pluginFile, FileSystemUtils.composeFilename("META-INF", "MANIFEST.MF")));
            try
            {
                return Headers.parseManifest(manifestIn);
            }
            finally
            {
                IOUtils.close(manifestIn);
            }
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(e);
        }
        catch (BundleException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    protected boolean delete()
    {
        return FileSystemUtils.rmdir(source);
    }

}
