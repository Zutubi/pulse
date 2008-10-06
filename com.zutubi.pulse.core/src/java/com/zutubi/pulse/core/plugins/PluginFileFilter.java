package com.zutubi.pulse.core.plugins;

import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.jar.JarFile;

/**
 *
 *
 */
public class PluginFileFilter implements FileFilter
{
    public boolean accept(File file)
    {
        if (file.isFile() && file.getName().endsWith(".jar"))
        {
            // check that the required manifest file is present.
            JarFile jarFile = null;
            try
            {
                jarFile = new JarFile(file);
                return jarFile.getJarEntry("META-INF/MANIFEST.MF") != null;
            }
            catch (IOException e)
            {
                return false;
            }
            finally
            {
                IOUtils.close(jarFile);
            }
        }
        else if (file.isDirectory())
        {
            // do a simple check for the required plugin manifest file.
            File manifestFile = new File(file, FileSystemUtils.composeFilename("META-INF", "MANIFEST.MF"));
            return manifestFile.isFile();
        }
        return false;
    }   
}
