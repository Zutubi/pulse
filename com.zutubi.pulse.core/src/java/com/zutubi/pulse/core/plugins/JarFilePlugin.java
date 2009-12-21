package com.zutubi.pulse.core.plugins;

import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import org.eclipse.osgi.framework.util.Headers;
import org.osgi.framework.BundleException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * An implementation of the local plugin that represents a bundle deployed
 * from a jar file.
 */
public class JarFilePlugin extends LocalPlugin
{
    public JarFilePlugin(File source)
    {
        super(source);
    }

    protected Headers loadPluginManifest(File pluginFile)
    {
        try
        {
            JarFile jarFile = null;
            try
            {
                jarFile = new JarFile(pluginFile);
                JarEntry entry = jarFile.getJarEntry("META-INF/MANIFEST.MF");
                if (entry == null)
                {
                    throw new IllegalArgumentException("No manifest found");
                }

                InputStream manifestIn = jarFile.getInputStream(entry);
                try
                {
                    return parseManifest(manifestIn);
                }
                finally
                {
                    IOUtils.close(manifestIn);
                }
            }
            finally
            {
                IOUtils.close(jarFile);
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
        try
        {
            FileSystemUtils.delete(source);
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }

}
